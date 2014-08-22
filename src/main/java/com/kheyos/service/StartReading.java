package com.kheyos.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StartReading {

	public StartReading() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main (String []args) throws IOException {
		HashScore hs;
		String keyFile = "src/main/resources/secret.txt";		
		String keywordFile = "src/main/resources/keywords.txt";
		String line="";
		
		BufferedReader br = new BufferedReader(new FileReader(keywordFile));
		String match_tag = br.readLine();
		while ((line = br.readLine()) != null) {
			hs = new HashScore (keyFile, line, match_tag);
			hs.start();
		}
		
		br.close();
	}
	
	
}
