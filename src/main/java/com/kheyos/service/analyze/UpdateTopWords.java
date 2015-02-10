package com.kheyos.service.analyze;

import java.util.*;

/**
 * Created by sarvesh on 2/10/15.
 */
public class UpdateTopWords extends TimerTask{

    private TreeMap<String, Integer> wordCount;
    private Set<Map.Entry<String, Integer>> sortedSet;
    private HashScore hs;

    public UpdateTopWords(HashScore hs) {
        this.hs = hs;
        this.wordCount = hs.getWordCount();
        this.sortedSet = new TreeSet<Map.Entry<String, Integer>>(new SetComparator());
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
        ArrayList<WordCount> topKWords = getTopKWords(10);
        hs.setTopKWords(topKWords);
    }
}
