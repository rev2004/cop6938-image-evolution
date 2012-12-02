package imageEvolveWeb;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class RequestManagement {
	
	/** Simple Queue Service connection;
	 * Thread local used to prevent blocking when threads concurrent.
	 */
	private static final ThreadLocal<AmazonSQSClient> sqs = new ThreadLocal<AmazonSQSClient>() {
		@Override protected AmazonSQSClient initialValue() { 
			try {
				AWSCredentials cred = new PropertiesCredentials(
						RequestServlet.class.getClassLoader()
				        .getResourceAsStream("AwsCredentials.properties"));
				return new AmazonSQSClient(cred);
			} catch (IOException e) {
				return null;
			}
		}
	};
	
	private static final String sqsQueueUrl = "https://sqs.us-east-1.amazonaws.com/551430067291/ImgEvo_reqs";
	
	
	/* instance variables */
	public String imageId;		// ImageId and base Id for evolved and genetic outputs
	public String targetId;		// Target image if different than Id
	public String baseGen;		// first generation starting DNA
	public double fitThresh;	// threshold fitness once exceeded can finish
	public long genThresh;		// threshold (limit) on number of generations
	public boolean strictThresh;	// strict threshold, both fitness and generation
									//thresholds must be satisfied before completion
									//Otherwise only one.
	public String receiptHandle; // SQS receiptHandle, used to clear the message from queue after completed
	
	
	public RequestManagement(){
		imageId=null;
		targetId=null;
		baseGen=null;
		fitThresh=0.0;
		genThresh=0;
		strictThresh=false;
		receiptHandle=null;
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
	
	public static RequestManagement parseMsg(JSONObject msg){
		RequestManagement tmp = new RequestManagement();
		tmp.imageId = msg.optString("imageId");
		tmp.targetId = msg.optString("targetId");
		tmp.baseGen = msg.optString("baseGen");
		tmp.fitThresh = msg.optDouble("fitThresh");
		tmp.genThresh = msg.optLong("genThresh");
		tmp.strictThresh = msg.optBoolean("strictThresh");
		return tmp;
	}
	public static RequestManagement parseMsg(String msg){
		try {
			return parseMsg(new JSONObject(msg));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void sendSqsMsg(RequestManagement req){
		sqs.get().sendMessage(new SendMessageRequest(sqsQueueUrl, 
				req.createMsg().toString()));
	}
	
	/**
	 * @param visTimeout The duration (in seconds) that the received messages are hidden from subsequent 
	 retrieve requests after being retrieved by a ReceiveMessage request.
	 * @return
	 */
	public static RequestManagement recvSqsMsg(int visTimeout){
		RequestManagement tmp = null;
		ReceiveMessageRequest req = new ReceiveMessageRequest();
		req = req.withQueueUrl(RequestManagement.sqsQueueUrl);
		req = req.withMaxNumberOfMessages(1);
		req = req.withVisibilityTimeout(visTimeout);
		req = req.withWaitTimeSeconds(10); // 10 second
		ReceiveMessageResult recv=null;
		// Loop until messages appear
		// 10 second msg wait + 20 second sleep = 30 second each loop
		for (int i=0; i<4; i++) {
			recv = sqs.get().receiveMessage(req);
			if(!recv.getMessages().isEmpty()){
				break;
			}
			// wait for 20 seconds
			try { Thread.sleep(20000); } catch (InterruptedException e) {}  
		};
		// check if 
		if (!recv.getMessages().isEmpty()){
			tmp = parseMsg(recv.getMessages().get(0).getBody());
			tmp.receiptHandle = recv.getMessages().get(0).getReceiptHandle();
			return tmp;
		} else {
			return null;
		}
	}
	
	public static void delSqsMsg(String receiptHandle){
		DeleteMessageRequest del = new DeleteMessageRequest()
			.withQueueUrl(RequestManagement.sqsQueueUrl)
			.withReceiptHandle(receiptHandle);
		sqs.get().deleteMessage(del);
	}
	
	@SuppressWarnings("unused")
	private static String getQueueUrl(String QueueName){
		//ListQueuesResult qlist = sqs.get().listQueues(new ListQueuesRequest(QueueName));
		//qlist.
		return null;
	}
	
}
