package com.topface.topface.ui.fragments.feed.enhanced.chat.items

import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.utils.DateUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import java.text.SimpleDateFormat

/**
 * Utils for date formatting
 * PS оставил его здесь и не стал захламлять наши DateUtils ибо это эксперимент
 */
internal class DateUtils {
    companion object {
        private val mDateFormatDayYear = SimpleDateFormat("dd.MM.yyyy", App.getCurrentLocale())
        private val mDateFormatDay = SimpleDateFormat("dd.MM", App.getCurrentLocale())

        /**
         * Converts date in milliseconds to string representation
         * PS partially copy/paste from global DateUtils
         */
        fun getRelativeDate(date: Long) =
            if (date > DateUtils.midnight) { // сегодня
                R.string.time_today.getString()
            } else if (date > DateUtils.midnight - Utils.DAY) { //вчера
                R.string.time_yesterday.getString()
            } else if (date > DateUtils.current_year) { // неделю назад
                mDateFormatDay.format(date)
            } else {
                mDateFormatDayYear.format(date)
            }
    }

}