package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.databinding.ObservableField

/**
 * ViewModel заглущки о пустом ответе от сервера на экране "Люди рядом" или при фэйле в процессе поиска
 * текущего местополжения
 * @param text - текст, который будет выведен под картинкой
 * Created by ppavlik on 11.01.17.
 */

class PeopleNearbyEmptyViewModel(val text: String)