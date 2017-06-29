package com.topface.topface.utils.hockey_app

import com.topface.topface.App
import net.hockeyapp.android.CrashManagerListener

/**
 * Created by ppavlik on 10.10.16.
 * Settings of crash manager of HA for our project
 */
class HockeyAppCrashManager : CrashManagerListener() {

    override fun shouldAutoUploadCrashes() = true

    override fun getUserID(): String {
        return App.get().profile.uid.toString()
    }
}