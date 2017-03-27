package com.topface.billing.ninja

import android.content.Intent

/**
 * Интерфейс для завершения активити
 * Created by petrp on 21.03.2017.
 */
interface IFinishDelegate {
    fun finishWithResult(resultCode: Int, data: Intent)
}