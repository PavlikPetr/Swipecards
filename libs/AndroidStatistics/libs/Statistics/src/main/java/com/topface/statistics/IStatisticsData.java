package com.topface.statistics;

import java.util.Map;

/**
 * Created by kirussell on 11.04.2014.
 *
 */
public interface IStatisticsData {
    IStatisticsData addSlice(String key, String value);
    IStatisticsData addAllSlice(Map<String, String> slices);
}
