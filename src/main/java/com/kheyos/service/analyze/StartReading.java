package com.kheyos.service.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

public class StartReading {

	private final String keyFile = "/secret.txt";
    private final String keywordFile = "/keywords.txt";
    public static HashScore hs = null;
    private UpdateTopWords updateWords;
    private Timer updateSetTimer = null;
    private HashMap<String, Integer> avoidKeywords; 
    public StartReading() {
		updateSetTimer = new Timer();
		avoidKeywords = new HashMap<String, Integer>();
		
		//standard keywords to avoid
		avoidKeywords.put("rt", 0);
		avoidKeywords.put("#cwc", 0);
		avoidKeywords.put("#CWC", 0);
        avoidKeywords.put("Sri", 0);
        avoidKeywords.put("Lanka", 0);
        avoidKeywords.put("sri", 0);
        avoidKeywords.put("lanka", 0);
        avoidKeywords.put("south", 0);
        avoidKeywords.put("africa", 0);
        avoidKeywords.put("South", 0);
        avoidKeywords.put("Africa", 0);
	}
	
	public void startProcess() throws IOException {
		
		BufferedReader br = null;
		InputStream stream = StartReading.class.getResourceAsStream(keywordFile);
		
		try {
			
			br = new BufferedReader(new InputStreamReader(stream));
	        String keywords="";

	        String team1 = br.readLine();
	        String team2 = br.readLine();
	        int matchId = Integer.parseInt(br.readLine());
	        
	        avoidKeywords.put(team1, 0);
	        avoidKeywords.put(team1.toLowerCase(), 0);
	        avoidKeywords.put(team2, 0);
	        avoidKeywords.put(team2.toLowerCase(), 0);
	        
	        //Counting the words in a timer
	        updateWords = new UpdateTopWords(team1, team2, matchId, avoidKeywords);
	        updateSetTimer.schedule(updateWords, 0, 60000);
	    
	        //Add all keywords to this arraylist
	        ArrayList<String> trackingKeywords = new ArrayList<String>();
	        
			while ((keywords = br.readLine()) != null) {
				avoidKeywords.put(keywords, 0);
				keywords = keywords.toLowerCase();
				trackingKeywords.add(keywords);
			}
			
			hs = new HashScore (keyFile, trackingKeywords, updateWords);
			hs.readTweets();
		
		}	
		finally {
			if (br != null)
				br.close();	
		}
		
	}

	public static void main (String []args) throws IOException {
		
		StartReading srObj = new StartReading();
		srObj.startProcess();
	}
	
	
}
