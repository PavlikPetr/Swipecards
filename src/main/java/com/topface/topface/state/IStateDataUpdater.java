package com.topface.topface.state;


import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;

/**
 * Created by onikitin on 31.08.15.
 */
public interface IStateDataUpdater {


    void onOptionsUpdate(Options options);

    Options getOptions();

    void onProfileUpdate(Profile profile);

    Profile getProfile();

}
