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

data class BottomSheetItemText(@StringRes val textRes: Int) : Parcelable {
    companion object {
        const val CANCEL_SUBSCRIPTION = R.string.ninja_cancel_vip_status
        const val RESUME_SUBSCRIPTION = R.string.ninja_resume_vip_status
        const val DELETE_CARD = R.string.ninja_delete_card
        const val USE_ANOTHER_CARD = R.string.ninja_use_another_card
        const val ADD_CARD = R.string.ninja_no_card_title

        @JvmField val CREATOR: Parcelable.Creator<BottomSheetItemText> = object : Parcelable.Creator<BottomSheetItemText> {
            override fun createFromParcel(source: Parcel): BottomSheetItemText = BottomSheetItemText(source)
            override fun newArray(size: Int): Array<BottomSheetItemText?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(textRes)
    }
}

data class BottomSheetData(var textRes: BottomSheetItemText, var data: Parcelable) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<BottomSheetData> = object : Parcelable.Creator<BottomSheetData> {
            override fun createFromParcel(source: Parcel): BottomSheetData = BottomSheetData(source)
            override fun newArray(size: Int): Array<BottomSheetData?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readParcelable<BottomSheetItemText>(BottomSheetItemText::class.java.classLoader),
            source.readParcelable(Parcelable::class.java.classLoader))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(textRes, 0)
        dest?.writeParcelable(data, flags)
    }
}

/**
 * Тип модального bottom sheet
 * @param type - тип dialogFragment-а ({@link #CARD_BOTTOM_SHEET}/{@link #RESTORE_SUBSCRIPTION_BOTTOM_SHEET}
 * /{@link #CANCEL_SUBSCRIPTION_BOTTOM_SHEET})
 */
data class ModalBottomSheetType(var type: Int) : Parcelable {
    companion object {
        const val CARD_BOTTOM_SHEET = 1

        const val RESTORE_SUBSCRIPTION_BOTTOM_SHEET = 2

        const val CANCEL_SUBSCRIPTION_BOTTOM_SHEET = 3

        const val CANCEL_AUTOFILLING_BOTTOM_SHEET = 4

        const val CARD_DELETED_BOTTOM_SHEET = 5

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

data class ModalBottomSheetData(var type: ModalBottomSheetType, var data: Parcelable) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ModalBottomSheetData> = object : Parcelable.Creator<ModalBottomSheetData> {
            override fun createFromParcel(source: Parcel): ModalBottomSheetData = ModalBottomSheetData(source)
            override fun newArray(size: Int): Array<ModalBottomSheetData?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readParcelable<ModalBottomSheetType>(ModalBottomSheetType::class.java.classLoader),
            source.readParcelable(Parcelable::class.java.classLoader))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(type, 0)
        dest?.writeParcelable(data, flags)
    }
}