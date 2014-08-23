package com.kheyos.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private String trackingKeywords;
	private String matchTag;
	private Thread t_ReadMessage;
	private String keyFile;
	private int trackingFor;
	public HashMap<Integer,Integer> track;
	
	
	public HashScore () {
		
	}
	
	public HashScore (String keyFile, String keyword, String match_tag) {
		this.keyFile = keyFile;
		this.matchTag = match_tag;
		track = new HashMap<Integer, Integer>();
		
		if (keyword.equals("six") || keyword.equals("SIX") || keyword.equals("6")) {
			trackingFor = 6;			
		}
		else if (keyword.equals("four") || keyword.equals("FOUR") || keyword.equals("4")) {
			trackingFor = 4;			
		}
		else if (keyword.equals("wicket")) {
			trackingFor = -1;
		}	
		track.put(trackingFor, 0);
		trackingKeywords = matchTag + " " + keyword;
		
	}
	
	public void readKeyFromFile () throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(keyFile));
		
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
		List<String> terms = Lists.newArrayList(trackingKeywords);
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
	
	
	public void start () throws IOException {
				
		t_ReadMessage = new Thread(this);
		t_ReadMessage.start();
	}
	
	public void JSONData (String msg) throws JsonProcessingException, IOException {
		//read json file data to String
		byte[] jsonData = msg.getBytes();
		 
		//create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();
		 
		//read JSON like DOM Parser
		JsonNode rootNode = objectMapper.readTree(jsonData);
		JsonNode dateNode = rootNode.path("created_at");
		JsonNode tweetNode = rootNode.path("text");
		
		String date = dateNode.asText();
		String tweet = tweetNode.asText(); 
		int count=0;
		
		//System.out.println("date = "+date);
		//System.out.println("text = "+tweet);
		
		if (tweet.contains(trackingKeywords) && track.containsKey(trackingFor)
				) {
			
			count = track.get(trackingFor);			
			count++;
			
			synchronized (track) {
				track.replace(Integer.parseInt(trackingKeywords), count);	
			}
			
			System.out.println("date = "+date);
			System.out.println("text = "+tweet);	
			
		}
	}
	
	public void terminate () {
		hosebirdClient.stop();
	}

	public void run() {
		
		// TODO Auto-generated method stub
		
	    try {
			readKeyFromFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		setup();
		authenticate();
		connect();
		
		while (!hosebirdClient.isDone()) {
			String msg = "";
			try {
				msg = msgQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				JSONData(msg);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println(msg);					
		}
	}
}
