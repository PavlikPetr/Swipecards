package com.topface.topface.utils.popups

import com.topface.topface.utils.controllers.ChosenStartAction

/**
 * Хранит очередь попапов
 * Created by tiberal on 31.08.16.
 */
class PopupSequence {

    private val list = mutableListOf<ActionHolder>()

    fun addAction(actionsClass: Class<*>): PopupSequence {
        list.add(ActionHolder(actionsClass))
        return this
    }

    fun addChosenAction(vararg actionsClass: Class<*>) : PopupSequence {
        val holder = ActionHolder(ChosenStartAction::class.java)
        holder.nestedActions.addAll(actionsClass)
        list.add(holder)
        return this
    }

    operator fun get(position: Int): ActionHolder = list[position]

    fun isEmpty() = list.isEmpty()

    fun count() = list.count()

    class ActionHolder(val actionsClass: Class<*>) {

        val nestedActions: MutableList<Class<*>> = mutableListOf()

        fun isCompositeAction() = nestedActions.count() > 1

    }

}

