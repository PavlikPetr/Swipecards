package com.topface.topface.experiments.onboarding.question

import android.os.Parcel
import android.os.Parcelable
import com.topface.topface.data.FeedUser
import com.topface.topface.utils.Utils
import org.json.JSONObject

/**
 * Различные модельки данных для опросника
 * Created by petrp on 29.03.2017.
 */

/**
 * Пользователь дал ответ и необходимо показать следующий вопрос
 * @param json - ответ пользователя в json
 */
data class UserChooseAnswer(var json: JSONObject = JSONObject())

/**
 * Моделька ответа на запрос настроек для опросника
 *
 * @param questionnaireMethodName - название метода в котором клиент отправит ответы пользователя
 * @param questions - список настроек для экранов опросника
 */
data class QuestionnaireResponse(var questionnaireMethodName: String = Utils.EMPTY,
                                 var questions: Array<QuestionSettings> = arrayOf<QuestionSettings>()) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<QuestionnaireResponse> = object : Parcelable.Creator<QuestionnaireResponse> {
            override fun createFromParcel(source: Parcel): QuestionnaireResponse = QuestionnaireResponse(source)
            override fun newArray(size: Int): Array<QuestionnaireResponse?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readParcelableArray(QuestionSettings::class.java.classLoader) as Array<QuestionSettings>)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(questionnaireMethodName)
        dest?.writeParcelableArray(questions, 0)
    }
}

/**
 * Настройки для конкретного экрана опросника
 *
 * @param type - тип вопроса (1 - экран с выбором диапазона; 2 - экран с выбором одного варианта из n;
 *                            3 - экран с вводом параметра; 4 - экран с выбором нескольких вариантов из списка;
 *                            5 - экран с вводом текстового ответа на вопрос)
 * @param typeFirst - данные для вопроса первого типа
 * @param typeSecond - данные для вопроса второго типа
 * @param questionWithInput - данные для вопроса третьего типа
 * @param typeFourth - данные для вопроса четвертого типа
 */
data class QuestionSettings(var type: Int = 0, var typeFirst: QuestionTypeFirst = QuestionTypeFirst(),
                            var typeSecond: QuestionTypeSecond = QuestionTypeSecond(),
                            var questionWithInput: InputValueSettings = InputValueSettings(),
                            var typeFourth: QuestionTypeFourth = QuestionTypeFourth()) : Parcelable {
    companion object {
        const val RangeQuestionScreen = 1
        const val SingleChoiseScreen = 2
        const val EnterValueScreen = 3
        const val MultiSelectScreen = 4
        const val EnterTextScreen = 5
        @JvmField val CREATOR: Parcelable.Creator<QuestionSettings> = object : Parcelable.Creator<QuestionSettings> {
            override fun createFromParcel(source: Parcel): QuestionSettings = QuestionSettings(source)
            override fun newArray(size: Int): Array<QuestionSettings?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt(), source.readParcelable<QuestionTypeFirst>(QuestionTypeFirst::class.java.classLoader), source.readParcelable<QuestionTypeSecond>(QuestionTypeSecond::class.java.classLoader), source.readParcelable<InputValueSettings>(InputValueSettings::class.java.classLoader), source.readParcelable<QuestionTypeFourth>(QuestionTypeFourth::class.java.classLoader))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(type)
        dest?.writeParcelable(typeFirst, 0)
        dest?.writeParcelable(typeSecond, 0)
        dest?.writeParcelable(questionWithInput, 0)
        dest?.writeParcelable(typeFourth, 0)
    }
}

/**
 * Настройки для первого типа экрана
 *
 * @param title - заголовок экрана
 * @param min - параметры для минимального значения
 * @param max - параметры для максимального значения
 * @param startValue - левое положение ползунка по умолчанию
 * @param endValue - правое положение ползунка по умолчанию
 */
data class QuestionTypeFirst(var title: String = Utils.EMPTY, var min: ValueConditions = ValueConditions(),
                             var max: ValueConditions = ValueConditions(), var startValue: Int = 0, var endValue: Int = 0) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<QuestionTypeFirst> = object : Parcelable.Creator<QuestionTypeFirst> {
            override fun createFromParcel(source: Parcel): QuestionTypeFirst = QuestionTypeFirst(source)
            override fun newArray(size: Int): Array<QuestionTypeFirst?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader), source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader), source.readInt(), source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeParcelable(min, 0)
        dest?.writeParcelable(max, 0)
        dest?.writeInt(startValue)
        dest?.writeInt(endValue)
    }
}

/**
 * Настройки для второго типа экрана
 *
 * @param title - заголовок экрана
 * @param fieldName - название поля, которое будет содержать ответ пользователя
 * @param buttons - список настроек для кнопок
 */
data class QuestionTypeSecond(var title: String = Utils.EMPTY, var fieldName: String = Utils.EMPTY,
                              var buttons: Array<Button> = arrayOf<Button>()) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<QuestionTypeSecond> = object : Parcelable.Creator<QuestionTypeSecond> {
            override fun createFromParcel(source: Parcel): QuestionTypeSecond = QuestionTypeSecond(source)
            override fun newArray(size: Int): Array<QuestionTypeSecond?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readParcelableArray(Button::class.java.classLoader) as Array<Button>)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(fieldName)
        dest?.writeParcelableArray(buttons, 0)
    }
}

/**
 * Настройки для кнопки на экране вопроса второго типа
 *
 * @param title - надпись на кнопке
 * @param value - значение, которое будет отправлено клиентом в поле fieldName {@link QuestionTypeSecond.fieldName}
 */
data class Button(var title: String = Utils.EMPTY, var value: String = Utils.EMPTY) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Button> = object : Parcelable.Creator<Button> {
            override fun createFromParcel(source: Parcel): Button = Button(source)
            override fun newArray(size: Int): Array<Button?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(value)
    }
}

/**
 * Настройки для третьего типа экрана
 *
 * @param title - заголовок экрана
 * @param min - параметры для минимального значения
 * @param max - параметры для максимального значения
 * @param unit - единицы измерения
 * @param fieldName - название поля, которое будет содержать ответ пользователя
 * @param hint - hint, отображаемый до начала ввода данных пользователем
 */
data class InputValueSettings(var title: String = Utils.EMPTY, var min: ValueConditions = ValueConditions(),
                              var max: ValueConditions = ValueConditions(), var unit: String = Utils.EMPTY,
                              var fieldName: String = Utils.EMPTY, var hint: String = Utils.EMPTY) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<InputValueSettings> = object : Parcelable.Creator<InputValueSettings> {
            override fun createFromParcel(source: Parcel): InputValueSettings = InputValueSettings(source)
            override fun newArray(size: Int): Array<InputValueSettings?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader), source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader), source.readString(), source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeParcelable(min, 0)
        dest?.writeParcelable(max, 0)
        dest?.writeString(unit)
        dest?.writeString(fieldName)
        dest?.writeString(hint)
    }
}

/**
 * Настройки для четвертого типа экрана
 *
 * @param title - заголовок экрана
 * @param fieldName - название поля, которое будет содержать ответ пользователя
 * @param list - список настроек для множественного выбора
 */
data class QuestionTypeFourth(var title: String = Utils.EMPTY, var fieldName: String = Utils.EMPTY,
                              var list: Array<MultiselectListItem> = arrayOf<MultiselectListItem>()) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<QuestionTypeFourth> = object : Parcelable.Creator<QuestionTypeFourth> {
            override fun createFromParcel(source: Parcel): QuestionTypeFourth = QuestionTypeFourth(source)
            override fun newArray(size: Int): Array<QuestionTypeFourth?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readParcelableArray(MultiselectListItem::class.java.classLoader) as Array<MultiselectListItem>)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(fieldName)
        dest?.writeParcelableArray(list, 0)
    }
}

/**
 * Настройки для элемента списка множественного выбора
 *
 * @param title - наименование
 * @param value - значение, которое будет отправлено клиентом в поле fieldName {@link QuestionTypeFourth.fieldName}
 * @param image - ссылка на картинку
 * @param isSelected - флаг выбора, установленный по умолчанию
 */
data class MultiselectListItem(var title: String = Utils.EMPTY, var value: String = Utils.EMPTY,
                               var image: String = Utils.EMPTY, var isSelected: Boolean = false) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<MultiselectListItem> = object : Parcelable.Creator<MultiselectListItem> {
            override fun createFromParcel(source: Parcel): MultiselectListItem = MultiselectListItem(source)
            override fun newArray(size: Int): Array<MultiselectListItem?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(), 1.equals(source.readInt()))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(value)
        dest?.writeString(image)
        dest?.writeInt((if (isSelected) 1 else 0))
    }
}

/**
 * Настройки условий крайних значений
 * @param value - предельное значение
 * @param errorMessage - сообщение, которое будет показано пользователю, если введенные им данные будут
 *                       за пределом порогового значения
 * @param fieldName - значение, которое будет отправлено клиентом в поле fieldName
 */
data class ValueConditions(var value: Int = 0, var fieldName: String = Utils.EMPTY, var errorMessage: String = Utils.EMPTY) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ValueConditions> = object : Parcelable.Creator<ValueConditions> {
            override fun createFromParcel(source: Parcel): ValueConditions = ValueConditions(source)
            override fun newArray(size: Int): Array<ValueConditions?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt(), source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(value)
        dest?.writeString(fieldName)
        dest?.writeString(errorMessage)
    }
}