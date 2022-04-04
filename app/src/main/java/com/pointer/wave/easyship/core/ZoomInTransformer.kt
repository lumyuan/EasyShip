package com.pointer.wave.easyship.core

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs


class ZoomInTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, pos: Float) {
        val scale = if (pos < 0) pos + 1f else abs(1f - pos)
        page.scaleX = scale
        page.scaleY = scale
        page.pivotX = page.width * 0.2f
        page.pivotY = page.height * 0.2f
        page.alpha = if (pos < -1f || pos > 1f) 0f else 1f - (scale - 1f)
    }
}