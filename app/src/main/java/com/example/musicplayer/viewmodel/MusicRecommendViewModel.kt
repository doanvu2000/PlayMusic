package com.example.musicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.model.apirecommend.Item
import com.example.musicplayer.repository.SongRepository

class MusicRecommendViewModel : ViewModel() {
    var mSongRecommendLiveData: MutableLiveData<MutableList<Item>> = MutableLiveData()
    fun getSongRecommendFromAPI(id: String) {
        SongRepository.getInstance().getSongRecommend(id) { isSuccess, respone ->
            if (isSuccess) {
                mSongRecommendLiveData.postValue(respone?.data?.items as MutableList<Item>?)
            }
        }
    }
}