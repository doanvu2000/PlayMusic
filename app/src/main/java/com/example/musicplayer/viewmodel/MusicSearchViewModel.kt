package com.example.musicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.apisearch.Song
import com.example.musicplayer.repository.SongRepository

class MusicSearchViewModel : ViewModel() {
    var mSongSearchLiveData: MutableLiveData<MutableList<Song>?> = MutableLiveData()

    fun getSongSearchFromAPI(query: String) {
        SongRepository.getInstance().getSongSearch(query) { isSuccess, respone ->
            if (isSuccess) {
                if (respone?.data?.size == 0) {
                    mSongSearchLiveData.postValue(null)
                } else
                    mSongSearchLiveData.postValue(respone!!.data[0].song as MutableList<Song>?)
            } else {
                mSongSearchLiveData.postValue(null)
            }
        }
    }

}