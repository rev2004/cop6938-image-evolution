package imageEvolveWeb;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.UpdateCondition;

public class StorageManagement {

	
	/** Source of randomness.
	 * ThreadLocal used to avoid blocking when used by
	 * concurrent threads
	 */
	private static final ThreadLocal<Random> rndSrc =
			new ThreadLocal <Random> () {
				@Override protected Random initialValue() { return new Random(); }
	};
	
	/** SimpleDB connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonSimpleDBClient> sdb = new ThreadLocal<AmazonSimpleDBClient>() {
		@Override protected AmazonSimpleDBClient initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSimpleDBClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	/** Simple Storage Service connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonS3Client> s3 = new ThreadLocal<AmazonS3Client>() {
		@Override protected AmazonS3Client initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonS3Client(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
	/** Allocates a unique random string for an image file name in S3.
	 * Available S3 keys are determined by synchronizing with SimpleDB.
	 * Sets some additional metadata into SimpleDB about the image such as
	 * created date (current time at method call).
	 * @param suffix desired ending suffix (e.g. file extension) of file name 
	 * @param name user specified name of image
	 * @param description user specified description of image
	 * @param userId owner (creator) of image.
	 * @return allocated Image 
	 */
	public static String allocateImageId(String suffix, String targetId, String userId, 
			String name, String description){
		boolean done = false;
		String imageId = null;
		while (!done){
			// randomly guess a new session id
			imageId = randomImageId(18)+((suffix!=null)?suffix:"");
			String created = Long.toString((new Date()).getTime());
			//System.out.println("rndSessionId: "+tmp.sessionId);
			// create update request
			PutAttributesRequest putReq = new PutAttributesRequest();
			// set session parameters
			putReq = putReq.withDomainName("ImgEvo_images").withItemName(imageId);
			putReq = putReq.withAttributes(new ReplaceableAttribute("userId",
					userId,true));
			putReq = putReq.withAttributes(new ReplaceableAttribute("created",
					created,true));
			if (targetId!=null && !targetId.equals("")){
				putReq = putReq.withAttributes(new ReplaceableAttribute("targetId",
						targetId,true));
			} else {
				putReq = putReq.withAttributes(new ReplaceableAttribute("targetId",
						imageId,true));
			}
			if (name!=null && !name.equals("")){
				putReq = putReq.withAttributes(new ReplaceableAttribute("name",
						name,true));
			}
			if (description!=null && !description.equals("")){
				putReq = putReq.withAttributes(new ReplaceableAttribute("description",
						description,true));
			}
			// set update conditions to prevent overwriting existing image
			putReq = putReq.withExpected(new UpdateCondition().withName("userId").withExists(false));
			putReq = putReq.withExpected(new UpdateCondition().withName("created").withExists(false));
			// make update
			sdb.get().putAttributes(putReq);
			// check update success
			GetAttributesResult existing = sdb.get().getAttributes(new GetAttributesRequest()
					.withDomainName("ImgEvo_images")
					.withItemName(imageId)
					.withAttributeNames("userId")
					.withAttributeNames("created")
					.withConsistentRead(true));
			done = !existing.getAttributes().isEmpty();
			for (Attribute a : existing.getAttributes()){
				if (a.getName().equals("userId")){
					done = (done) ? a.getValue().equals(userId) : false;
				} else if (a.getName().equals("created")){ 
					done = (done) ? a.getValue().equals(created) : false;
				}
			}
		}
		return imageId;
	}
	/** Generate a random string in base64 URL-safe form
	 * gives specified bytes of entropy (will return more
	 * bytes in characters though due to 3byte->4byte base64
	 * conversion.
	 * @param byteLength bytes of entropy (variability)
	 * @return base64 URL-safe encoded string
	 */
	public static String randomImageId(int byteLength){
		// get some random bytes
		byte[] rndBytes = new byte[byteLength];
		rndSrc.get().nextBytes(rndBytes);
		// convert random bytes to hex
		byte[] b64Bytes = Base64.encodeBase64(rndBytes);
		// return base64 as string UTF-8
		// unfortunately commons codec 1.3 is missing the url-safe b64 variant
		// so i have to make that transformation here.
		try {
			return new String(b64Bytes, "UTF-8").replace('+', '-').replace('/', '_');
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public static InputStream getImage(String imgKey){
		return s3.get().getObject("ImgEvo",imgKey).getObjectContent();
	}
	public static boolean storeImage(String imgKey, BufferedImage img){
		try {
			// write image data in memory
			ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
			ImageIO.write(img, "png", imgOutput);
			ByteArrayInputStream s3Input = new ByteArrayInputStream(imgOutput.toByteArray());
			// set file metadata
			ObjectMetadata fileMeta = new ObjectMetadata();
			fileMeta.setContentType("image/png");
			fileMeta.setContentLength(s3Input.available());
			// upload
			s3.get().putObject("ImgEvo", imgKey, s3Input, fileMeta);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
}
