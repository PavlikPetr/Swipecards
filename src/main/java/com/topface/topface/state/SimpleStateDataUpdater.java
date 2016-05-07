package com.topface.topface.state;

import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;

public abstract class SimpleStateDataUpdater implements IStateDataUpdater {
    public abstract void onOptionsUpdate(Options options);

    public abstract void onProfileUpdate(Profile profile);

    @Override
    public Options getOptions() {
        return null;
    }

    @Override
    public Profile getProfile() {
        return null;
    }
}
