package com.topface.topface.ui.fragments

import com.ironsource.mediationsdk.IronSource
import com.topface.topface.ui.CrashReportActivity

/**
 * Created by ppavlik on 30.05.17.
 * Активити для прокидывания lifecycle колбеков в IronSrc, не забыть выпилить активити при удалении sdk
 */
abstract class IronSrcIntegrationActivity : CrashReportActivity() {
    override fun onPause() {
        super.onPause()
        IronSource.onPause(this);
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this);
    }
}