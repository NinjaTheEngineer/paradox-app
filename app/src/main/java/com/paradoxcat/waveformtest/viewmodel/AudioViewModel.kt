package com.paradoxcat.waveformtest.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paradoxcat.waveformtest.model.AudioModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val audioModel = AudioModel()

    private var rawAudioData: ByteBuffer? = null // Store the original data

    private val _waveformData = MutableLiveData<IntArray>()
    val waveformData: LiveData<IntArray>
        get() = _waveformData

    fun loadAndTransformAudioData(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val assetManager = getApplication<Application>().assets
            val rawData = audioModel.loadAudioData(assetManager, fileName)
            val transformedData = audioModel.transformRawData(rawData)
            rawAudioData = rawData // Store the original data
            _waveformData.postValue(transformedData)
        }
    }

    fun getRawAudioData(): ByteBuffer? {
        return rawAudioData
    }
}