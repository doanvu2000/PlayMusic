package com.example.musicplayer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayer.database.SongFavourite
import com.example.musicplayer.repository.SongRepository

class MusicFavouriteViewModel : ViewModel() {
    var mSongFavouriteLiveData: MutableLiveData<MutableList<SongFavourite>> = MutableLiveData()
    var listFavourite: MutableList<SongFavourite> = ArrayList()
    fun getSongFavourite(context: Context) {
        listFavourite = SongRepository.getInstance().getSongFavourite(context)
        mSongFavouriteLiveData.postValue(listFavourite)
    }

    fun deleteSongFavourite(songFavourite: SongFavourite) {
        var index = -1
        for (i in 0 until listFavourite.size) {
            if (listFavourite[i].id == songFavourite.id && listFavourite[i].name == songFavourite.name && listFavourite[i].artist == songFavourite.artist) {
                index = i
            }
        }
        try {
            listFavourite.removeAt(index)
        }catch (ex:Exception){

        }
        mSongFavouriteLiveData.postValue(listFavourite)
    }
}