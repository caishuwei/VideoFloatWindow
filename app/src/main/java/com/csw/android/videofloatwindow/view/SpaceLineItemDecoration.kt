package com.csw.android.videofloatwindow.view

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class SpaceLineItemDecoration : RecyclerView.ItemDecoration {
    private val left: Int
    private val top: Int
    private val right: Int
    private val bottom: Int
    private val color: Int
    private val drawPaint: Paint

    constructor(left: Int, top: Int, right: Int, bottom: Int, color: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        this.color = color
        drawPaint = Paint()
        drawPaint.color = color
        drawPaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(left, top, right, bottom)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val layoutManager = parent.layoutManager
        if (layoutManager != null) {
            var child: View? = null
            for (i in 0 until layoutManager.childCount) {
                child = layoutManager.getChildAt(i)
                if (child != null && !skipDraw(layoutManager.getPosition(child))) {
                    if (left > 0) c.drawRect((child.left - left).toFloat(), child.top.toFloat(), child.left.toFloat(), child.bottom.toFloat(), drawPaint)
                    if (top > 0) c.drawRect(child.left.toFloat(), (child.top - top).toFloat(), child.right.toFloat(), child.top.toFloat(), drawPaint)
                    if (right > 0) c.drawRect(child.right.toFloat(), child.top.toFloat(), (child.right + right).toFloat(), child.bottom.toFloat(), drawPaint)
                    if (bottom > 0) c.drawRect(child.left.toFloat(), child.bottom.toFloat(), child.right.toFloat(), (child.bottom + bottom).toFloat(), drawPaint)
                }
            }
        }
    }

    open fun skipDraw(position: Int): Boolean {
        return false
    }
}