package com.topface.topface.ui.fragments.feed.dialogs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.utils.extensions.registerReceivers
import com.topface.topface.utils.extensions.unregisterReceiver
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * Класс реализующий поддержку/обработку пушей ADMIRATION, MUTUAL и DIALOGS
 * Created by siberia87 on 01.12.16.
 */
class FeedPushHandler(val mDelegate: IFeedPushHandlerDelegate, val mContext: Context) {

    private lateinit var mFeedDialogsReceiver: BroadcastReceiver
    private lateinit var mFeedMutualReceiver: BroadcastReceiver
    private lateinit var mFeedAdmirationReceiver: BroadcastReceiver
    private lateinit var mReadItemReceiver: BroadcastReceiver

    init {
        createAndRegisterBroadcasts()
    }

    private fun createAndRegisterBroadcasts() {
        mFeedDialogsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mDelegate.updateFeedDialogs()
            }
        }
        mFeedMutualReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mDelegate.updateFeedMutual()
            }
        }
        mFeedAdmirationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mDelegate.updateFeedAdmiration()
            }
        }
        mReadItemReceiver = object : BroadcastReceiver() {
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
        with(mutableMapOf<String, BroadcastReceiver>()) {
            put(GCMUtils.GCM_DIALOGS_UPDATE, mFeedDialogsReceiver)
            put(GCMUtils.GCM_MUTUAL_UPDATE, mFeedMutualReceiver)
            put(GCMUtils.GCM_ADMIRATION_UPDATE, mFeedAdmirationReceiver)
            this@with
        }.apply {
            registerReceivers(mContext)
        }
    }

    fun release() {
        arrayOf(mFeedDialogsReceiver, mFeedMutualReceiver, mFeedAdmirationReceiver, mReadItemReceiver)
                .unregisterReceiver(mContext)
    }

}