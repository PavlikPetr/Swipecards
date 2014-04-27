package com.topface.statistics;

import java.util.List;

/**
 * Created by kirussell on 10.04.14.
 */
public interface IHitDataBuilder {
    String build(List<String> list);

    String build(Hit hit);
}
