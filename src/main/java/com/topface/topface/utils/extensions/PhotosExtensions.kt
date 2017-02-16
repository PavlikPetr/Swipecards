package com.topface.topface.utils.extensions

import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.data.AlbumPhotos
import com.topface.topface.data.Photo
import com.topface.topface.data.Photos
import com.topface.topface.utils.Utils
import org.json.JSONException

/**
 * Утилиты для фоточек
 * Created by tiberal on 25.07.16.
 */

fun Photos.photosForPhotoBlog(): Photos {
    val result = Photos()
    result.clear()
    this.forEach {
        if (it.canBecomeLeader) {
            result.add(Photo(it))
        }
    }
    return result
}

fun Photos.toJsonString() =
        try {
            this.toJson().toString()
        } catch (e: JSONException) {
            Debug.error(e)
            Utils.EMPTY
        }

fun Photos.getFakePhotosCount(): Int {
    var count = 0
    this.forEach {
        if (it.isFake) {
            count++
        }
    }
    return count
}

fun ObservableField<Photos>.addData(newPhotos: AlbumPhotos?) =
        apply {
            set(
                    get().addData(newPhotos)
            )
            notifyChange()
        }

fun Photos.addData(newPhotos: AlbumPhotos?) =
        apply {
            forEachIndexed { i, photo ->
                if (photo.isFake || photo.isEmpty) {
                    newPhotos?.find { it.getPosition() == i }?.let {
                        set(i, it)
                    }
                }
            }
        }



