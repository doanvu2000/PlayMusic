package com.example.musicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.`object`.MusicAudioLocal
import com.example.musicplayer.repository.SongRepository

class MusicLocalViewModel:ViewModel() {
    var mMusicLocalLiveData:MutableLiveData<MutableList<MusicAudioLocal>> = MutableLiveData()
    fun getMusicLocal(){
        mMusicLocalLiveData.postValue(SongRepository.getInstance().getSongLocal())
    }
}