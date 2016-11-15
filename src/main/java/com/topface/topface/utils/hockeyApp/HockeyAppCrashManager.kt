package com.topface.topface.utils.hockeyApp

import net.hockeyapp.android.CrashManagerListener

/**
 * Created by ppavlik on 10.10.16.
 * Settings of crash manager of HA for our project
 */
class HockeyAppCrashManager : CrashManagerListener() {
	override fun shouldAutoUploadCrashes() = true
}