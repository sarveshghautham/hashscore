package com.kheyos.service.analyze;

import java.util.*;

/**
 * Created by sarvesh on 2/10/15.
 */
public class UpdateTopWords extends TimerTask{

    private String team1;
    private String team2;
    private String matchTag; 
    private TreeMap<String, Integer> wordCount;
    private Set<Map.Entry<String, Integer>> sortedSet;


    public UpdateTopWords(String t1, String t2, String matchTag) {
        this.team1 = t1;
        this.team2 = t2;
        this.matchTag = matchTag;    
        this.wordCount = new TreeMap<String, Integer>();
        this.sortedSet = new TreeSet<Map.Entry<String, Integer>>(new SetComparator());
     
    }

    public TreeMap<String, Integer> getWordCount() {
        return wordCount;
    }

    public void setWordCount(TreeMap<String, Integer> wordCount) {
        this.wordCount = wordCount;
    }

    public ArrayList<WordCount> getTopKWords(int K) {
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


    @Override
    public void run() {
    	
    	ArrayList<WordCount> topKWords = getTopKWords(5);
        for (WordCount w : topKWords) {
    		System.out.println("Word: "+w.getWord());
    		System.out.println("count: "+w.getCount());			
        }

        System.out.println();
    }
}
