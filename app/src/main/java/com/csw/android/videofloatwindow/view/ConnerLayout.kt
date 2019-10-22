package com.csw.android.videofloatwindow.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.util.ScreenInfo

/**
 * 圆角布局，可设置4个圆角大小
 */
class ConnerLayout : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var connerRadius: Int

    private val maskPaint: Paint
    private var sourcesBitmap: Bitmap? = null
    private var sourcesCanvas: Canvas? = null

    private val destPath = Path()

    init {
        connerRadius = ScreenInfo.dp2Px(10f)

        //遮罩画笔，设置图象叠加模式SRC_IN，先绘制控件内容，再用此画笔绘制图案，会自动截取相交的内容图
        maskPaint = Paint()
        maskPaint.isAntiAlias = true
        maskPaint.color = Color.RED
        maskPaint.style = Paint.Style.FILL_AND_STROKE
        maskPaint.strokeWidth = 0f
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.let { c ->
            c.drawPath(destPath, maskPaint)
//            val sB = sourcesBitmap
//            val sC = sourcesCanvas
//            if (sB != null && sC != null) {
//                //绘制本控件及子视图内容作为源图
//                super.dispatchDraw(sC)
//                if (!destPath.isEmpty) {
//                    //叠加目标图取源图重合部分
//                    sC.drawPath(destPath, maskPaint)
//                }
//                c.drawBitmap(sB, 0f, 0f, null)
//                return
//            }
        }
        super.dispatchDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateSrcBitmap()
        updateDestPath()
    }

    private fun updateDestPath() {
        destPath.reset()
        if (width > 0 && height > 0) {
            destPath.moveTo(0f, 0f + connerRadius)
            destPath.quadTo(0f, 0f, 0f + connerRadius, 0f)
            destPath.lineTo(width - connerRadius.toFloat(), 0f)
            destPath.quadTo(width.toFloat(), 0f, width.toFloat(), 0f + connerRadius)
            destPath.lineTo(width.toFloat(), height - connerRadius.toFloat())
            destPath.quadTo(width.toFloat(), height.toFloat(), width - connerRadius.toFloat(), height.toFloat())
            destPath.lineTo(0f + connerRadius, height.toFloat())
            destPath.quadTo(0f, height.toFloat(), 0f, height - connerRadius.toFloat())
            destPath.close()
        }
    }

    private fun updateSrcBitmap() {
        sourcesBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        sourcesBitmap = null
        sourcesCanvas = null
        if (width > 0 && height > 0) {
            sourcesBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            sourcesCanvas = Canvas(sourcesBitmap)
        }
    }
}