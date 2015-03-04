package com.kheyos.service.analyze;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sarvesh on 2/9/15.
 */

public class POSTagger {

    private static final String f_model = "models/english-left3words-distsim.tagger";
    private static MaxentTagger tagger;
    private static final HashMap<String, Integer> tags = new HashMap<>();
    private POSTagger() {
    }

    private static class POSTaggerSingleton {
        private static final POSTagger taggerObj = new POSTagger();
    }

    public static POSTagger getTaggerInstance(){

        if (tagger == null) {
            try {
				loadTagger();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return POSTaggerSingleton.taggerObj;
    }

    private static void loadTagger() throws IOException {
    	
    	tagger = new MaxentTagger(f_model);
    	if (tagger == null) {
    		throw new RuntimeException("tagger not loaded");
    	}
        
        tags.put("JJ", 0);
        tags.put("JJR", 0);
        tags.put("JJS", 0);
        tags.put("NN", 0);
        tags.put("NNS", 0);
        tags.put("NNP", 0);
        tags.put("NNPS", 0);
        tags.put("RB", 0);
        tags.put("RBR", 0);
        tags.put("RBS", 0);
        tags.put("WRB", 0);

    }

    public ArrayList<String> getWords(String tweet) {

        String tag = tagger.tagString(tweet);
        ArrayList<String> words = new ArrayList<>();

        String[] tweetSplit = tag.split(" ");
        for (int i=0; i<tweetSplit.length;i++) {
            String[] wordSplit = tweetSplit[i].split("_");
            
            if (wordSplit[1] != "") {
	            PartOfSpeech p = PartOfSpeech.get(wordSplit[1]);
	            if (p != null) {
	                if (tags.containsKey(p.getTag())) {
	                    words.add(wordSplit[0]);
	                }
	            }
            }
        }

        return words;
    }

}
