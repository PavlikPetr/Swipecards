package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import com.topface.topface.R

/**
 * Данные для отображения во вьюхах
 * Created by tiberal on 16.11.16.
 */
class BoilerplateData private constructor(val title: Int, val buttonText: Int, val description: Int) {

    private constructor(builder: Builder) : this(builder.title, builder.buttonText, builder.description)

    /**
     * @param title - титул попапа
     * @param buttonText - текст на кнопке()
     * @param description - описание под кнопкой
     */
    companion object {
        fun create(init: Builder.() -> Unit) = Builder(init).build()
    }

    class Builder private constructor() {

        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        var title = 0
        var buttonText = R.string.buy_vip_button_text
        var description = R.string.seven_days_free

        fun build() = BoilerplateData(this)

    }
}