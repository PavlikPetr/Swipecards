package com.topface.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirussell on 04.04.14.
 */
public class Hit implements IStatisticsData {

    private String n;
    private Integer c;
    private Map<String, String> x;

    public Hit(String name, Integer count, Map<String, String> slices) {
        n = name;
        c = count;
        x = slices == null ? new HashMap<String, String>() : new HashMap<>(slices);
    }

    @Override
    public Hit addSlice(String key, String value) {
        x.put(key, value);
        return this;
    }

    @Override
    public IStatisticsData addAllSlice(Map<String, String> slices) {
        x.putAll(slices);
        return this;
    }
}
