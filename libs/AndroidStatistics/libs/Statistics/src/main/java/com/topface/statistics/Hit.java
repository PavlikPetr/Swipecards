package com.topface.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirussell on 04.04.14.
 * Class to form hit data.
 * Basic implementation contains fields' names and types corresponding to server requirements
 * (https://tasks.verumnets.ru/projects/topface/wiki/Мобильная_клиентская_статистика)
 */
public class Hit {

    private String n;
    private Integer c;
    private Map<String, String> x;

    public Hit(String name, Integer count, Map<String, String> slices) {
        n = name;
        c = count;
        x = slices == null ? new HashMap<String, String>() : new HashMap<>(slices);
    }

    public Hit addSlice(String key, String value) {
        x.put(key, value);
        return this;
    }

    public Hit addAllSlice(Map<String, String> slices) {
        x.putAll(slices);
        return this;
    }
}
