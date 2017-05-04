package com.topface.topface.api.responses.response_utils

class Interval(private val minSize: Size, private val maxSize: Size) {

    fun isSizeInInterval(size: Size) = size.width > minSize.width && size.height > minSize.height
            && size.width < maxSize.width && size.height < maxSize.height

}