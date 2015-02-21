package com.kheyos.service.analyze;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    
    public UpdateTopWords(String t1, String t2, int match_id, HashMap<String, Integer> avoidKeywords) {
        this.team1 = t1;
        this.team2 = t2;
        this.matchId = match_id;    
        this.wordCount = new HashMap<>();
        this.sortedSet = new TreeSet<>(new SetComparator());
        this.avoidKeywords = avoidKeywords;
     
    }

    public HashMap<String, Integer> getWordCount() {
        return wordCount;
    }

    public void setWordCount(HashMap<String, Integer> wordCount) {
        this.wordCount = wordCount;
    }

    public ArrayList<WordCount> getTopKWords(int K) {
    	sortedSet = new TreeSet<Map.Entry<String, Integer>>(new SetComparator());
        sortedSet.addAll(wordCount.entrySet());
        ArrayList<WordCount> wordCount = new ArrayList<WordCount>();
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
//                	System.out.println(eachWord);   
                    int count = wordCount.get(eachWord);
                    count++;
                    wordCount.replace(eachWord, count);
                } else {
                    wordCount.put(eachWord, 1);
                }
        	}
        }
    }
    
    public synchronized void insertWordIntoDb() throws SQLException {
    	
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

    	for (Map.Entry<String, Integer> mapValues : wordCount.entrySet()) {

        	String word = mapValues.getKey();
        	int count = mapValues.getValue();
        	
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

                        System.out.println("Inserting into history table");
                        System.out.println("Word ID: " + wordId);
                        System.out.println("Word: "+word);
                        System.out.println("DB count: "+dbCount);
                        System.out.println("Count: " + count);
                        System.out.println("Timestamp: " + timestamp);

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

//                        System.out.println("Inserting first time into history table");
//                        System.out.println("Word ID: "+lastId);
//                        System.out.println("Word: "+word);
//                        System.out.println("Count: "+count);
//                        System.out.println("Timestamp: "+timestamp);

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
    
    @Override
    public void run() {
    	
    	try {
			insertWordIntoDb();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    }
}
