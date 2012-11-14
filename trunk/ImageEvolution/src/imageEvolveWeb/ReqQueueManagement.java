package imageEvolveWeb;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class ReqQueueManagement {
	
	/** Simple Queue Service connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonSQSClient> sqs = new ThreadLocal<AmazonSQSClient>() {
		@Override protected AmazonSQSClient initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						EvoRequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSQSClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
	private static final String sqsQueueUrl = "https://sqs.us-east-1.amazonaws.com/551430067291/ImgEvo_reqs";
	
	
	/* instance variables */
	public String imageId;		// ImageId and base ID for evolved and genetic outputs
	public String targetId;		// Target image if different than Id
	public String baseGen;		// first generation starting DNA
	public double fitThresh;	// threshold fitness once exceeded can finish
	public long genThresh;		// threshold (limit) on number of generations
	public boolean strictThresh;	// strict threshold, both fitness and generation
									//thresholds must be satisfied before completion
									//Otherwise only one.
	
	
	public ReqQueueManagement(){
		imageId=null;
		targetId=null;
		baseGen=null;
		fitThresh=0.0;
		genThresh=0;
		strictThresh=false;
	}
	
	public JSONObject createMsg(){
		JSONObject msg = new JSONObject();
		try {
			msg.putOnce("imageId", this.imageId);
			msg.putOnce("targetId", this.targetId);
			msg.putOnce("baseGen", this.baseGen);
			if (this.fitThresh>0.0) {msg.put("fitThresh", this.fitThresh); }
			if (this.genThresh>0) {msg.put("genThresh",this.genThresh); }
			msg.put("strictThresh",this.strictThresh);
		} catch (JSONException e) {
			msg = null;
		}
		return msg;
	}
	
	public static ReqQueueManagement parseMsg(JSONObject msg){
		ReqQueueManagement tmp = new ReqQueueManagement();
		tmp.imageId = msg.optString("imageId");
		tmp.targetId = msg.optString("targetId");
		tmp.baseGen = msg.optString("baseGen");
		tmp.fitThresh = msg.optDouble("fitThresh");
		tmp.genThresh = msg.optLong("genThresh");
		tmp.strictThresh = msg.optBoolean("strictThresh");
		return tmp;
	}

	public void sendSqsMsg(){
		sqs.get().sendMessage(new SendMessageRequest(sqsQueueUrl, 
				this.createMsg().toString()));
	}
	
	/**
	 * @param visTimeout The duration (in seconds) that the received messages are hidden from subsequent 
	 retrieve requests after being retrieved by a ReceiveMessage request.
	 * @return
	 */
	public static String recvSqsMsg(int visTimeout){
		ReceiveMessageRequest req = new ReceiveMessageRequest();
		req = req.withQueueUrl(ReqQueueManagement.sqsQueueUrl);
		req = req.withMaxNumberOfMessages(1);
		req = req.withVisibilityTimeout(visTimeout);
		req = req.withWaitTimeSeconds(10); // 10 second
		ReceiveMessageResult recv=null;
		// Loop until messages appear
		// 10 second msg wait + 20 second sleep = 30 second each loop
		do {
			// wait for 20 seconds
			try { Thread.sleep(20000); } catch (InterruptedException e) {}  
			recv = sqs.get().receiveMessage(req);
			if(!recv.getMessages().isEmpty()){
				break;
			}
		} while (true); 
		return recv.getMessages().get(0).getBody();
	}
	
	@SuppressWarnings("unused")
	private static String getQueueUrl(String QueueName){
		//ListQueuesResult qlist = sqs.get().listQueues(new ListQueuesRequest(QueueName));
		//qlist.
		return null;
	}
	
	
	
}
