package com.example.musicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.Song
import com.example.musicplayer.repository.SongRepository

class MusicTopViewModel : ViewModel() {

    var mSongTopLiveData: MutableLiveData<MutableList<Song>> = MutableLiveData()

    fun getTopSongFromAPI() {
        SongRepository.getInstance().getTopMusic { isSuccess, response ->
            if (isSuccess) {
                mSongTopLiveData.postValue(response?.data?.song as MutableList<Song>?)
            }
        }
    }

}