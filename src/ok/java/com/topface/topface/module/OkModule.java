package com.topface.topface.module;

import com.topface.topface.modules.TopfaceModule;
import com.topface.topface.ui.OkApplication;

import dagger.Module;

@Module(includes = TopfaceModule.class,
        injects = OkApplication.class)
public class OkModule {

    public OkModule() {

    }
}