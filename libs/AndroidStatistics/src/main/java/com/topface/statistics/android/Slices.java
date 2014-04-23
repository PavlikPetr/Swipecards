package com.topface.statistics.android;

import java.util.HashMap;

/**
 * Created by kirussell on 22.04.2014.
 */
public class Slices extends HashMap<String, String> {

    public Slices addSlice(String key, String value) {
        super.put(key, value);
        return this;
    }
}
