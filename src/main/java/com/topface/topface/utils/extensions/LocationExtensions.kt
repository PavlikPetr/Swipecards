package com.topface.topface.utils.extensions

import android.location.Location


/* Расширения для Location
 * Created by mbulgakov on 20.01.17.
 */

fun Location.isValidLocation() = this.latitude in -90..90 && this.longitude in -180..180


