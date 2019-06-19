package com.dominiczirbel.dementia

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.core.graphics.ColorUtils

/**
 * Wraps an [ObjectAnimator] to animate the [View.setBackgroundColor] property of the given [View] through the given
 * array of colors.
 *
 * @param view      the [View] to whose background color should be animated
 * @param colorsRes the resource ID of the array of colors to animate through
 * @param tintRes   the resource ID of the color with which to tint all the animated colors, or null to do no tinting;
 *                  default null
 * @param tintRatio the ratio by which to mix the tint; 0.0 for no tint or 1.0 to just use the tint color, or null to do
 *                  no tinting; default null
 */
class BackgroundAnimator(
    view: View,
    @ArrayRes colorsRes: Int,
    @ColorRes tintRes: Int? = null,
    tintRatio: Float? = null
) {

    private val animator: ObjectAnimator?

    init {
        var colorsArray = view.resources.getIntArray(colorsRes).toTypedArray()

        // append the first color so there is an animation back to it
        if (colorsArray.size > 1) {
            colorsArray += colorsArray[0]
        }

        if (tintRes != null && tintRatio != null && tintRatio > 0) {
            val tint = view.resources.getColor(tintRes, null)
            colorsArray = colorsArray.map { ColorUtils.blendARGB(it, tint, tintRatio) }.toTypedArray()
        }

        animator = when (colorsArray.size) {
            0 -> null
            1 -> {
                view.setBackgroundColor(colorsArray[0])
                null
            }
            else ->
                ObjectAnimator.ofObject(
                    view,
                    "backgroundColor",
                    ArgbEvaluator(),
                    *colorsArray
                ).apply {
                    repeatMode = ObjectAnimator.RESTART
                    repeatCount = ObjectAnimator.INFINITE
                    duration = DURATION_MS * colorsArray.size
                }
        }
    }

    fun pause() = animator?.pause()

    fun resume() = animator?.resume()

    fun toggleAnimation(toggle: Boolean) {
        if (toggle) {
            animator?.takeIf { !it.isRunning }?.start()
        } else {
            animator?.end()
        }
    }

    companion object {

        private const val DURATION_MS = 15_000L
    }
}
