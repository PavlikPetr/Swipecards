package com.topface.statistics;

import java.util.List;

/**
 * Created by kirussell on 14.04.2014.
 * Interface for dispatcher
 */
public interface IDataDispatcher {
    void dispatchData(List<String> data);

    IDataDispatcher setDataBuilder(IHitDataBuilder builder);

    IDataDispatcher setLogger(ILogger logger);
}
