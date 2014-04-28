package com.topface.statistics;

import java.util.List;

/**
 * Created by kirussell on 10.04.14.
 * Interface for building simple String-data from complex data objects
 */
public interface IHitDataBuilder {
    /**
     * Builds string-based data from list of previously built data items
     *
     * @param list list of built data items
     * @return string-based data
     */
    String build(List<String> list);

    /**
     * Builds string-based data from Hit
     *
     * @param hit hit object
     * @return data to dispatch
     */
    String build(Hit hit);
}
