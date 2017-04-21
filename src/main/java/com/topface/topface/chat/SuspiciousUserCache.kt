package com.topface.topface.chat

import android.databinding.ObservableBoolean

/**
 * Cache of suspicious users
 */
class SuspiciousUserCache {
    /**
     * кэш с индексом по id пользователя
     * содержит в себе пары
     * Boolean - была ли произведена настройка с сервера
     * ObservableBoolean - сама настройка "подозрительный" пользователь или нет
     * пара используется чтобы с сервера настройку воспринимать только один раз за время жизни приложения
     */
    private val mCache: MutableMap<Int, Pair<Boolean, ObservableBoolean>> = mutableMapOf()

    fun getIsUserSuspicious(uid: Int): ObservableBoolean {
        var pair = mCache[uid]
        if (pair == null) {
            pair = Pair(false, ObservableBoolean(false))
            mCache[uid] = pair
        }
        return pair.second
    }

    fun setUserIsSuspicious(uid: Int, isSuspicious: Boolean) {
        val pair = mCache[uid]
        if (pair == null) {
            mCache[uid] = Pair(false, ObservableBoolean(isSuspicious))
        } else {
            pair.second.set(isSuspicious)
        }
    }

    fun setUserIsSuspiciousIfNeed(uid: Int, isSuspicious: Boolean) {
        val pair = mCache[uid]
        if (pair == null) {
            mCache[uid] = Pair(true, ObservableBoolean(isSuspicious))
        } else if (!pair.first) {
            pair.second.set(isSuspicious)
            mCache[uid] = Pair(true, pair.second)
        }
    }
}