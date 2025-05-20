package com.example.scorochenie.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.scorochenie.R

public class DiagonalLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 4f * resources.displayMetrics.density // 4dp
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val textView = (parent as View).findViewById<TextView>(R.id.animation_text_diagonal)
        val height = textView?.measuredHeight ?: 0
        setMeasuredDimension(width, height)
        Log.d("DiagonalLineView", "Measured size: ${width}x${height}")

        // Наблюдение за изменением размеров animationTextView
        if (textView != null) {
            textView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                if (textView.measuredHeight != measuredHeight) {
                    requestLayout()
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(0f, 0f, width.toFloat(), height.toFloat(), paint)
        Log.d("DiagonalLineView", "Drawing line with size: ${width}x${height}")
    }
}