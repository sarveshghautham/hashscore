package com.kheyos.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimerTask;

public class AnalysisTimer extends TimerTask {
	private HashMap<Integer, Integer> track;
	private String matchTag;
	private Cassandra cOp;
	
	public AnalysisTimer (HashScore hs, String matchTag) {
		this.track = hs.track;
		this.matchTag = matchTag;
		this.cOp = new Cassandra();
		cOp.connect("127.0.0.1");
	}
	
	public void run () {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar startCal  = Calendar.getInstance();
		Calendar endCal  = Calendar.getInstance();
		startCal.add(Calendar.MINUTE, -1);
		
		String startTime = dateFormat.format(startCal.getTime());
		String endTime = dateFormat.format(endCal.getTime());
		
		System.out.println("Timer running");
		
		if (!track.isEmpty()) {
			System.out.println("Track not empty: "+track);
			//Write to cassandra
			
			String query = "INSERT INTO ks_hashscore.hashscore "
					+ "(start_time, "
					+ "end_time, "
					+ "match_tag, "
					+ "fours_count, "
					+ "sixers_count, "
					+ "wickets_count) "
					+ "VALUES ("
					+ "'" + startTime + "' , '"
					+ endTime + "' , '"
					+ matchTag + "' ,"
					+ track.get(4) + ","
					+ track.get(6) + ","
					+ track.get(-1)
					+")";
			System.out.println(query);
			cOp.insertData(query);
			
		}
		else {
			System.out.println("Hash map is empty");
		}			
	
	}
}