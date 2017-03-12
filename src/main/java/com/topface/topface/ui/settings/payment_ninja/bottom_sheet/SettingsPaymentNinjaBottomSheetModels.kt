package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import android.support.annotation.IntDef
import android.support.annotation.StringRes
import com.topface.topface.R

/**
 * Различные модельки для bottom sheet экрана платежей payment ninja
 * Created by petrp on 10.03.2017.
 */

/**
 * Модель для title bottom sheet экрана платежей payment ninja
 * @param title - текст для отображения
 */
data class BottomSheetTitle(var title: String)

/**
 * Пул итемов для bottom sheet
 * @param textRes - ресурс для отображения во вью
 */
enum class BOTTOM_SHEET_ITEMS_POOL(@StringRes val textRes: Int) {
    CANCEL_SUBSCRIPTION(R.string.ninja_cancel_vip_status),
    RESUME_SUBSCRIPTION(R.string.ninja_resume_vip_status),
    DELETE_CARD(R.string.ninja_delete_card),
    USE_ANOTHER_CARD(R.string.ninja_use_another_card)
}