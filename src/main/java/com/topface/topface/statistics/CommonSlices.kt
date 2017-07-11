package com.topface.topface.statistics

import com.topface.statistics.android.Slices
import com.topface.statistics.android.StatisticsTracker
import com.topface.topface.App
import com.topface.topface.BuildConfig
import com.topface.topface.data.Options
import com.topface.topface.utils.rx.shortSubscription

/**
 * Добавляем дефолтный набор срезов в клиентскую статистику
 * подписываемся на обновление опций, чтобы подсунуть в конфиг статистики актуальные клиентские срезы
 * Created by ppavlik on 23.12.16.
 */

class CommonSlices private constructor() {
    private val mAppState by lazy {
        App.getAppComponent().appState()
    }

    private object Holder {
        val INSTANCE = CommonSlices()
    }

    companion object {
        // срезы, которые будут в статистике по умолчанию
        val DEFAULT_SLICES = hashMapOf<String, Any>(Pair("app", BuildConfig.STATISTICS_APP), Pair("cvn", BuildConfig.VERSION_NAME))
        val instance: CommonSlices by lazy { Holder.INSTANCE }
    }

    init {
        StatisticsTracker.getInstance().setPredefinedSlice(CommonSlices.DEFAULT_SLICES)
        mAppState.getObservable(Options::class.java).distinctUntilChanged().subscribe(shortSubscription {
            StatisticsTracker.getInstance().setPredefinedSlice(Slices().apply {
                putAll(CommonSlices.DEFAULT_SLICES)
            })
        })
    }
}