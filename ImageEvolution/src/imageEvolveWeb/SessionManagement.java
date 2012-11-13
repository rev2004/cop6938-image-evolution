package imageEvolveWeb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.net.URLCodec;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.UpdateCondition;

public class SessionManagement {
	
	/** Source of randomness -
	 * ThreadLocal used to avoid blocking when used by concurrent threads.
	 * Uses SecureRandom with SHA1PRNG this time for greater security.
	 */
	private static final ThreadLocal<Random> rndSrc = new ThreadLocal <Random> () {
		@Override protected Random initialValue() { 
			try {
				Random tmpRnd = SecureRandom.getInstance("SHA1PRNG");
				byte[] b = new byte[1];
				tmpRnd.nextBytes(b);
				return tmpRnd;
			} catch (NoSuchAlgorithmException e){
				return null;
			}
		}
	};
	
	private static AmazonSimpleDBClient sdb = null;
	public static final String HMAC_Algorithm = "HmacSHA1";
	public static final String HMAC_Key = "";
	
	public String sessionId;
	public String userId;
	public String userName;
	public String friendlyName;
	public String jSession;
	public Date lastActivity;
	
	public SessionManagement(){
		this.sessionId = null;
		this.userId = null;
		this.userName = null;
		this.friendlyName = null;
		this.jSession = null;
		this.lastActivity = null;
	}
	
	/** Get the SimpleDb connect (initialize if not already existing)
	 * keeps multiple instances of connection from being created
	 */
	private static AmazonSimpleDBClient getSDB(){
		if (sdb==null){ 
			AWSCredentials cred;
			try {
				cred = new PropertiesCredentials(
				        SessionManagement.class.getClassLoader().getResourceAsStream("AwsCredentials.properties"));
				sdb = new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				sdb = null;
			}
		}
		return sdb;
	}
	
	/** Allocate an new globally unique session and return
	 * the session identifier (cookie).
	 * @param username
	 * @return
	 */
	public static SessionManagement makeSession(String userId, String userName, String jSession){
		// Initialize return object
		SessionManagement newSession = new SessionManagement();
		newSession.userName = userName;
		newSession.userId = userId;
		newSession.jSession = jSession;
		newSession.lastActivity = new Date();
		// try to get a new session Id until succeed
		boolean done = false;
		while (!done){
			// randomly guess a new session id
			newSession.sessionId = randomSessionId(20);
			// grab the connection object
			AmazonSimpleDBClient sdb_loc = getSDB();
			// check for collision
			GetAttributesResult existing = sdb_loc.getAttributes(new GetAttributesRequest()
					.withDomainName("ImgEvo_sessions")
					.withItemName(newSession.sessionId)
					.withConsistentRead(true));
			
			PutAttributesRequest putReq = new PutAttributesRequest();
			// set new values
			putReq = putReq.withDomainName("ImgEvo_sessions").withItemName(newSession.sessionId);
			putReq = putReq.withAttributes(new ReplaceableAttribute("userId",newSession.userId,true));
			putReq = putReq.withAttributes(new ReplaceableAttribute("jSession",newSession.jSession,true));
			putReq = putReq.withAttributes(new ReplaceableAttribute("userName",newSession.userName,true));
			putReq = putReq.withAttributes(new ReplaceableAttribute("friendlyName",
					newSession.friendlyName,true));
			putReq = putReq.withAttributes(new ReplaceableAttribute("lastActivity",
					Long.toString(newSession.lastActivity.getTime()),true));
			// set update conditions assuming no existing
			putReq = putReq.withExpected(new UpdateCondition().withName("userId").withExists(false));
			putReq = putReq.withExpected(new UpdateCondition().withName("userName").withExists(false));
			putReq = putReq.withExpected(new UpdateCondition().withName("userjSession").withExists(false));
			// loop through existing and set expected values
			for (Attribute a : existing.getAttributes()){
				if (a.getName().equals("userId")){ 
					putReq = putReq.withExpected(new UpdateCondition("userId", a.getValue(), true));
				} else if (a.getName().equals("userName")){ 
					putReq = putReq.withExpected(new UpdateCondition("userName", a.getValue(), true));
				} else if (a.getName().equals("jSession")){ 
					putReq = putReq.withExpected(new UpdateCondition("jSession", a.getValue(), true));
				}
			}
			// make update
			sdb_loc.putAttributes(putReq);
			// check update success
			done = true;
			existing = sdb_loc.getAttributes(new GetAttributesRequest()
					.withDomainName("ImgEvo_sessions")
					.withItemName(newSession.sessionId)
					.withConsistentRead(true));
			for (Attribute a : existing.getAttributes()){
				if (!a.getName().equals(newSession.userId)){ 
					done = false;
				} else if (!a.getName().equals(newSession.userName)){ 
					done = false;
				} else if (!a.getName().equals(newSession.jSession)){ 
					done = false;
				}
			}
			
		}
		return newSession;
	}
	
	private static String randomSessionId(int byteLength){
		// get some random bytes
		byte[] sessionId = new byte[byteLength];
		rndSrc.get().nextBytes(sessionId);
		// convert random bytes to hex
		byte[] hexBytes = new Hex().encode(sessionId);
		// return hex as string UTF-8
		try {
			return new String(hexBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public String getCookie(){
		// session id
		String tmp = this.sessionId;
		// do hmac on session id for validation
		try {
			byte[] input = Hex.decodeHex(this.sessionId.toCharArray());
		    byte[] keyBytes = Hex.decodeHex(SessionManagement.HMAC_Key.toCharArray());
		    Key key = new SecretKeySpec(keyBytes,0,keyBytes.length,SessionManagement.HMAC_Algorithm); 
		    Mac mac = Mac.getInstance(SessionManagement.HMAC_Algorithm);
		    mac.init(key); 
		    tmp += "_"+String.valueOf(Hex.encodeHex(mac.doFinal(input)));
		} catch (Exception e) {
			tmp += "_";
		}
		try {
			tmp += "_"+(new URLCodec()).encode(this.userId);
		} catch (EncoderException e) {
			tmp += "_";
		}
		return tmp;
	}
	public static SessionManagement parseCookie(){
		return null;
	}
	public static boolean validCookie(String cookie){
		return false;
	}

		
	
}
