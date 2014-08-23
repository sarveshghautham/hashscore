package com.kheyos.service;

import java.io.IOException;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class ReadScores extends TimerTask {

	private String scoreId;
	private String wicketId;
	private String prevScore;
	private AnalysisTimer at;
	private HashScore hs;
	private String matchTag;
	
	public ReadScores (HashScore hs, String match_tag, String score_id, String wicket_id) {
		this.scoreId = score_id;
		this.wicketId = wicket_id;
		this.hs = hs;
		this.matchTag = match_tag;
		at = new AnalysisTimer(this.hs, this.matchTag);
	}
	
	public void run () {
		Document doc = null;
		try {
			doc = Jsoup.connect("http://cricruns.com/widgetbase").get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String score = doc.select(scoreId).text();
		if (prevScore == null) {
			prevScore = score;
		}
		else {
			if (prevScore.equals(score)) {
				//TODO: Do nothing. Decide later.
			}
			else {
				//TODO: Change from score to ball.
				prevScore = score;
				at.analyseFeeds();
			}
			
		}
		
		String wickets = doc.select(wicketId).text();
		System.out.println("Score: "+score+"/"+wickets);
	}

}
