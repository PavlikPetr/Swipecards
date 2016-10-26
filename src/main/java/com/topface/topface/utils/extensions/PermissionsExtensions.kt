package com.topface.topface.utils.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

/**
 * расширения для работы с пермишинами
 * Created by ppavlik on 26.10.16.
 */

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
fun Context.isGrantedPermissions(vararg permissions: String) = permissions.find {
    PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this,
            it)
} == null

// временное решение для фотохелпера, т.к. permissionsDispatcher работает только с фрагментами и активити
fun Activity.showPermissionDialog(requestId: Int, vararg permissions: String) =
        ActivityCompat.requestPermissions(this,
                permissions,
                requestId)

// временное решение для фотохелпера, т.к. permissionsDispatcher работает только с фрагментами и активити
// показ диалога на запрогс пермишина с возвратом в колбек фрагмента
fun Fragment.showPermissionDialog(requestId: Int, vararg permissions: String) =
        this.requestPermissions(permissions,
                requestId)

// временное решение для фотохелпера, т.к. permissionsDispatcher работает только с фрагментами и активити
// прост небольшой врапер
fun Activity.isNeedShowPermissionRationale(permissions: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(this,
                permissions)
