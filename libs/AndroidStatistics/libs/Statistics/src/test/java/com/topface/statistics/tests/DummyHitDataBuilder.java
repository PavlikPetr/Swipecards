package com.topface.statistics.tests;

import com.google.gson.Gson;
import com.topface.statistics.IHitDataBuilder;

/**
* Created by kirussell on 22.04.2014.
*/
class DummyHitDataBuilder implements IHitDataBuilder {

    @Override
    public String build(Object object) {
        return new Gson().toJson(object);
    }
}
