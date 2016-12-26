package com.topface.topface.statistics

import com.topface.statistics.android.StatisticsTracker
import com.topface.topface.App
import com.topface.topface.BuildConfig
import com.topface.topface.data.Options
import com.topface.topface.state.TopfaceAppState
import javax.inject.Inject

/**
 * Добавляем дефолтный набор срезов в клиентскую статистику
 * подписываемся на обновление опций, чтобы подсунуть в конфиг статистики актуальные клиентские срезы
 * Created by ppavlik on 23.12.16.
 */

class CommonSlices private constructor() {
    @Inject lateinit internal var mAppState: TopfaceAppState

    private object Holder {
        val INSTANCE = CommonSlices()
    }

    companion object {
        // срезы, которые будут в статистике по умолчанию
        val DEFAULT_SLICES = hashMapOf<String, Any>(Pair("app", BuildConfig.STATISTICS_APP), Pair("cvn", BuildConfig.VERSION_NAME))
        val instance: CommonSlices by lazy { Holder.INSTANCE }
    }

    init {
        App.get().inject(this)
        StatisticsTracker.getInstance().setPredefinedSlice(CommonSlices.DEFAULT_SLICES)
        mAppState.getObservable(Options::class.java).distinctUntilChanged().subscribe {
            StatisticsTracker.getInstance().setPredefinedSlice(it.statisticsSlices.apply {
                putAll(CommonSlices.DEFAULT_SLICES)
            })
        }
    }
}