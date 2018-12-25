package com.sarcosuchus.widgets.vectorpathimageview

import android.graphics.Color
import android.support.annotation.ColorInt
import java.util.*

data class VectorPath(@ColorInt val defaultSelectedColorBackground: Int = Color.parseColor("#9ddfe7"),
                      @ColorInt val defaultColorBackground: Int =  Color.parseColor("#d5d8d9"),
                      val pathNames: Array<String>, val name: String? = null, val description: String? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VectorPath

        if (defaultSelectedColorBackground != other.defaultSelectedColorBackground) return false
        if (defaultColorBackground != other.defaultColorBackground) return false
        if (!Arrays.equals(pathNames, other.pathNames)) return false
        if (name != other.name) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = defaultSelectedColorBackground
        result = 31 * result + defaultColorBackground
        result = 31 * result + Arrays.hashCode(pathNames)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }


}