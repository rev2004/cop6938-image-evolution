package imageEvolveWeb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;

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
	public static String makeSession(String userId, String userName, String jSession){
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
			GetAttributesResult record = sdb_loc.getAttributes(
					new GetAttributesRequest("ImgEvo_sessions",newSession.sessionId).withConsistentRead(true));
			// set new values
			PutAttributesRequest req = new PutAttributesRequest();
			req = req.withDomainName("ImgEvo_sessions").withItemName(newSession.sessionId);
			req = req.withAttributes(new ReplaceableAttribute("userId",newSession.userId,true));
			req = req.withAttributes(new ReplaceableAttribute("jSession",newSession.jSession,true));
			req = req.withAttributes(new ReplaceableAttribute("userName",newSession.userName,true));
			req = req.withExpected(new UpdateCondition().withName("userId").withExists(false));
			req = req.withExpected(new UpdateCondition().withName("userName").withExists(false));
			req = req.withExpected(new UpdateCondition().withName("userjSession").withExists(false));
			// loop through existing and set expected values
			for (Attribute a : record.getAttributes()){
				if (a.getName().equals("userId")){ 
					req = req.withExpected(new UpdateCondition("userId", a.getValue(), true));
				} else if (a.getName().equals("userName")){ 
					req = req.withExpected(new UpdateCondition("userName", a.getValue(), true));
				} else if (a.getName().equals("jSession")){ 
					req = req.withExpected(new UpdateCondition("jSession", a.getValue(), true));
				}
			}
			// make update
			sdb_loc.putAttributes(req);
			// check update success
			done = true;
			record = sdb_loc.getAttributes(
					new GetAttributesRequest("ImgEvo_sessions",newSession.sessionId).withConsistentRead(true));
			for (Attribute a : record.getAttributes()){
				if (!a.getName().equals(newSession.userId)){ 
					done = false;
				} else if (!a.getName().equals(newSession.userName)){ 
					done = false;
				} else if (!a.getName().equals(newSession.jSession)){ 
					done = false;
				}
			}
			
		}
		return newSession.toString();
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
	
	
	
}
