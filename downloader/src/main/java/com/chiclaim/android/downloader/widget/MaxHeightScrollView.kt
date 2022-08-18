package com.chiclaim.android.downloader.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import com.chiclaim.android.downloader.R

class MaxHeightScrollView : ScrollView {

    private var maxHeight: Int = -1

    constructor(context: Context) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(attrs)
    }

    private fun defaultMaxHeight() = context.resources.displayMetrics.heightPixels / 2

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
    }

    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs != null) {
            val attr = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
            maxHeight = attr.getDimensionPixelSize(R.styleable.MaxHeightScrollView_maxHeight, -1)
            attr.recycle()
            maxHeight
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (maxHeight == -1) maxHeight = defaultMaxHeight()
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}