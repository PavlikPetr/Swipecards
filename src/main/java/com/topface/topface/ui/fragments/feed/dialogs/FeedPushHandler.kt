package com.topface.topface.ui.fragments.feed.dialogs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import com.topface.topface.App
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.FeedFragment
import com.topface.topface.utils.actionbar.OverflowMenu
import com.topface.topface.utils.extensions.registerReceiver
import com.topface.topface.utils.extensions.unregisterReceiver
import com.topface.topface.utils.gcmutils.GCMUtils

/**
 * Класс реализующий поддержку/обработку пушей ADMIRATION, MUTUAL и DIALOGS
 * Created by siberia87 on 01.12.16.
 */
class FeedPushHandler(private var mListener: IFeedPushHandlerListener?) {

    private val mContext = App.getContext()

    private var mFeedDialogsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mListener?.updateFeedDialogs()
        }
    }
    private var mBlackListAddReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.hasExtra(OverflowMenu.USER_ID_FOR_REMOVE)) {
                val userId = intent.getIntExtra(OverflowMenu.USER_ID_FOR_REMOVE, -1)
                mListener?.userAddToBlackList(userId)
            }
        }
    }
    private var mAddToBookmarksReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE) as BlackListAndBookmarkHandler.ActionTypes
            if (type == BlackListAndBookmarkHandler.ActionTypes.BOOKMARK &&
                    intent.hasExtra(BlackListAndBookmarkHandler.FEED_ID)) {
                val userId = intent.getIntExtra(BlackListAndBookmarkHandler.FEED_ID, -1)
                mListener?.userAddToBookmarks(userId)
            }
        }
    }
    private var mFeedMutualReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mListener?.updateFeedMutual()
        }
    }
    private var mFeedAdmirationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mListener?.updateFeedAdmiration()
        }
    }
    private var mReadItemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val itemId = intent.getStringExtra(ChatFragment.INTENT_ITEM_ID)
            val userId = intent.getIntExtra(ChatFragment.INTENT_USER_ID, 0)
            if (userId == 0) {
                if (!TextUtils.isEmpty(itemId)) {
                    mListener?.makeItemReadWithFeedId(itemId)
                }
            } else {
                mListener?.makeItemReadUserId(userId, intent.getIntExtra(ChatFragment.LOADED_MESSAGES, 0))
            }
        }
    }

    init {
        mBlackListAddReceiver.registerReceiver(IntentFilter(FeedFragment.REFRESH_DIALOGS))
        mAddToBookmarksReceiver.registerReceiver(IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY))
        mFeedDialogsReceiver.registerReceiver(IntentFilter(GCMUtils.GCM_DIALOGS_UPDATE))
        mFeedMutualReceiver.registerReceiver(IntentFilter(GCMUtils.GCM_MUTUAL_UPDATE))
        mFeedAdmirationReceiver.registerReceiver(IntentFilter(GCMUtils.GCM_ADMIRATION_UPDATE))
        mReadItemReceiver.registerReceiver(IntentFilter(ChatFragment.MAKE_ITEM_READ).apply {
            addAction(ChatFragment.MAKE_ITEM_READ_BY_UID)
        })
    }

    fun release() {
        arrayOf(mFeedDialogsReceiver, mFeedMutualReceiver, mFeedAdmirationReceiver,
                mReadItemReceiver, mBlackListAddReceiver, mAddToBookmarksReceiver)
                .unregisterReceiver()
        mListener = null
    }
}