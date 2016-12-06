package com.topface.topface.ui.fragments.feed.dialogs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.utils.extensions.registerReceiver
import com.topface.topface.utils.extensions.unregisterReceiver
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * Класс реализующий поддержку/обработку пушей ADMIRATION, MUTUAL и DIALOGS
 * Created by siberia87 on 01.12.16.
 */
class FeedPushHandler(val mDelegate: IFeedPushHandlerDelegate, val mContext: Context) {

    private var mFeedDialogsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mDelegate.updateFeedDialogs()
        }
    }
    private var mFeedMutualReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mDelegate.updateFeedMutual()
        }
    }
    private var mFeedAdmirationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mDelegate.updateFeedAdmiration()
        }
    }
    private var mReadItemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val itemId = intent.getStringExtra(ChatFragment.INTENT_ITEM_ID)
            val userId = intent.getIntExtra(ChatFragment.INTENT_USER_ID, 0)
            if (userId == 0) {
                if (!TextUtils.isEmpty(itemId)) {
                    mDelegate.makeItemReadWithFeedId(itemId)
                }
            } else {
                mDelegate.makeItemReadUserId(userId, intent.getIntExtra(ChatFragment.LOADED_MESSAGES, 0))
            }
        }
    }

    init {
        mFeedDialogsReceiver.registerReceiver(mContext, IntentFilter(GCMUtils.GCM_DIALOGS_UPDATE))
        mFeedMutualReceiver.registerReceiver(mContext, IntentFilter(GCMUtils.GCM_MUTUAL_UPDATE))
        mFeedAdmirationReceiver.registerReceiver(mContext, IntentFilter(GCMUtils.GCM_ADMIRATION_UPDATE))
        mReadItemReceiver.registerReceiver(mContext, IntentFilter(ChatFragment.MAKE_ITEM_READ).apply {
            addAction(ChatFragment.MAKE_ITEM_READ_BY_UID)
        })
    }

    fun release() =
            arrayOf(mFeedDialogsReceiver, mFeedMutualReceiver, mFeedAdmirationReceiver, mReadItemReceiver)
                    .unregisterReceiver(mContext)


}