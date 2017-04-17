package com.topface.topface.utils.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import com.topface.topface.App
import com.topface.topface.ui.dialogs.PermissionAlertDialogFactory
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
 * @param permissions array of permissions
 * @return true if all *permissions* in list are granted
 */
fun Context.isGrantedPermissions(vararg permissions: String) = this.isGrantedPermissions(permissions.sorted())

/**
 * Check *permissions* list
 *
 * It will check all *permissions* in list and return true if all of it are granted
 * @param permissions list of permissions
 * @return true if all *permissions* in list are granted
 */
fun Context.isGrantedPermissions(permissions: List<String>) =
        permissions.find {
            PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, it)
        } == null

/**
 * Check *permissions* list
 *
 * It will check all *permissions* in list and return true if at least one are need
 * ShowRequestPermissionRationale
 * @param permissions array of permissions
 * @return true if at least one are need ShowRequestPermissionRationale
 */
fun Activity.shouldShowRequestPermissionRationale(vararg permissions: String) =
        this.shouldShowRequestPermissionRationale(permissions.sorted())

/**
 * Check *permissions* list
 *
 * It will check all *permissions* in list and return true if at least one are need
 * ShowRequestPermissionRationale
 * @param permissions list of permissions
 * @return true if at least one are need ShowRequestPermissionRationale
 */
fun Activity.shouldShowRequestPermissionRationale(permissions: List<String>) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            permissions.find {
                this.shouldShowRequestPermissionRationale(it)
            } != null
        else
            false

/**
 * Get permission current state
 *
 * @param permissions array of permissions
 * @return current state of permission group
 */
@PermissionsExtensions.PermissionState
fun Activity.getPermissionStatus(vararg permissions: String) =
        if (PermissionUtils.getTargetSdkVersion(this) < Build.VERSION_CODES.M &&
                permissions.find { !PermissionUtils.hasSelfPermissions(this, it) } != null) PermissionsExtensions.PERMISSION_DENIED
        else if (this.isGrantedPermissions(permissions.sorted())) PermissionsExtensions.PERMISSION_GRANTED
        else if (!this.shouldShowRequestPermissionRationale(permissions.sorted()) &&
                permissions.find { App.getAppConfig().permissionStateMap.containsKey(it) } != null) PermissionsExtensions.PERMISSION_NEVER_ASK_AGAIN
        else PermissionsExtensions.PERMISSION_DENIED

fun Activity.isPermissinsBlockedForever(vararg permissions: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.find {
                this.getPermissionStatus(it) == PermissionsExtensions.PERMISSION_NEVER_ASK_AGAIN
            } != null
        } else false

fun Activity.askUnlockStoragePermissionIfNeed() {
    this?.let {
        if (it.isPermissinsBlockedForever(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionAlertDialogFactory().constructNeverAskAgain(it)
        }
    }
}