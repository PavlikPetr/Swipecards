package com.topface.topface.experiments.onboarding.question

import android.os.Parcel
import android.os.Parcelable
import com.topface.topface.data.FeedUser
import com.topface.topface.utils.Utils
import org.json.JSONObject
import java.util.*

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

    constructor(source: Parcel) : this(source.readString(), source.createTypedArray(QuestionSettings.CREATOR))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(questionnaireMethodName)
        dest?.writeTypedArray(questions, 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as QuestionnaireResponse

        if (questionnaireMethodName != other.questionnaireMethodName) return false
        if (!Arrays.equals(questions, other.questions)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = questionnaireMethodName.hashCode()
        result = 31 * result + Arrays.hashCode(questions)
        return result
    }

    fun isEmpty() = this == QuestionnaireResponse()
}

/**
 * Настройки для конкретного экрана опросника
 *
 * @param type - тип вопроса (1 - экран с выбором диапазона; 2 - экран с выбором одного варианта из n;
 *                            3 - экран с вводом параметра; 4 - экран с выбором нескольких вариантов из списка;
 *                            5 - экран с вводом текстового ответа на вопрос)
 * @param typeFirst - данные для вопроса первого типа
 * @param typeSecond - данные для вопроса второго типа
 * @param typeThird - данные для вопроса третьего типа
 * @param typeFourth - данные для вопроса четвертого типа
 * @param typeFifth - данные для вопроса пятого типа
 */
data class QuestionSettings(var type: Int = 0, var typeFirst: QuestionTypeFirst = QuestionTypeFirst(),
                            var typeSecond: QuestionTypeSecond = QuestionTypeSecond(),
                            var typeThird: InputValueSettings = InputValueSettings(),
                            var typeFourth: QuestionTypeFourth = QuestionTypeFourth(),
                            var typeFifth: InputValueSettings = InputValueSettings()) : Parcelable {
    companion object {
        const val RangeQuestionScreen = 1
        const val SingleChoiseScreen = 2
        const val DigitInputScreen = 3
        const val MultiSelectScreen = 4
        const val TextInputScreen = 5
        @JvmField val CREATOR: Parcelable.Creator<QuestionSettings> = object : Parcelable.Creator<QuestionSettings> {
            override fun createFromParcel(source: Parcel): QuestionSettings = QuestionSettings(source)
            override fun newArray(size: Int): Array<QuestionSettings?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readInt(),
            source.readParcelable<QuestionTypeFirst>(QuestionTypeFirst::class.java.classLoader),
            source.readParcelable<QuestionTypeSecond>(QuestionTypeSecond::class.java.classLoader),
            source.readParcelable<InputValueSettings>(InputValueSettings::class.java.classLoader),
            source.readParcelable<QuestionTypeFourth>(QuestionTypeFourth::class.java.classLoader),
            source.readParcelable<InputValueSettings>(InputValueSettings::class.java.classLoader))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(type)
        dest?.writeParcelable(typeFirst, 0)
        dest?.writeParcelable(typeSecond, 0)
        dest?.writeParcelable(typeThird, 0)
        dest?.writeParcelable(typeFourth, 0)
        dest?.writeParcelable(typeFifth, 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as QuestionSettings

        if (type != other.type) return false
        if (typeFirst != other.typeFirst) return false
        if (typeSecond != other.typeSecond) return false
        if (typeThird != other.typeThird) return false
        if (typeFourth != other.typeFourth) return false
        if (typeFifth != other.typeFifth) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + typeFirst.hashCode()
        result = 31 * result + typeSecond.hashCode()
        result = 31 * result + typeThird.hashCode()
        result = 31 * result + typeFourth.hashCode()
        result = 31 * result + typeFifth.hashCode()
        return result
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

    constructor(source: Parcel) : this(source.readString(),
            source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader),
            source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader),
            source.readInt(), source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeParcelable(min, 0)
        dest?.writeParcelable(max, 0)
        dest?.writeInt(startValue)
        dest?.writeInt(endValue)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as QuestionTypeFirst

        if (title != other.title) return false
        if (min != other.min) return false
        if (max != other.max) return false
        if (startValue != other.startValue) return false
        if (endValue != other.endValue) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + min.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + startValue
        result = 31 * result + endValue
        return result
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

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.createTypedArray(Button.CREATOR))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(fieldName)
        dest?.writeTypedArray(buttons, 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as QuestionTypeSecond

        if (title != other.title) return false
        if (fieldName != other.fieldName) return false
        if (!Arrays.equals(buttons, other.buttons)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + fieldName.hashCode()
        result = 31 * result + Arrays.hashCode(buttons)
        return result
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Button

        if (title != other.title) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + value.hashCode()
        return result
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

    constructor(source: Parcel) : this(source.readString(),
            source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader),
            source.readParcelable<ValueConditions>(ValueConditions::class.java.classLoader),
            source.readString(), source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeParcelable(min, 0)
        dest?.writeParcelable(max, 0)
        dest?.writeString(unit)
        dest?.writeString(fieldName)
        dest?.writeString(hint)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as InputValueSettings

        if (title != other.title) return false
        if (min != other.min) return false
        if (max != other.max) return false
        if (unit != other.unit) return false
        if (fieldName != other.fieldName) return false
        if (hint != other.hint) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + min.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + fieldName.hashCode()
        result = 31 * result + hint.hashCode()
        return result
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

    constructor(source: Parcel) : this(source.readString(), source.readString(),
            source.createTypedArray(MultiselectListItem.CREATOR))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(fieldName)
        dest?.writeTypedArray(list, 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as QuestionTypeFourth

        if (title != other.title) return false
        if (fieldName != other.fieldName) return false
        if (!Arrays.equals(list, other.list)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + fieldName.hashCode()
        result = 31 * result + Arrays.hashCode(list)
        return result
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

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(), 1 == source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(title)
        dest?.writeString(value)
        dest?.writeString(image)
        dest?.writeInt((if (isSelected) 1 else 0))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as MultiselectListItem

        if (title != other.title) return false
        if (value != other.value) return false
        if (image != other.image) return false
        if (isSelected != other.isSelected) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + isSelected.hashCode()
        return result
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ValueConditions

        if (value != other.value) return false
        if (fieldName != other.fieldName) return false
        if (errorMessage != other.errorMessage) return false
        return true
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + fieldName.hashCode()
        result = 31 * result + errorMessage.hashCode()
        return result
    }
}

/**
 * Настройки для экрана показа "100500" объектов для знакомств
 *
 * @param foundTitle - заголовок экрана
 * @param buyMessage - текст самого попапа
 * @param productId - Id продукта для оплаты
 * @param users - список людишек для отображения их аватаров
 */
data class QuestionnaireResult(var foundTitle: String = Utils.EMPTY, var buyMessage: String = Utils.EMPTY,
                               var productId: String = Utils.EMPTY, var users: Array<FeedUser> = arrayOf<FeedUser>()) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<QuestionnaireResult> = object : Parcelable.Creator<QuestionnaireResult> {
            override fun createFromParcel(source: Parcel): QuestionnaireResult = QuestionnaireResult(source)
            override fun newArray(size: Int): Array<QuestionnaireResult?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(source.readString(), source.readString(), source.readString(),
            source.readParcelableArray(FeedUser::class.java.classLoader) as Array<FeedUser>)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(foundTitle)
        dest?.writeString(buyMessage)
        dest?.writeString(productId)
        dest?.writeParcelableArray(users, 0)
    }
}