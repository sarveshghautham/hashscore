package com.kheyos.service.analyze;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class HashScore {
	
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
	private String keyFile;
	
    private UpdateTopWords updateWords;
    private POSTagger taggerObj;
    private ArrayList<String> trackingKeywords;

	public HashScore() {

	}

	public HashScore(String keyFile, ArrayList<String> keywords, UpdateTopWords wordsInstance) {
        this.keyFile = keyFile;
		this.trackingKeywords = keywords;
        this.updateWords = wordsInstance;
        this.taggerObj = POSTagger.getTaggerInstance();
	}
	
	public void readKeyFromFile() throws IOException {
		
		InputStream stream = StartReading.class.getResourceAsStream(keyFile);
		
		BufferedReader reader = null;
		
		try {
		
			reader = new BufferedReader(new InputStreamReader(stream));
			
			consumerKey = reader.readLine();
			consumerSecret = reader.readLine();
			token = reader.readLine();
			secret = reader.readLine();
			
		} 
		finally {
			if (reader != null)
				reader.close();
		}
		
	}
	
	public void setup() {
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
	
	public void authenticate() {
		// These secrets should be read from a config file
		hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);
	}
	
	public void connect() {
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

	public void JSONData(String msg) throws IOException {
        //read json file data to String
        byte[] jsonData = msg.getBytes();

        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        //read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(jsonData);
        JsonNode tweetNode = rootNode.path("text");

        String tweet = cleanTweets(tweetNode.asText());

        ArrayList<String> words = null;
        if (tweet != null) {
            words = taggerObj.getWords(tweet);
            if (words != null) {
                updateWords.updateWordsInMap(words);
            }
        }
	}

    public String cleanTweets(String tweet) {

        String cleanTweet = "";
        try {
            byte[] utf8Bytes = tweet.getBytes("UTF-8");

            cleanTweet = new String(utf8Bytes, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Pattern unicodeOutliers = Pattern.compile("[^\\x00-\\x7F]",
                Pattern.UNICODE_CASE | Pattern.CANON_EQ
                        | Pattern.CASE_INSENSITIVE);
        Matcher unicodeOutlierMatcher = unicodeOutliers.matcher(cleanTweet);
        cleanTweet = unicodeOutlierMatcher.replaceAll("");

        return cleanTweet;

    }

    public void terminate () {
		hosebirdClient.stop();
	}

	public void readTweets() {
		
		try {
			readKeyFromFile();
			setup();
			authenticate();
			connect();
			
			while (!hosebirdClient.isDone()) {
				String msg = msgQueue.take();
				JSONData(msg);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	}
}