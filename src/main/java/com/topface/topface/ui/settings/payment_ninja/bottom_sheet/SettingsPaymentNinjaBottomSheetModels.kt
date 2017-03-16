package com.topface.topface.ui.settings.payment_ninja.bottom_sheet

import android.os.Parcel
import android.os.Parcelable
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

/**
 * Тип модального bottom sheet
 * @param type - тип dialogFragment-а ({@link #CARD_BOTTOM_SHEET}/{@link #RESTORE_SUBSCRIPTION_BOTTOM_SHEET}/{@link #CANCEL_SUBSCRIPTION_BOTTOM_SHEET})
 */
data class ModalBottomSheetType(var type: Int) : Parcelable {
    companion object {
        const val CARD_BOTTOM_SHEET = 1

        const val RESTORE_SUBSCRIPTION_BOTTOM_SHEET = 2

        const val CANCEL_SUBSCRIPTION_BOTTOM_SHEET = 3

        const val CANCEL_AUTOFILLING_BOTTOM_SHEET = 4

        @JvmField val CREATOR: Parcelable.Creator<ModalBottomSheetType> = object : Parcelable.Creator<ModalBottomSheetType> {
            override fun createFromParcel(source: Parcel): ModalBottomSheetType = ModalBottomSheetType(source)
            override fun newArray(size: Int): Array<ModalBottomSheetType?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(type)
    }
}