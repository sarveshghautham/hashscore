package hashscore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class HashScore implements Runnable {
	
	private String consumerKey = "";
	private String consumerSecret = "";
	private String token = "";
	private String secret = "";
	private BlockingQueue<String> msgQueue ;
	private BlockingQueue<Event> eventQueue;
	private Hosts hosebirdHosts ;
	private Authentication hosebirdAuth ;
	public StatusesFilterEndpoint hosebirdEndpoint ;
	private Client hosebirdClient;
	
	public void readKeyFromFile () throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/secret.txt"));
		
		consumerKey = reader.readLine();
		consumerSecret = reader.readLine();
		token = reader.readLine();
		secret = reader.readLine();
		
		reader.close();
	}
	
	public void setup () {
		/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
		msgQueue = new LinkedBlockingQueue<String>(100000);
		eventQueue = new LinkedBlockingQueue<Event>(1000);
	
		/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
		hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		hosebirdEndpoint = new StatusesFilterEndpoint();
		// Optional: set up some followings and track terms
		List<Long> followings = Lists.newArrayList(1234L, 566788L);
		List<String> terms = Lists.newArrayList("indvseng");
		hosebirdEndpoint.followings(followings);
		hosebirdEndpoint.trackTerms(terms);
		
	}
	
	public void authenticate () {
		// These secrets should be read from a config file
		hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);
	}
	
	public void connect () {
		ClientBuilder builder = new ClientBuilder()
		  .name("HashScore")                              // optional: mainly for the logs
		  .hosts(hosebirdHosts)
		  .authentication(hosebirdAuth)
		  .endpoint(hosebirdEndpoint)
		  .processor(new StringDelimitedProcessor(msgQueue))
		  .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

		hosebirdClient = builder.build();
		// Attempts to establish a connection.
		hosebirdClient.connect();

	}
	
	
	public static void main (String []args) throws IOException {
				
		HashScore hs = new HashScore();
		hs.readKeyFromFile();
		hs.setup();
		hs.authenticate();
		hs.connect();
		Thread readMessage = new Thread(hs);
		readMessage.start();
	}

	public void run() {
		// TODO Auto-generated method stub
		while (!hosebirdClient.isDone()) {
			String msg = "";
			try {
				msg = msgQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(msg);		  
		}
	}
	
	public void terminate () {
		hosebirdClient.stop();
	}
	
}
