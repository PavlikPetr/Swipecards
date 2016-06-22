package com.topface.topface.state;

import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;

import org.jetbrains.annotations.Nullable;

public abstract class SimpleStateDataUpdater implements IStateDataUpdater {
    public abstract void onOptionsUpdate(Options options);

    public abstract void onProfileUpdate(Profile profile);

    @Override
    @Nullable
    public Options getOptions() {
        return null;
    }

    @Override
    @Nullable
    public Profile getProfile() {
        return null;
    }
}
