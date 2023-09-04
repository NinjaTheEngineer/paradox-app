package com.paradoxcat.waveformtest.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.nio.ByteBuffer
import kotlin.math.pow

/**
 * Draw all samples as small red circles and connect them with straight green lines.
 * All functionality assumes that provided data has only 1 channel, 44100 Hz sample rate, 16-bits per sample, and is
 * already without WAV header.
 */
class WaveformSlideBar(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        const val LEFT_RIGHT_PADDING = 50.0f
        const val TOP_BOTTOM_PADDING = 50.0f
        const val SAMPLE_DOT_RADIUS = 2.0f
        private val MAX_VALUE = 2.0f.pow(16.0f) - 1 // max 16-bit value
        val INV_MAX_VALUE = 1.0f / MAX_VALUE // multiply with this to get % of max value
    }

    private var sampleDistance: Float = 0f
    private var maxAmplitude: Float = 0f
    private var amplitudeScaleFactor: Float = 0f

    private val linePaint = Paint()
    private val sampleDotPaint = Paint()
    private lateinit var waveForm: IntArray

    // Playback-related properties
    private var isPlaying = false
    private var playbackPosition: Float = 0.0f
    private var playbackMarkerX: Float = 0.0f

    init {
        linePaint.color = Color.rgb(0, 255, 0)
        sampleDotPaint.color = Color.rgb(255, 0, 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calculate amplitude scale factor
        val centerY = height / 2.0f
        maxAmplitude = centerY - TOP_BOTTOM_PADDING
        amplitudeScaleFactor = maxAmplitude / MAX_VALUE

        // Draw waveform
        if (::waveForm.isInitialized) {
            var prevX = LEFT_RIGHT_PADDING
            var prevY = centerY - waveForm[0] * amplitudeScaleFactor
            for (i in 1 until waveForm.size) {
                val x = LEFT_RIGHT_PADDING + i * sampleDistance
                val y = centerY - waveForm[i] * amplitudeScaleFactor
                canvas.drawLine(prevX, prevY, x, y, linePaint)
                prevX = x
                prevY = y
            }
        }

        // Draw playback position marker
        if (isPlaying) {
            canvas.drawLine(playbackMarkerX, 0f, playbackMarkerX, height.toFloat(), linePaint)
        }
    }

    fun setData(data: IntArray) {
        waveForm = data
        sampleDistance = (width - LEFT_RIGHT_PADDING * 2) / (waveForm.size - 1)
        invalidate()
    }

    fun updatePlaybackPosition(position: Float) {
        playbackPosition = position
        playbackMarkerX = LEFT_RIGHT_PADDING + position * (width - LEFT_RIGHT_PADDING * 2)
        invalidate()
    }

    fun togglePlayback() {
        isPlaying = !isPlaying
        invalidate()
    }

    private fun isTouchWithinWaveform(x: Float, y: Float): Boolean {
        return x >= LEFT_RIGHT_PADDING && x <= width - LEFT_RIGHT_PADDING && y >= 0f && y <= height.toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (isTouchWithinWaveform(event.x, event.y)) {
                    val position = (event.x - LEFT_RIGHT_PADDING) / (width - LEFT_RIGHT_PADDING * 2)
                    updatePlaybackPosition(position)
                }
                return true
            }
        }
        return false
    }
}