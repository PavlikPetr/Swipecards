package com.topface.topface.utils.popups

import android.support.v4.app.FragmentActivity
import com.topface.topface.utils.controllers.startactions.IStartAction

interface IStartActionFactory {
    fun construct(actionHolder: PopupSequence.ActionHolder, activity: FragmentActivity, from: String): IStartAction?
}