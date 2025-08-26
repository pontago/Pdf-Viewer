package com.rajat.pdfviewer

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sqrt

class ZoomableLinearLayoutManager(
    context: Context,
    private val scaleFactorProvider: () -> Float
) : LinearLayoutManager(context, VERTICAL, false) {

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        val scaleFactor = scaleFactorProvider()
        val adjustedDy = (dy / scaleFactor).toInt()
        return super.scrollVerticallyBy(adjustedDy, recycler, state)
    }

    fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        position: Int,
        offset: Int
    ) {
        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                super.onTargetFound(targetView, state, action)
                val dx = calculateDxToMakeVisible(targetView, horizontalSnapPreference)
                val dy = calculateDyToMakeVisible(targetView, SNAP_TO_START)

                val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
                val time = calculateTimeForDeceleration(distance)
                if (time > 0) {
                    action.update(-dx, -dy - offset, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return 10f / displayMetrics.densityDpi
            }
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }
}