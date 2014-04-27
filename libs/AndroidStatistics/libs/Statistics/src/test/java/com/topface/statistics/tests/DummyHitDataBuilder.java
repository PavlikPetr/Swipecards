package com.topface.statistics.tests;

import com.topface.statistics.Hit;
import com.topface.statistics.IHitDataBuilder;

import java.util.List;

/**
 * Created by kirussell on 22.04.2014.
 */
class DummyHitDataBuilder implements IHitDataBuilder {

    @Override
    public String build(List<String> list) {
        return null;
    }

    @Override
    public String build(Hit hit) {
        return null;
    }
}
