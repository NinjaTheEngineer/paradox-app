package com.paradoxcat.waveformtest

import android.annotation.SuppressLint
import android.content.res.AssetFileDescriptor
import android.media.AudioFormat
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.paradoxcat.waveformtest.view.WaveformSlideBar
import com.paradoxcat.waveformtest.viewmodel.AudioViewModel
import com.paradoxcat.waveformtest.waveviewer.databinding.ActivityMainBinding
import java.io.IOException
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    val EXAMPLE_AUDIO_FILE_NAME = "whistle_mono_44100Hz_16bit.wav" // small enough to load
    private lateinit var _binding: ActivityMainBinding
    private lateinit var viewModel: AudioViewModel
    private var mediaPlayer: MediaPlayer? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        viewModel = ViewModelProvider(this)[AudioViewModel::class.java]
        mediaPlayer = MediaPlayer()

        _binding.playButton.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            } else {
                mediaPlayer?.start()
            }
            _binding.waveformView.togglePlayback()
        }

        _binding.waveformView.setOnTouchListener { _, event ->
            _binding.waveformView.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val position = (event.x - WaveformSlideBar.LEFT_RIGHT_PADDING) /
                        (_binding.waveformView.width - WaveformSlideBar.LEFT_RIGHT_PADDING * 2)
                mediaPlayer?.seekTo((mediaPlayer?.duration?.times(position))?.toInt() ?: 0)
            }
            true
        }

        mediaPlayer?.setOnCompletionListener {
            _binding.playButton.isSelected = false
            _binding.waveformView.togglePlayback()
            _binding.waveformView.updatePlaybackPosition(0f)
        }
        mediaPlayer?.setOnSeekCompleteListener {
            if (mediaPlayer?.isPlaying == true) {
                _binding.waveformView.togglePlayback()
                _binding.waveformView.updatePlaybackPosition(
                    mediaPlayer?.currentPosition?.toFloat()?.div(mediaPlayer?.duration ?: 1) ?: 0f
                )
            }
        }

        viewModel.loadAndTransformAudioData(EXAMPLE_AUDIO_FILE_NAME)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}