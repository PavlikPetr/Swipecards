package com.topface.topface.ui.fragments

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import com.topface.topface.R
import com.topface.topface.utils.hockeyApp.HockeyAppCrashManager
import net.hockeyapp.android.CrashManager

/**
 * Created by ppavlik on 10.10.16.
 * Base activity for a whole project, cause it send a crash reports
 */

abstract class CrashReportActivity : AppCompatActivity() {

	override fun onResume() {
		super.onResume()
		checkForCrashes();
	}

	private fun checkForCrashes() {
		CrashManager.register(this@CrashReportActivity, getAppId(), HockeyAppCrashManager())
	}

	private fun getAppId(): String {
		val activityInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
		// если менять AppId в build.gradle, то поменять надо и здесь.
		// если по каким-то причинам не достали из манифеста метадату, то краш уйдет в продовское приложение HA
		return activityInfo.metaData?.getString(getString(R.string.hockey_app_meta_data_name)) ?: "817b00ae731c4a663272b4c4e53e4b61"
	}
}