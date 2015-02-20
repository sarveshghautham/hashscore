package com.kheyos.service.analyze;

import java.util.Comparator;
import java.util.Map;

/**
 * Created by sarvesh on 2/10/15.
 */
class SetComparator implements Comparator<Map.Entry<String, Integer>> {

    public int compare(Map.Entry<String, Integer> e1,
            Map.Entry<String, Integer> e2) {
        return -(e1.getValue().compareTo(e2.getValue()));
    }
}
