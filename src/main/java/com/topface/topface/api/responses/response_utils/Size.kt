package com.topface.topface.api.responses.response_utils

class Size(var width: Int = 0, var height: Int = 0) {

    companion object {
        /**
         * Процент от максимального измерения фотографии,
         * отношение длины к ширине которое мы считаем допустимым при опредлении "квадратных" фотографий
         */
        const val SQUARE_MODIFICATOR = 0.10
        const val WIDTH = "width"
        const val HEIGHT = "height"
    }

    //Если разница высоты и ширины меньше 10% от размера фотографии, то считаем ее условно квадратной
    val isSquare: Boolean
        get() = Math.abs(width - height) < Math.min(width, height) * SQUARE_MODIFICATOR

    fun getDifference(size: Size) = if ((size.maxSide == WIDTH || height == 0) && width != 0)
        Math.abs(width - size.width)
    else
        Math.abs(height - size.height)


    val maxSide: String
        get() = if (width > height) WIDTH else HEIGHT

    val maxSideSize: Int
        get() = if (maxSide == WIDTH) width else height

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size) return false

        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        return result
    }
}