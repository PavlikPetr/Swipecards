package com.topface.topface.utils.extensions

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.topface.topface.App


/**
 * Экстеншены для Broadcast
 * Created by siberia87 on 01.12.16.
 */

fun BroadcastReceiver.unregisterReceiver() =
        LocalBroadcastManager.getInstance(App.getContext()).unregisterReceiver(this)

fun Array<BroadcastReceiver>.unregisterReceiver() =
        forEach { it.unregisterReceiver() }

fun BroadcastReceiver.registerReceiver(intentFilter: IntentFilter) =
        LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(this, intentFilter)

