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
	public StartReading() {
		// TODO Auto-generated constructor stub
	}
	
	public void startProcess() throws IOException {
		//Read the keywords and pass the secret.txt file

		String keywords="";
		
		BufferedReader br = new BufferedReader(new FileReader(keywordFile));
		String matchTag = br.readLine();
		while ((keywords = br.readLine()) != null) {
			hs = new HashScore (keyFile, keywords, matchTag);
			hs.start();
		}
		br.close();

        //readScores();
	}

    public void readScores() {
        /* Not doing the score reading part now
		String []score = new String[2];
		br = new BufferedReader(new FileReader(scoreFile));
		Timer readScoreTimer = new Timer();
		int i = 0;

		//Start reading scores.
		while ((keywords = br.readLine()) != null) {
			score[i] = keywords;
			i++;
		}

		readScoreTimer.schedule(new ReadScores(hs, matchTag, score[0], score[1]), 0, 60000);

		br.close();
		*/
    }
	
	public static void main (String []args) throws IOException {
		StartReading srObj = new StartReading();
		srObj.startProcess();
	}
	
	
}
