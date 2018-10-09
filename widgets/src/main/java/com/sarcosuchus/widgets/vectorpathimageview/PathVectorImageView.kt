package com.sarcosuchus.widgets.vectorpathimageview

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import com.devs.vectorchildfinder.VectorChildFinder
import com.devs.vectorchildfinder.VectorDrawableCompat.VFullPath
import com.sarcosuchus.widgets.Constants
import com.sarcosuchus.widgets.Constants.Common
import com.sarcosuchus.widgets.Constants.Common.DEFAULT_NOTHING
import com.sarcosuchus.widgets.R.styleable
import kotlin.properties.Delegates.observable

class PathVectorImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var colorBackground: Int by observable(Color.parseColor("#F3F3F3")) { _, _, _ ->
        drawVectorImage()
    }

    var colorStrokeDefault: Int by observable(Color.parseColor("#9ddfe7")) { _, _, _ ->
        drawVectorImage()
    }

    var colorStroke: Int? by observable<Int?>(null) { _, _, _ ->
        drawVectorImage()
    }

    var colorRegion: Int? by observable<Int?>(null) { _, _, _ ->
        drawVectorImage()
    }

    var pathNameBackground: String? by observable<String?>(null) { _, _, _ ->
        pathBackground = vectorChildFinder?.findPathByName(pathNameBackground)
        pathBackground?.fillColor = colorBackground
        drawVectorImage()
    }

    var vectorDrawableId: Int by observable(Common.DEFAULT_NOTHING) { _, _, newValue ->
        if (newValue != DEFAULT_NOTHING) {
            setImageDrawable(ContextCompat.getDrawable(context, newValue))
            vectorChildFinder = VectorChildFinder(context, newValue, this).apply {
                if (pathBackground == null) {
                   pathBackground = findPathByName(pathNameBackground)
                }
                pathBackground!!.fillColor = colorBackground
            }
            drawVectorImage()
        }
    }

    var vectorPathArray: Array<VectorPath> by observable(emptyArray()) { _, _, newValue ->
        step = 0
        currentVectorPath = newValue[0]
        drawVectorImage()
    }

    private var step = 0
    private var vectorChildFinder: VectorChildFinder? = null
    private var currentVectorPath: VectorPath? = null
    private var previousVectorPath: VectorPath? = null
    private var pathBackground: VFullPath? = null

    init {
        attrs?.let { _ ->
            val typedArray = context.obtainStyledAttributes(attrs,
                    styleable.PathVectorImageView, 0, 0)

            (0 until typedArray.indexCount)
                    .map { typedArray.getIndex(it) }
                    .forEach {
                        when (it) {
                            styleable.PathVectorImageView_color_background_image -> colorBackground = typedArray
                                    .getColor(it, Color.parseColor("#F3F3F3"))
                            styleable.PathVectorImageView_color_stroke_image -> colorStrokeDefault = typedArray
                                    .getColor(it, Color.parseColor("#9ddfe7"))
                            styleable.PathVectorImageView_path_name_background_image -> pathNameBackground = typedArray.getString(it)
                            styleable.PathVectorImageView_vector_drawable -> vectorDrawableId = typedArray
                                    .getResourceId(it, Constants.Common.DEFAULT_NOTHING)
                        }
                    }
            typedArray.recycle()
        }
    }

    fun getSelecteionPosition() = step

    fun setSelectedPosition(position: Int) {
        step = position
        drawVectorImage()
    }

    fun isFirstRegion() = step == 0

    fun isLastRegion() = step == vectorPathArray.lastIndex

    fun setSelectedColor(@ColorInt colorRegion: Int?, @ColorInt colorStroke: Int?) {
        this.colorRegion = colorRegion
        this.colorStroke = colorStroke
    }

    fun next(@ColorInt colorRegion: Int?, @ColorInt colorStroke: Int?) {
        step++
        if (step > vectorPathArray.lastIndex) {
            step = vectorPathArray.lastIndex
            previousVectorPath = null
        } else {
            previousVectorPath = currentVectorPath
        }
        initRegion(step, colorRegion, colorStroke)
    }

    fun previous(@ColorInt colorRegion: Int?, @ColorInt colorStroke: Int?) {
        step--
        if (step < 0) {
            step = 0
            previousVectorPath = null
        } else {
            previousVectorPath = currentVectorPath
        }
        initRegion(step, colorRegion, colorStroke)
    }

    private fun initRegion(position: Int, @ColorInt colorRegion: Int?, @ColorInt colorStroke: Int?) {
        currentVectorPath = vectorPathArray[position]
        this.colorRegion = colorRegion
        this.colorStroke = colorStroke
    }

    private fun drawVectorImage() {
        vectorChildFinder?.apply {
            previousVectorPath?.let { vectorPath ->
                vectorPath.pathNames.forEach {
                    vectorChildFinder?.findPathByName(it)?.fillColor = vectorPath.defaultColorBackground
                }
            }
            currentVectorPath?.let { vectorPath ->
                pathBackground?.strokeColor = colorStroke ?: colorStrokeDefault
                vectorPath.pathNames.forEach {
                    vectorChildFinder?.findPathByName(it)?.fillColor = colorRegion ?: vectorPath.defaultSelectedColorBackground
                }
            }
        }
        invalidate()
    }
}