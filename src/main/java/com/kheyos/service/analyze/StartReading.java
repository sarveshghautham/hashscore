package com.kheyos.service.analyze;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;

public class StartReading {

    private final String keyFile = "src/main/resources/secret.txt";
    private final String keywordFile = "src/main/resources/keywords.txt";
    private final String scoreFile = "src/main/resources/scores.txt";
    public static HashScore hs = null;
    private UpdateTopWords updateWords;
    private Timer updateSetTimer = null;

    public StartReading() {
		updateSetTimer = new Timer();
	}
	
	public void startProcess() throws IOException {
		//Read the keywords and pass the secret.txt file
        BufferedReader br = new BufferedReader(new FileReader(keywordFile));
        String keywords="";

        String team1 = br.readLine();
        String team2 = br.readLine();

        //Counting the words in a timer
        updateWords = new UpdateTopWords(team1, team2);
        updateSetTimer.schedule(updateWords, 0, 60000);

        String matchTag = br.readLine();
		while ((keywords = br.readLine()) != null) {
			hs = new HashScore (keyFile, keywords, matchTag, updateWords);
			hs.start();
		}
		br.close();
	}

	public static void main (String []args) throws IOException {
		StartReading srObj = new StartReading();
		srObj.startProcess();
	}
	
	
}
