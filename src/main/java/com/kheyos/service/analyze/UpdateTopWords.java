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
        this.wordCount = new HashMap<String, Integer>();
        this.sortedSet = new TreeSet<Map.Entry<String, Integer>>(new SetComparator());
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
    	PreparedStatement updateHistoryQuery = null;
    	PreparedStatement selectLastId = null;
    	PreparedStatement selectExistingQuery = null;
    	
    	String existingQuery = "SELECT count FROM word_tracker "
    			+ "WHERE word=? AND match_id=?";
    	
    	String query = "INSERT INTO word_tracker (match_id, word, count) "
    			+ "VALUES (?,?,?)";
    	
		String historyQuery = "INSERT INTO word_history (word_id, count, updated_time) "
				+ "VALUES (?,?,?)";
		
		String selectLastIdQuery = "SELECT word_id FROM word_tracker "
				+ "WHERE word=? AND match_id=?";
		
    	String updateWordQuery = "UPDATE "+db.getDbName()+".word_tracker "
    			+ "SET count=? "
    			+ "WHERE word=?";

		String updateWordHistoryQuery = "UPDATE "+db.getDbName()+".word_history "
    			+ "SET updated_time=? "
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
        	
        	if (rs1.next()) {
        		
        		dbCount = rs1.getInt("count");
        		
        		if (dbCount != count) {
        			        			
        			try {
    			   		con.setAutoCommit(false);
    			   	    
    			   		//Updating tracker table
    			   		updateQuery = con.prepareStatement(updateWordQuery);
    			   	    updateQuery.setInt(1, count);
    			   	    updateQuery.setString(2, word);
    			   	    updateQuery.executeUpdate();

                        selectLastId = con.prepareStatement(selectLastIdQuery);
                        selectLastId.setString(1, word);
                        selectLastId.setInt(2, matchId);
                        ResultSet rs = selectLastId.executeQuery();
                        int wordId=0;
                        if (rs.next()) {

                            //Insert the new count in history table
                            insertHistoryQuery = con.prepareStatement(historyQuery);
                            insertHistoryQuery.setInt(1, wordId);
                            insertHistoryQuery.setInt(2, count);
                            insertHistoryQuery.setTimestamp(3, timestamp);
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
                else {

                    //Get word id from tracker table
                    selectLastId = con.prepareStatement(selectLastIdQuery);
                    int lastId = 0;
                    selectLastId.setString(1, word);
                    selectLastId.setInt(2, matchId);
                    ResultSet rs = selectLastId.executeQuery();
                    int wordId=0;
                    if (rs.next()) {
                        wordId = rs.getInt("word_id");

                        try {
                            //Insert the new count in history table
                            updateHistoryQuery = con.prepareStatement(updateWordHistoryQuery);
                            updateHistoryQuery.setTimestamp(1, timestamp);
                            insertHistoryQuery.setInt(2, wordId);
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
                            if (updateHistoryQuery != null) {
                                updateHistoryQuery.close();
                            }

                            if (selectLastId != null) {
                                selectLastId.close();
                            }

                            con.setAutoCommit(true);
                        }
                    }
                }
        	}
            else {
        		
        	   //First time occurring. So insert.
				try {
			   		con.setAutoCommit(false);
			   	    insertQuery = con.prepareStatement(query);
			   	    
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
                        insertHistoryQuery = con.prepareStatement(historyQuery);
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
