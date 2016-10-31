package com.topface.topface.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import permissions.dispatcher.PermissionUtils

/**
 * расширения для работы с пермишинами
 * Created by ppavlik on 26.10.16.
 */
class PermissionsExtensions {
    companion object {
        const val PERMISSION_GRANTED = 0L
        const val PERMISSION_DENIED = 1L
        const val PERMISSION_NEVER_ASK_AGAIN = 2L
    }

    @IntDef(PERMISSION_GRANTED, PERMISSION_DENIED, PERMISSION_NEVER_ASK_AGAIN)
    annotation class PermissionState
}

/**
 * Show app settings
 *
 * It can be used when the user denied *permissions* and you should direct him to the settings screen
 */
fun Context.showAppSettings() =
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package",
                        this.getPackageName(), null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

/**
 * Check *permissions* list
 *
 * It will check all *permissions* in list and return true if all of it are granted
 * @return true if all *permissions* in list are granted
 */
fun Context.isGrantedPermissions(vararg permissions: String) = this.isGrantedPermissions(permissions.sorted())

fun Context.isGrantedPermissions(permissions: List<String>) = permissions.find { PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this,
            it)
} == null

fun Activity.shouldShowRequestPermissionRationale(vararg permissions: String) =
        this.shouldShowRequestPermissionRationale(permissions.sorted())

fun Activity.shouldShowRequestPermissionRationale(permissions: List<String>) = if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.M)
    permissions.find {
        this.shouldShowRequestPermissionRationale(it)
    } != null
else
    false

@PermissionsExtensions.PermissionState
fun Activity.getPermissionStatus(vararg permissions: String)=
    if (PermissionUtils.getTargetSdkVersion(this) < Build.VERSION_CODES.M &&
            !PermissionUtils.hasSelfPermissions(this, permissions[0])) PermissionsExtensions.PERMISSION_DENIED
    else if (this.isGrantedPermissions(permissions.sorted())) PermissionsExtensions.PERMISSION_GRANTED
    else if (!this.shouldShowRequestPermissionRationale(permissions.sorted())) PermissionsExtensions.PERMISSION_NEVER_ASK_AGAIN
        else PermissionsExtensions.PERMISSION_DENIED