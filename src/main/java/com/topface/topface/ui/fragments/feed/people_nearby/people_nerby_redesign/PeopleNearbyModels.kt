package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.os.Bundle
import android.support.annotation.IntDef
import com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup

/**
 * Модельки для нового экрана "Люди рядом"
 * Created by ppavlik on 11.01.17.
 */

/**
 * Модель для отправки актуального статуса ptr на экране "Люди рядом"
 */
data class PeopleNearbyRefreshStatus(var isRefreshing: Boolean = true)

/**
 * Заглушка пустого списка "Люди рядом" (Вокруг ни души)
 */
class PeopleNearbyEmptyList //----------------

/**
 * Заглушка заблокированного списка "Люди рядом" (PREMIUM_ACCESS_ONLY)
 */
class PeopleNearbyVipOnly

/**
 * Заглушка о проблеме с получением ГЕО
 */
class PeopleNearbyEmptyLocation //----------------

/**
 * Заглушка с запросом на разрешение доступа к пермишину {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
 */
class PeopleNearbyPermissionDenied //----------------

/**
 * Заглушка с информацией о том, что следует пройти в настройки приложения для активации пермишина {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
 */
class PeopleNearbyPermissionNeverAskAgain //----------------

/**
 * Итем списка "Люди рядом"
 */
class PeopleNearbyList

/**
 * Итем списка "Фотолента"
 */
class PhotoBlogList

/**
 * Лоадер на время отправки запроса
 */
class PeopleNearbyLoader //----------------


data class PeopleNearbyListEvents(val action:PeopleNearbyAction){
    companion object {


        const val ACTION_UNDEFINED = 0L
        const val ACTION_CAMERA_CHOSEN = 1L
        const val ACTION_GALLERY_CHOSEN = 2L
        const val ACTION_CANCEL = 3L
    }

    @IntDef(ACTION_UNDEFINED, ACTION_CAMERA_CHOSEN, ACTION_GALLERY_CHOSEN, ACTION_CANCEL)
    annotation class PeopleNearbyAction
}