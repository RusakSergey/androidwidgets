package com.sarcosuchus.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.StyleableRes
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.math.BigDecimal
import kotlin.properties.Delegates

private const val DEFAULT_MAX_RATING = 5
private const val DEFAULT_MARGIN_BETWEEN = 10
private const val DEFAULT_NOTHING = -1

class ColorRatingWidget @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        interface ColorRatingListener {
            fun onChange(value: Int, @ColorInt intColor: Int)
        }
    }

    private val attrs = intArrayOf(android.R.attr.textSize, android.R.attr.textColor, android.R.attr.fontFamily)

    private val startColorDefault = Color.parseColor("#66b456")
    private val middleColorDefault = Color.parseColor("#f4b342")
    private val endColorDefault = Color.parseColor("#c2362a")
    private val rect = Rect()

    private val srcDrawable: Drawable by lazy {
        ContextCompat.getDrawable(context, R.drawable.ic_color_rating)!!
    }

    private val redColors: Array<Int> by lazy {
        arrayOf(Color.red(startColorDefault), Color.red(middleColorDefault), Color.red(endColorDefault))
    }
    private val greenColors: Array<Int> by lazy {
        arrayOf(Color.green(startColorDefault), Color.green(middleColorDefault), Color.green(endColorDefault))
    }
    private val blueColors: Array<Int> by lazy {
        arrayOf(Color.blue(startColorDefault), Color.blue(middleColorDefault), Color.blue(endColorDefault))
    }

    private val paintTextDefault: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private val paintTextSelected: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private var markerWidth: Int by Delegates.observable(srcDrawable.intrinsicWidth) { _, _, newValue ->
        markerWidthWithSpace = marginBetween + newValue
        positionMarkerArray = IntArray(quantity) { i ->
            if (i == 0) marginBetween
            else markerWidthWithSpace * i + marginBetween
        }
        textPositionArray = IntArray(quantity) { i -> positionMarkerArray[i] + newValue / 2 }
        invalidate()
    }

    var isTextVisibility: Boolean by Delegates.observable(false) { _, _, _ ->
        invalidate()
    }

    var displayNames by Delegates.observable<Array<String>?>(null) { _, _, _ ->
        invalidate()
    }

    var maxRatingValue: Int by Delegates.observable(DEFAULT_MAX_RATING) { _, _, _ ->
        quantity = calculateQuantity()
    }

    var isStartZero: Boolean by Delegates.observable(false) { _, _, _ ->
        quantity = calculateQuantity()
    }

    var marginBetween: Int by Delegates.observable(DEFAULT_MARGIN_BETWEEN) { _, _, _ ->
        invalidate()
    }

    var startColor: Int by Delegates.observable(startColorDefault) { _, _, newValue ->
        redColors[0] = Color.red(newValue)
        greenColors[0] = Color.green(newValue)
        blueColors[0] = Color.blue(newValue)
        invalidate()
    }

    var middleColor: Int by Delegates.observable(middleColorDefault) { _, _, newValue ->
        redColors[1] = Color.red(newValue)
        greenColors[1] = Color.green(newValue)
        blueColors[1] = Color.blue(newValue)
        invalidate()
    }

    var endColor: Int by Delegates.observable(endColorDefault) { _, _, newValue ->
        redColors[2] = Color.red(newValue)
        greenColors[2] = Color.green(newValue)
        blueColors[2] = Color.blue(newValue)
        invalidate()
    }

    var textAppearanceDefault: Int by Delegates.observable(DEFAULT_NOTHING) { _, _, newValue ->
        setTextPaint(paintTextDefault, newValue)
        invalidate()
    }
    var textAppearanceSelected: Int by Delegates.observable(DEFAULT_NOTHING) { _, _, newValue ->
        setTextPaint(paintTextSelected, newValue)
        invalidate()
    }

    private var quantity: Int by Delegates.observable(DEFAULT_MAX_RATING + 1) { _, _, newValue ->
        middleRatingValue = newValue / 2
        stepCount = BigDecimal(newValue / 2).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
        drawableArray = Array(newValue) {
            srcDrawable.constantState.newDrawable().apply {
                mutate()
                DrawableCompat.wrap(this)
            }
        }
        markerWidth = srcDrawable.intrinsicWidth
        colorMarkerArray = IntArray(newValue) { i -> getColorFromPosition(i) }
        invalidate()
    }

    private var mColorRatingListener: ColorRatingListener? = null

    private var isDrawingFinish = false

    private var middleRatingValue = 3
    private var stepCount = 0

    private var textPositionArray: IntArray = intArrayOf()
    private var colorMarkerArray: IntArray = intArrayOf()
    private var positionMarkerArray: IntArray = intArrayOf()

    private lateinit var drawableArray: Array<Drawable>

    private var markerWidthWithSpace = 0
    private var currentSelectX = 0f

    var selectItemPosition: Int = DEFAULT_NOTHING
        set(value) {
            field = value
            currentSelectX = calculateXFromPosition(value)
            mColorRatingListener?.onChange(value, colorMarkerArray[value])
            invalidate()
        }

    init {
        attrs?.let { attributeSet ->
            val typedArray = context.obtainStyledAttributes(attributeSet,
                    R.styleable.ColorRatingWidget, 0, 0)

            (0 until typedArray.indexCount)
                    .map { typedArray.getIndex(it) }
                    .forEach { resId ->
                        when (resId) {
                            R.styleable.ColorRatingWidget_text_visibility -> isTextVisibility = typedArray.getBoolean(resId, false)
                            R.styleable.ColorRatingWidget_display_names -> displayNames = typedArray.getTextArray(resId).map { it.toString() }.toTypedArray()
                            R.styleable.ColorRatingWidget_max_rating_value -> maxRatingValue = typedArray.getInt(resId, DEFAULT_MAX_RATING)
                            R.styleable.ColorRatingWidget_start_zero -> isStartZero = typedArray.getBoolean(resId, false)
                            R.styleable.ColorRatingWidget_margin_between -> marginBetween = typedArray.getDimensionPixelSize(resId, DEFAULT_MARGIN_BETWEEN)
                            R.styleable.ColorRatingWidget_start_color -> startColor = typedArray.getColor(resId, startColorDefault)
                            R.styleable.ColorRatingWidget_middle_color -> middleColor = typedArray.getColor(resId, middleColorDefault)
                            R.styleable.ColorRatingWidget_end_color -> endColor = typedArray.getColor(resId, endColorDefault)
                            R.styleable.ColorRatingWidget_text_appearance_default -> textAppearanceDefault = typedArray.getResourceId(resId, DEFAULT_NOTHING)
                            R.styleable.ColorRatingWidget_text_appearance_selected -> textAppearanceSelected = typedArray.getResourceId(resId, DEFAULT_NOTHING)
                        }
                    }
            typedArray.recycle()
        }
    }

    private fun calculateQuantity() = if (isStartZero) maxRatingValue + 1 else maxRatingValue

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = quantity * (srcDrawable.intrinsicWidth + marginBetween) + 2 * marginBetween

        val width = when (widthMode) {
            View.MeasureSpec.EXACTLY ->
                widthSize
            View.MeasureSpec.AT_MOST ->
                Math.min(desiredWidth, widthSize)
            View.MeasureSpec.UNSPECIFIED ->
                desiredWidth
            else -> desiredWidth
        }

        markerWidth = (width - (quantity + 1) * marginBetween) / quantity
        val zoomFactor: Float = markerWidth / srcDrawable.intrinsicWidth.toFloat()
        val desiredHeight: Int = (srcDrawable.intrinsicHeight
                * if (zoomFactor < 1) 1f else zoomFactor).toInt()

        val height = when (heightMode) {
            View.MeasureSpec.EXACTLY ->
                heightSize
            View.MeasureSpec.AT_MOST ->
                Math.min(desiredHeight, heightSize)
            View.MeasureSpec.UNSPECIFIED ->
                desiredHeight
            else -> desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        currentSelectX = calculateXFromPosition(selectItemPosition)
    }

    private fun setTextPaint(paint: Paint, textAppearanceId: Int) {
        context.theme.obtainStyledAttributes(textAppearanceId, attrs).apply {
            @StyleableRes var i = 0
            paint.textSize = getDimensionPixelSize(i, 52).toFloat()
            paint.color = getColor(++i, Color.BLACK)
            paint.typeface = getResourceId(++i, DEFAULT_NOTHING).let {
                if (it != -1) {
                    ResourcesCompat.getFont(context, it)
                } else {
                    Typeface.DEFAULT
                }
            }
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.textAlign = Paint.Align.CENTER
            recycle()
        }
    }

    fun setColorRatingListener(colorRatingListener: ColorRatingListener) {
        this.mColorRatingListener = colorRatingListener
    }

    fun getCurrentColor(position: Int): Int = colorMarkerArray[position]

    fun getSelectItem(): Int = if (selectItemPosition == DEFAULT_NOTHING) 0 else selectItemPosition

    private fun getColorFromPosition(position: Int): Int {
        fun calculateColorStep(color1: Int, color2: Int) = (color1 - color2) / stepCount

        return if (position <= stepCount) {
            val stepStartRed = calculateColorStep(redColors[1], redColors[0])
            val stepStartGreen = calculateColorStep(greenColors[1], greenColors[0])
            val stepStartBlue = calculateColorStep(blueColors[1], blueColors[0])

            Color.rgb(redColors[0] + stepStartRed * position,
                    greenColors[0] + stepStartGreen * position,
                    blueColors[0] + stepStartBlue * position)
        } else {
            val stepEndRed = calculateColorStep(redColors[2], redColors[1])
            val stepEndGreen = calculateColorStep(greenColors[2], greenColors[1])
            val stepEndBlue = calculateColorStep(blueColors[2], blueColors[1])
            val factor = position - stepCount

            Color.rgb(redColors[1] + stepEndRed * factor,
                    greenColors[1] + stepEndGreen * factor,
                    blueColors[1] + stepEndBlue * factor)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        isDrawingFinish = false
        canvas?.let {
            drawDrawable(it)
            if (isTextVisibility) {
                drawText(it)
            }
        }
        isDrawingFinish = true
    }

    private fun drawDrawable(canvas: Canvas) {
        drawableArray.forEachIndexed { index, drawable ->
            changeColorDrawable(drawable, index, positionMarkerArray[index])
            drawable.setBounds(0, 0, markerWidth, canvas.height)
            if (index == 0) {
                canvas.translate(marginBetween.toFloat(), 0f)
            } else {
                canvas.translate(markerWidthWithSpace.toFloat(), 0f)
            }
            drawable.draw(canvas)
        }
        canvas.translate(-positionMarkerArray.last().toFloat(), 0f)
    }

    private fun drawText(canvas: Canvas) {
        fun draw(positionX: Int, text: String) {
            val paint: Paint = if (currentSelectX >= positionX - markerWidth / 2) {
                paintTextSelected
            } else {
                paintTextDefault
            }
            paint.getTextBounds(text.toUpperCase(resources.configuration.locale), 0, text.length, rect)
            canvas.drawText(text, positionX.toFloat(),
                    (canvas.height / 2 + rect.height() / 3).toFloat(), paint)
        }

        if (displayNames == null) {
            for (i in 0 until quantity) {
                draw(textPositionArray[i], (if (isStartZero) i else i + 1).toString())
            }
        } else {
            displayNames!!.forEachIndexed { index, s -> draw(textPositionArray[index], s) }
        }
    }

    private fun changeColorDrawable(drawable: Drawable, position: Int, positionX: Int) {
        if (positionX < currentSelectX) {
            DrawableCompat.setTint(drawable, colorMarkerArray[position])
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.MULTIPLY)
        } else {
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.DST)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.x > marginBetween) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    currentSelectX = event.x
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    selectItemPosition = findNearest(event.x, positionMarkerArray)
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findNearest(referenceVal: Float, array: IntArray): Int {
        var nearest = 0
        var value: Float = measuredWidth.toFloat()
        array.forEach {
            if (value > Math.abs(referenceVal - it)) {
                value = Math.abs(referenceVal - it)
                nearest = it
            }
        }
        return array.indexOf(nearest)
    }

    private fun calculateXFromPosition(position: Int) = if (position != DEFAULT_NOTHING) {
        positionMarkerArray[position].toFloat() + marginBetween
    } else {
        0f
    }

    override fun invalidate() {
        if (isDrawingFinish) {
            super.invalidate()
        }
    }

    fun reset() {
        selectItemPosition = DEFAULT_NOTHING
        invalidate()
    }
}