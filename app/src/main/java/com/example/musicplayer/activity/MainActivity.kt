package com.example.musicplayer.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.`object`.MusicAudio
import com.example.musicplayer.adapter.MusicAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val PERMISSION_READ = 0
    val listSong: MutableList<MusicAudio> = ArrayList()
    lateinit var musicAdapter: MusicAdapter

    companion object {
        var musicList: MutableList<MusicAudio> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        musicAdapter = MusicAdapter(listSong)
        if (checkPermission()) {
            loadSong()
        }
        rcvListSong.layoutManager = LinearLayoutManager(this)
        rcvListSong.adapter = musicAdapter
        rcvListSong.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        musicAdapter.setOnClickSongItem {
            //intent to PlayMusicActivity
            val intent = Intent(this, PlayMusicActivity::class.java)
            intent.putExtra("song", listSong[it])
            intent.putExtra("indexSong", it)
            startActivity(intent)
        }
        btnShuffle.setOnClickListener {
            val intent = Intent(this, PlayMusicActivity::class.java)
            var random = -1
            while (random < 0 || random == listSong.size) {
                random = Random.nextInt(0, listSong.size)
            }
            intent.putExtra("indexSong", random)
            startActivity(intent)
        }
        search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {


                search_bar.clearFocus()
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    var searchList: MutableList<MusicAudio> = ArrayList()
                    Log.d(TAG, "${musicList.size} ")
                    searchList = musicList.filter { song ->
                        song.name.lowercase().contains(p0!!)
                    } as MutableList<MusicAudio>
                    Log.d(TAG, "onQueryTextChange: $searchList")
                    musicAdapter.listSong = searchList
                    musicAdapter.notifyDataSetChanged()
                }

                return true
            }
        })
    }

    private fun loadSong() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor =
            contentResolver.query(uri, null, MediaStore.Audio.Media.IS_MUSIC + "!=0", null, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                listSong.add(MusicAudio(title, duration, author, Uri.parse(url)))
            } while (cursor.moveToNext())
        }
        cursor!!.close()
        musicAdapter.notifyDataSetChanged()
        musicList = listSong
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_READ
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_READ -> {
                if (grantResults.isNotEmpty() && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(this, "Please allow storage permission", Toast.LENGTH_SHORT)
                            .show()
                    } else loadSong()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayMusicActivity.isPlaying && PlayMusicActivity.musicService != null) {
            PlayMusicActivity.musicService!!.stopForeground(true)
            PlayMusicActivity.musicService!!.mediaPlayer!!.release()
            PlayMusicActivity.musicService = null
            exitProcess(1)
        }
    }
}