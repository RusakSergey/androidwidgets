package com.sarcosuchus.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.StyleableRes
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.sarcosuchus.widgets.Constants.Common.DEFAULT_NOTHING
import java.math.BigDecimal

private const val DEFAULT_MAX_RATING = 5
private const val DEFAULT_MARGIN_BETWEEN = 10
private const val DEFAULT_PADDING = 0F

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
    private val srcDrawableDefault =
        ContextCompat.getDrawable(context, R.drawable.ic_color_rating) as Drawable
    private val rect = Rect()

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

    var srcDrawable: Drawable = srcDrawableDefault
        set(value) {
            field = value
            invalidate()
        }

    private var markerWidth: Int = srcDrawable.intrinsicWidth
        set(value) {
            field = value
            initMarkers(field)
        }

    private fun initMarkers(markerWidth: Int) {
        markerWidthWithSpace = marginBetween + markerWidth
        positionMarkerArray = IntArray(quantity) { i ->
            if (i == 0) marginBetween
            else markerWidthWithSpace * i + marginBetween
        }
        textPositionArray = IntArray(quantity) { i -> positionMarkerArray[i] + markerWidth / 2 }
        invalidate()
    }

    var isTextVisibility: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var displayNames: Array<String>? = null
        set(value) {
            field = value
            invalidate()
        }

    var maxRatingValue: Int = DEFAULT_MAX_RATING
        set(value) {
            field = value
            quantity = calculateQuantity()
        }

    var isStartZero: Boolean = false
        set(value) {
            field = value
            quantity = calculateQuantity()
        }

    var marginBetween: Int = DEFAULT_MARGIN_BETWEEN
        set(value) {
            field = value
            invalidate()
        }

    var textPaddingTop: Float = DEFAULT_PADDING
        set(value) {
            field = value.pxToDp()
            invalidate()
        }

    var textPaddingBottom: Float = DEFAULT_PADDING
        set(value) {
            field = value.pxToDp()
            invalidate()
        }

    var startColor: Int = startColorDefault
        set(value) {
            field = value
            redColors[0] = Color.red(field)
            greenColors[0] = Color.green(field)
            blueColors[0] = Color.blue(field)
            invalidate()
        }

    var middleColor: Int = middleColorDefault
        set(value) {
            field = value
            redColors[1] = Color.red(field)
            greenColors[1] = Color.green(field)
            blueColors[1] = Color.blue(field)
            invalidate()
        }

    var endColor: Int = endColorDefault
        set(value) {
            field = value
            redColors[2] = Color.red(field)
            greenColors[2] = Color.green(field)
            blueColors[2] = Color.blue(field)
            invalidate()
        }

    var textAppearanceDefault: Int = DEFAULT_NOTHING
        set(value) {
            field = value
            setTextPaint(paintTextDefault, field)
            invalidate()
        }

    var textAppearanceSelected: Int = DEFAULT_NOTHING
        set(value) {
            field = value
            setTextPaint(paintTextSelected, field)
            invalidate()
        }

    private var quantity: Int = DEFAULT_MAX_RATING + 1
        set(value) {
            field = value
            middleRatingValue = field / 2
            stepCount = BigDecimal(field / 2).setScale(0, BigDecimal.ROUND_HALF_UP).toInt()
            drawableArray = Array(field) {
                srcDrawable.constantState.newDrawable().apply {
                    mutate()
                    DrawableCompat.wrap(this)
                }
            }
            markerWidth = (width - (field + 1) * marginBetween) / quantity
            colorMarkerArray = IntArray(field) { i -> getColorFromPosition(i) }
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
            currentSelectX = calculateXFromPosition(field)
            if (field != DEFAULT_NOTHING) {
                mColorRatingListener?.onChange(field, colorMarkerArray[field])
            }
            invalidate()
        }

    init {
        attrs?.let { attributeSet ->
            val typedArray = context.obtainStyledAttributes(
                attributeSet,
                R.styleable.ColorRatingWidget, 0, 0
            )

            (0 until typedArray.indexCount)
                .map { typedArray.getIndex(it) }
                .forEach { resId ->
                    when (resId) {
                        R.styleable.ColorRatingWidget_text_visibility -> isTextVisibility = typedArray.getBoolean(resId, false)
                        R.styleable.ColorRatingWidget_display_names -> displayNames = typedArray.getTextArray(resId).map { it.toString() }.toTypedArray()
                        R.styleable.ColorRatingWidget_max_rating_value -> maxRatingValue = typedArray.getInt(resId, DEFAULT_MAX_RATING)
                        R.styleable.ColorRatingWidget_start_zero -> isStartZero = typedArray.getBoolean(resId, false)
                        R.styleable.ColorRatingWidget_margin_between -> marginBetween = typedArray.getDimensionPixelSize(resId, DEFAULT_MARGIN_BETWEEN)
                        R.styleable.ColorRatingWidget_text_padding_top -> textPaddingTop = typedArray.getDimension(resId, DEFAULT_PADDING)
                        R.styleable.ColorRatingWidget_text_padding_bottom -> textPaddingBottom = typedArray.getDimension(resId, DEFAULT_PADDING)
                        R.styleable.ColorRatingWidget_start_color -> startColor = typedArray.getColor(resId, startColorDefault)
                        R.styleable.ColorRatingWidget_middle_color -> middleColor = typedArray.getColor(resId, middleColorDefault)
                        R.styleable.ColorRatingWidget_end_color -> endColor = typedArray.getColor(resId, endColorDefault)
                        R.styleable.ColorRatingWidget_text_appearance_default -> textAppearanceDefault = typedArray.getResourceId(resId, DEFAULT_NOTHING)
                        R.styleable.ColorRatingWidget_text_appearance_selected -> textAppearanceSelected = typedArray.getResourceId(resId, DEFAULT_NOTHING)
                        R.styleable.ColorRatingWidget_drawable -> srcDrawable = typedArray.getDrawable(resId) ?: srcDrawableDefault
                    }
                }
            typedArray.recycle()
        }
        initMarkers(markerWidth)
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

    private fun Float.pxToDp(): Float = this / Resources.getSystem().displayMetrics.density

    private fun setTextPaint(paint: Paint, textAppearanceId: Int) {
        context.theme.obtainStyledAttributes(textAppearanceId, attrs).apply {
            @StyleableRes var i = 0
            paint.apply {
                textSize = getDimensionPixelSize(i, 52).toFloat()
                color = getColor(++i, Color.BLACK)
                typeface = getResourceId(++i, DEFAULT_NOTHING).let {
                    if (it != -1) {
                        ResourcesCompat.getFont(context, it)
                    } else {
                        Typeface.DEFAULT
                    }
                }
                style = Paint.Style.FILL_AND_STROKE
                textAlign = Paint.Align.CENTER
            }
            recycle()
        }
    }

    fun setColorRatingListener(colorRatingListener: ColorRatingListener) {
        this.mColorRatingListener = colorRatingListener
    }

    fun getCurrentColor(): Int? = if (selectItemPosition == DEFAULT_NOTHING) null else colorMarkerArray[selectItemPosition]

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
        fun draw(index: Int, text: String) {
            val paint: Paint = if (currentSelectX >= positionMarkerArray[index]) {
                paintTextSelected
            } else {
                paintTextDefault
            }
            paint.getTextBounds(text.toUpperCase(resources.configuration.locale), 0, text.length, rect)
            canvas.drawText(text, textPositionArray[index].toFloat(),
                (canvas.height / 2 + (rect.height() / 3 + textPaddingTop - textPaddingBottom)), paint
            )
        }

        if (displayNames == null) {
            for (i in 0 until quantity) {
                draw(i, (if (isStartZero) i else i + 1).toString())
            }
        } else {
            displayNames!!.forEachIndexed { index, s -> draw(index, s) }
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
                    currentSelectX = event.x
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