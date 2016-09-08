package com.topface.topface.utils

import android.text.TextUtils
import com.topface.framework.utils.Debug
import com.topface.topface.BuildConfig

/**
 * Утилит методы отвечающие за состояние приложения
 * Created by tiberal on 31.08.16.
 */
object AppUtils {

    fun isOldVersion(version: String): Boolean {
        val template = "\\."
        try {
            val curVersion = BuildConfig.VERSION_NAME
            if (!TextUtils.isEmpty(version) && !TextUtils.isEmpty(curVersion)) {
                val splittedVersion = TextUtils.split(version, template)
                val splittedCurVersion = TextUtils.split(curVersion, template)
                for (i in splittedVersion.indices) {
                    if (i < splittedCurVersion.size) {
                        val curVersionLong = java.lang.Long.parseLong(splittedCurVersion[i])
                        val maxVersionLong = java.lang.Long.parseLong(splittedVersion[i])
                        if (curVersionLong < maxVersionLong) {
                            return true
                        } else if (curVersionLong > maxVersionLong) {
                            return false
                        }
                    }
                }
                if (splittedCurVersion.size < splittedVersion.size) {
                    return true
                }
            }
        } catch (e: Exception) {
            Debug.error("Check Version Error: $version", e)
        }
        return false
    }

}