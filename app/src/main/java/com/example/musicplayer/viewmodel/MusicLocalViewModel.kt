package com.example.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.repository.SongRepository

class MusicLocalViewModel(app: Application) : AndroidViewModel(app) {
    var mMusicLocalLiveData: MutableLiveData<MutableList<MusicAudioLocal>> = MutableLiveData()
    fun getMusicLocal() {
        mMusicLocalLiveData.postValue(
            SongRepository.getInstance()
                .getSongLocal(getApplication<Application>().applicationContext)
        )
    }
}