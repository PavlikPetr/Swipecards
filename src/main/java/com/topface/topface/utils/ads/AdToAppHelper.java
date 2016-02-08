package com.topface.topface.utils.ads;

import android.app.Activity;

import com.topface.topface.App;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by ppetr on 08.02.16.
 * Чтобы не забыть про инициализацию sdk, для которой нужна Activity
 */
public class AdToAppHelper {

    @Inject
    AdToAppController mController;

    public AdToAppHelper(@NotNull Activity activity) {
        App.from(activity).inject(this);
        mController.initSdk(activity);
    }

    public AdToAppController getController() {
        return mController;
    }
}
