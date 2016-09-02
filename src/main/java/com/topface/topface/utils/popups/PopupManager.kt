package com.topface.topface.utils.popups

import android.support.v4.app.FragmentActivity
import com.topface.framework.utils.Debug
import com.topface.topface.utils.controllers.startactions.IStartAction
import org.jetbrains.anko.doAsync
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Новая инкарнация менеджера попапов
 * Created by tiberal on 25.08.16.
 */
object PopupManager {

    val AC_PRIORITY_HIGH = 3
    val AC_PRIORITY_NORMAL = 2
    val AC_PRIORITY_LOW = 1

    private val mSequences = ConcurrentHashMap<String, SequenceHolder>()
    private var mActionFactory: IStartActionFactory? = null
    private var mActivity: FragmentActivity? = null

    fun init(activity: FragmentActivity) {
        mActivity = activity
        mActionFactory = StartActionFactory()
    }

    fun release() {
        mActivity = null
        mActionFactory = null
    }

    /**
     * синглтон и нужно инитить его каждый раз как меняется контекст
     */

    @Synchronized fun registerSequence(name: String, popupSequence: PopupSequence) {
        if (popupSequence.isEmpty()) {
            Debug.log("PopupMANAGER Sequence is empty")
            return
        }
        if (!mSequences.containsKey(name)) {
            mSequences.put(name, SequenceHolder(popupSequence))
        } else {
            Debug.log("PopupMANAGER Sequence for this class already exists")
        }
    }

    @Synchronized fun informManager(name: String) {
        if (mSequences.containsKey(name)) {
            mSequences[name]?.let {
                if (it.hasMoarActions()) {
                    runAction(getApplicableAction(it, name), name)
                } else {
                    it.isSequenceComplete = true
                }
            }
        }
    }

    private fun runAction(action: IStartAction?, name: String) {
        action?.let {
            Debug.log("PopupMANAGER Action ${action.actionName} runned")
            doAsync(executorService = Executors.newCachedThreadPool()) {
                action.callInBackground()
            }
            action.callOnUi()
        }
    }

    private fun getApplicableAction(holder: SequenceHolder, name: String): IStartAction? {
        val activity = mActivity
        val factory = mActionFactory
        if (activity != null && factory != null) {
            for (i in holder.position..holder.sequence.count() - 1) {
                val clazz = holder.sequence[holder.position]
                val action = factory.construct(clazz, activity, name)
                holder.position = i + 1
                if (action != null) {
                    if (action.isApplicable) {
                        return action
                    } else {
                        Debug.log("PopupMANAGER $clazz is non applicable")
                    }
                }
            }
        }
        return null
    }

    @Synchronized fun runSequence(name: String) {
        if (mSequences.containsKey(name)) {
            val sequence = mSequences[name]
            if (sequence != null && !sequence.isExecuted) {
                Debug.log("PopupMANAGER GOOOOOO!!!")
                sequence.isExecuted = true
                runAction(getApplicableAction(sequence, name), name)
            }
        }
    }

    fun isSequenceComplete(name: String): Boolean =
            if (mSequences.containsKey(name)) {
                mSequences[name]?.let {
                    return@let it.isSequenceComplete
                }
                false
            } else {
                false
            }

    fun clear() = mSequences.clear()


    private class SequenceHolder(val sequence: PopupSequence, var position: Int = 0, var isSequenceComplete: Boolean = false, var isExecuted: Boolean = false) {
        fun hasMoarActions() = isSequenceComplete || position < sequence.count()
    }

}