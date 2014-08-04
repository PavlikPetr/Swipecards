package com.topface.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirussell on 04.04.14.
 * Class to form hit data.
 * Basic implementation contains fields' names and types corresponding to server requirements
 * https://tasks.verumnets.ru/projects/topface/wiki/%D0%9C%D0%BE%D0%B1%D0%B8%D0%BB%D1%8C%D0%BD%D0%B0%D1%8F_%D0%BA%D0%BB%D0%B8%D0%B5%D0%BD%D1%82%D1%81%D0%BA%D0%B0%D1%8F_%D1%81%D1%82%D0%B0%D1%82%D0%B8%D1%81%D1%82%D0%B8%D0%BA%D0%B0
 */
@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
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
