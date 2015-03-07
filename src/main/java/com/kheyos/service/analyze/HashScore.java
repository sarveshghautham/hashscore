package com.kheyos.service.analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

public class HashScore {

	private final String keyFile = "/secret.txt";
    private final String keywordFile = "/keywords.txt";
    private final String avoidKeywordsFile = "/avoidKeywords.txt";

    public static ReadTweets readTweet = null;
    private UpdateTopWords updateWords;
    private Timer updateSetTimer = null;

    private HashMap<String, Integer> avoidKeywords;

    public HashScore() {
		updateSetTimer = new Timer();
        avoidKeywords = new HashMap<>();
	}

    public void readAvoidKeywords() throws IOException {
        BufferedReader br = null;
        InputStream stream = HashScore.class.getResourceAsStream(avoidKeywordsFile);

        try {

            br = new BufferedReader(new InputStreamReader(stream));
            String keywords="";

            while ((keywords = br.readLine()) != null) {
                avoidKeywords.put(keywords, 0);
            }
        }
        finally {
            if (br != null)
                br.close();
        }
    }

	public void startProcess() throws IOException {
		
		BufferedReader br = null;
		InputStream stream = HashScore.class.getResourceAsStream(keywordFile);
		
		try {
			
			br = new BufferedReader(new InputStreamReader(stream));
	        String keywords="";

	        String team1 = br.readLine();
	        String team2 = br.readLine();
	        int matchId = Integer.parseInt(br.readLine());
            String yMatchId = br.readLine();
            int k = Integer.parseInt(br.readLine());
	        
	        avoidKeywords.put(team1, 0);
	        avoidKeywords.put(team1.toLowerCase(), 0);
	        avoidKeywords.put(team2, 0);
	        avoidKeywords.put(team2.toLowerCase(), 0);
	        
	        //Counting the words in a timer
	        updateWords = new UpdateTopWords(team1, team2, matchId, avoidKeywords, yMatchId, k);
	        updateSetTimer.schedule(updateWords, 0, 10000);
	    
	        //Add all keywords to this arraylist
	        ArrayList<String> trackingKeywords = new ArrayList<String>();
	        
			while ((keywords = br.readLine()) != null) {
				avoidKeywords.put(keywords, 0);
				keywords = keywords.toLowerCase();
				trackingKeywords.add(keywords);
			}
			
			readTweet = new ReadTweets(keyFile, trackingKeywords, updateWords);
			readTweet.readTweets();
		
		}	
		finally {
			if (br != null)
				br.close();	
		}
		
	}

	public static void main (String []args) throws IOException {
		
		HashScore startProcess = new HashScore();
        startProcess.readAvoidKeywords();
		startProcess.startProcess();
	}
	
	
}
