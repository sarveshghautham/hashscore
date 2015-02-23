package com.kheyos.service.analyze;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.sql.PreparedStatement;

/**
 * Created by sarvesh on 2/10/15.
 */
public class UpdateTopWords extends TimerTask{

    private String team1;
    private String team2;
    private int matchId; 
    private HashMap<String, Integer> wordCount;
    private Set<Map.Entry<String, Integer>> sortedSet;
    private HashMap<String, Integer> avoidKeywords;
    private DbConnection db;
    private double prevBall = 0.0;
    private final String USER_AGENT = "Mozilla/5.0";
    private String yMatchId;
    private String url = "";
    private int k = 0;
    private boolean endOfInnings = false;
    
    public UpdateTopWords(String t1, String t2, int match_id, HashMap<String, Integer> avoidKeywords, String yMatchId, int k) {
        this.team1 = t1;
        this.team2 = t2;
        this.matchId = match_id;    
        this.wordCount = new HashMap<>();
        this.sortedSet = new TreeSet<>(new SetComparator());
        this.avoidKeywords = avoidKeywords;
        this.yMatchId = yMatchId;
        this.url = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20cricket.scorecard%20where%20match_id%3D%20"+this.yMatchId+"&format=json&diagnostics=true&env=store%3A%2F%2F0TxIGQMQbObzvU4Apia0V0&callback=";
        this.k = k;
    }

    public HashMap<String, Integer> getWordCount() {
        return wordCount;
    }

    public void setWordCount(HashMap<String, Integer> wordCount) {
        this.wordCount = wordCount;
    }

    public ArrayList<WordCount> getTopKWords(int K) {
    	sortedSet = new TreeSet<>(new SetComparator());
        synchronized (wordCount) {
            sortedSet.addAll(wordCount.entrySet());
        }
        ArrayList<WordCount> wordCount = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> words : sortedSet) {
            wordCount.add(new WordCount(words.getKey(), words.getValue()));
            count++;
            if (count == K) {
                break;
            }
        }

        return wordCount;
    }

    public synchronized void updateWordsInMap(ArrayList<String> words) {

        for (String eachWord : words) {
        	eachWord = eachWord.toLowerCase();
        	if (!avoidKeywords.containsKey(eachWord) &&
        			!eachWord.startsWith("@") &&
        			!eachWord.startsWith("#")) {
                if (wordCount.containsKey(eachWord)) {
                    int count = wordCount.get(eachWord);
                    count++;
                    wordCount.replace(eachWord, count);
                } else {
                    wordCount.put(eachWord, 1);
                }
        	}
        }
    }
    
    public void insertWordIntoDb(ArrayList<WordCount> topKWords) throws SQLException {
    	
    	db = DbConnection.getInstance();
    	Connection con = db.getConnection();
    	
    	PreparedStatement insertQuery = null;
    	PreparedStatement insertHistoryQuery = null;
    	PreparedStatement updateQuery = null;
    	PreparedStatement selectLastId = null;
    	PreparedStatement selectExistingQuery = null;
    	
    	String existingQuery = "SELECT word_id, count FROM word_tracker "
    			+ "WHERE word=? AND match_id=?";
    	
    	String insertIntoWordTrackerQuery = "INSERT INTO word_tracker (match_id, word, count) "
    			+ "VALUES (?,?,?)";
    	
		String insertIntoWordHistoryQuery = "INSERT INTO word_history (word_id, count, updated_time) "
				+ "VALUES (?,?,?)";
		
		String selectLastIdQuery = "SELECT word_id FROM word_tracker "
				+ "WHERE word=? AND match_id=?";
		
    	String updateWordQuery = "UPDATE "+db.getDbName()+".word_tracker "
    			+ "SET count=? "
    			+ "WHERE word_id=?";

    	Date date = new Date();
	   	Timestamp timestamp = new Timestamp(date.getTime());
	   	
    	selectExistingQuery = con.prepareStatement(existingQuery);
    	int dbCount = 0;

        for (WordCount w : topKWords) {
        	String word = w.getWord();
        	int count = w.getCount();
        	
        	selectExistingQuery.setString(1, word);
        	selectExistingQuery.setInt(2, matchId);
        	ResultSet rs1 = selectExistingQuery.executeQuery();

            //If the word exists
        	if (rs1.next()) {
        		
        		dbCount = rs1.getInt("count");
                int wordId = rs1.getInt("word_id");

                //If the counts don't match update it
        		if (dbCount < count) {
        			        			
        			try {
    			   		con.setAutoCommit(false);
    			   	    
    			   		//Updating tracker table
    			   		updateQuery = con.prepareStatement(updateWordQuery);
    			   	    updateQuery.setInt(1, count);
    			   	    updateQuery.setInt(2, wordId);
    			   	    updateQuery.executeUpdate();

                        //Insert the new count in history table
                        insertHistoryQuery = con.prepareStatement(insertIntoWordHistoryQuery);
                        insertHistoryQuery.setInt(1, wordId);
                        insertHistoryQuery.setInt(2, count);
                        insertHistoryQuery.setTimestamp(3, timestamp);
                        insertHistoryQuery.executeUpdate();
                        con.commit();

    			   	    
    			   	} catch (SQLException e ) {
    		   	        e.printStackTrace();
    		   	        if (con != null) {
    		   	            try {
    		   	                System.err.print("Transaction is being rolled back");
    		   	                con.rollback();
    		   	            } catch(SQLException excep) {
    		   	                e.printStackTrace();
    		   	            }
    		   	        }
    		   	    } finally {
    		   	        if (updateQuery != null) {
    		   	            updateQuery.close();
    		   	        }

                        if (selectLastId != null) {
                            selectLastId.close();
                        }

                        if (insertHistoryQuery != null) {
                            insertHistoryQuery.close();
                        }

    		   	        con.setAutoCommit(true);
    		   	    }
        		}
        	}
            else {
        		
        	   //First time occurring. So insert.
				try {
			   		con.setAutoCommit(false);
			   	    insertQuery = con.prepareStatement(insertIntoWordTrackerQuery);
			   	    
			   	    //Tracker table
			   	    insertQuery.setInt(1, matchId);
			   	    insertQuery.setString(2, word);
			   	    insertQuery.setInt(3, count);
			   	    insertQuery.executeUpdate();
			   	    
			   	    selectLastId = con.prepareStatement(selectLastIdQuery);
				   	int lastId = 0;
				   	selectLastId.setString(1, word);
				   	selectLastId.setInt(2, matchId);
				   	ResultSet rs = selectLastId.executeQuery();
				   	if (rs.next()) {
                        lastId = rs.getInt("word_id");

                        //History table
                        insertHistoryQuery = con.prepareStatement(insertIntoWordHistoryQuery);
                        insertHistoryQuery.setInt(1, lastId);
                        insertHistoryQuery.setInt(2, count);
                        insertHistoryQuery.setTimestamp(3, timestamp);
                        insertHistoryQuery.executeUpdate();
                        con.commit();
                    }
			   	} catch (SQLException e ) {
		   	        e.printStackTrace();
		   	        if (con != null) {
		   	            try {
		   	                System.err.print("Transaction is being rolled back");
		   	                con.rollback();
		   	            } catch(SQLException excep) {
		   	                e.printStackTrace();
		   	            }
		   	        }
		   	    } finally {
		   	        if (insertQuery != null) {
		   	            insertQuery.close();
		   	        }

                    if (selectLastId != null) {
                        selectLastId.close();
                    }

                    if (insertHistoryQuery != null) {
                        insertHistoryQuery.close();
                    }
		   	        
		   	        con.setAutoCommit(true);
		   	    }
			}
    	}

        if (selectExistingQuery != null) {
            selectExistingQuery.close();
        }
    }

    public double getCurrentBall() throws IOException {

        URL obj = null;
        BufferedReader in = null;
        String inputLine;
        double overs = 0.0;

        try {
            obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            overs = JSONData(response.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return overs;
    }

    public double JSONData(String msg) throws IOException {
        byte[] jsonData = msg.getBytes();

        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        //read JSON like DOM Parser
        JsonNode rootNode = objectMapper.readTree(jsonData);
        JsonNode oversNode = rootNode.path("query").path("results").path("Scorecard").path("past_ings").path(0).path("s").path("a").path("o");
        Double overs = Double.parseDouble(oversNode.asText());

        return overs;
    }


    public boolean checkChangeInBall() {

        double currentBall = 0;
        try {

            currentBall = getCurrentBall();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Case 1: both prevBall and currentBall are in 0.0. No change. So, return false.
        // Case 2: if prevBall and currentBall don't match, compare them. If prevBall is
        // less than currentBall, then update the prevBall.

//        System.out.println("Prev Ball: "+prevBall);
//        System.out.println("Current Ball: "+currentBall);

        if (prevBall < currentBall) {
            prevBall = currentBall;
            return true;
        }
        else if (!endOfInnings && currentBall == 0.0) {
            endOfInnings = true;
            prevBall = 0.0;
            return false;
        }

        return false;
    }

    @Override
    public void run() {
    	
    	try {

            // readScore
            // if current ball is different from the read score
            // trigger insertWordIntoDb

            if (checkChangeInBall()) {
                insertWordIntoDb(getTopKWords(k));
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    }
}
