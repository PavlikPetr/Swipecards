package com.topface.topface.ui.fragments.feed.feed_base

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.utils.Utils
import javax.inject.Inject

/**
 * Меню открывающееся при лонг тапу по фиду
 * Created by tiberal on 01.08.16.
 */
class ActionModeController(private val mMenuInflater: MenuInflater, private val mActionModeMenu: Int,
                           private val mActionModeEventsListener: OnActionModeEventsListener) : ActionMode.Callback {

    @Inject lateinit internal var context: Context
    private var mActionMode: ActionMode? = null
    private val mActionModeTitle: TextView by lazy {
        LayoutInflater.from(context).inflate(actionModeTitleRes, null) as TextView
    }
    @LayoutRes var actionModeTitleRes = R.layout.action_mode_text
    var isActionClicked: Boolean = false

    init {
        App.get().inject(this)
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        var result = true
        when (item?.itemId) {
            R.id.delete_list_item -> {
                isActionClicked = true
                mActionModeEventsListener.onDeleteFeedItems()
            }
            R.id.add_to_black_list -> {
                isActionClicked = true
                mActionModeEventsListener.onAddToBlackList()
            }
            else -> result = false
        }
        if (result) {
            mActionMode?.let {
                it.finish()
            }
        }
        return result
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let {
            mActionMode = it
            it.customView = mActionModeTitle
        }
        mActionModeEventsListener.onSetToolbarVisibility(false)
        menu?.clear()
        mMenuInflater.inflate(mActionModeMenu, menu)
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        mActionModeEventsListener.onSetToolbarVisibility(true)
        mActionModeEventsListener.onActionModeFinish()
        mActionMode = null
    }

    fun setSelectedCount(size: Int) {
        if (size > 0) {
            setTitle(size)
        } else {
            finish()
        }
    }

    private fun setTitle(size: Int) {
        mActionModeTitle.text = Utils.getQuantityString(R.plurals.selected, size, size)
    }

    fun finish() = mActionMode?.finish()


    fun isActionModeEnabled() = mActionMode != null

    fun finishIfEnabled() {
        if (isActionModeEnabled()) {
            finish()
        }
    }

    interface OnActionModeEventsListener {
        fun onDeleteFeedItems()
        fun onAddToBlackList()
        fun onSetToolbarVisibility(visibility: Boolean)
        fun onActionModeFinish()
    }

}

