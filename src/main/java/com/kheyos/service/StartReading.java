package com.kheyos.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;

public class StartReading {

	public StartReading() {
		// TODO Auto-generated constructor stub
	}
	
	public void startProcess () throws IOException {
		//Read the keywords and pass the secret.txt file
		HashScore hs = null;
		String keyFile = "src/main/resources/secret.txt";		
		String keywordFile = "src/main/resources/keywords.txt";
		String scoreFile = "src/main/resources/scores.txt";
		String line="";
		
		BufferedReader br = new BufferedReader(new FileReader(keywordFile));
		String matchTag = br.readLine();
		while ((line = br.readLine()) != null) {
			hs = new HashScore (keyFile, line, matchTag);
			hs.start();
		}
		br.close();
		
		String []score = new String[2];
		br = new BufferedReader(new FileReader(scoreFile));
		Timer readScoreTimer = new Timer();
		int i = 0;
		
		//Start reading scores.
		while ((line = br.readLine()) != null) {
			score[i] = line;
			i++;
		}
		
		readScoreTimer.schedule(new ReadScores(hs, matchTag, score[0], score[1]), 0, 60000);
		
		br.close();
		
	}
	
	public static void main (String []args) throws IOException {
		StartReading srObj = new StartReading();
		srObj.startProcess();
	}
	
	
}
