package com.topface.statistics.android;

import java.util.HashMap;

/**
 * Created by kirussell on 22.04.2014.
 * Collect slices with objects of this class
 */
public class Slices extends HashMap<String, String> {

    public Slices putSlice(String key, String value) {
        super.put(key, value);
        return this;
    }
}
