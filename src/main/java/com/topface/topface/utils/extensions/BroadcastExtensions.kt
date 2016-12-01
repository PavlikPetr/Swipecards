package com.topface.topface.utils.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager


/**
 * Экстеншены для Broadcast
 * Created by siberia87 on 01.12.16.
 */

fun BroadcastReceiver.unregisterReceiver(c: Context) =
        LocalBroadcastManager.getInstance(c).unregisterReceiver(this)

fun Array<BroadcastReceiver>.unregisterReceiver(c: Context) =
        forEach { it.unregisterReceiver(c) }

fun BroadcastReceiver.registerReceiver(c: Context, intentFilter: IntentFilter) =
        LocalBroadcastManager.getInstance(c).registerReceiver(this, intentFilter)

