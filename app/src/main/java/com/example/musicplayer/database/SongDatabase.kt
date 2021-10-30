package com.example.musicplayer.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.lifecycle.ViewModelProvider

class SongDatabase(var context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null,
    DATABASE_VERSION
) {
    companion object {
        val DATABASE_NAME = "song.db"
        val DATABASE_VERSION = 1
        val TBL_SONG = "tbl_song"
        val ID = "id"
        val NAME = "name"
        val ARTIST = "artist"
        val DURATION = "duration"
        val URL = "url"
        val THUMB = "thumb"
        val ISONLINE = "isOnline"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblSong =
            ("CREATE TABLE $TBL_SONG ($ID TEXT, $NAME TEXT, $ARTIST TEXT, $DURATION INTEGER, $URL TEXT, " +
                    "$THUMB TEXT, $ISONLINE bit)")
        db?.execSQL(createTblSong)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_SONG")
        onCreate(db)
    }

    @SuppressLint("Range")
    fun getAllSongFavourite(): MutableList<SongFavourite> {
        val songFavouriteList: MutableList<SongFavourite> = ArrayList()
        val query = "select * from $TBL_SONG"
        val db = this.readableDatabase
        val cusor: Cursor?
        try {
            cusor = db.rawQuery(query, null)
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
        var id: String
        var name: String
        var artist: String
        var duration: Int
        var url: String
        var thumb: String
        var isOnline : Boolean
        if (cusor.moveToFirst()) {
            do {
                id = cusor.getString(cusor.getColumnIndex(ID))
                name = cusor.getString(cusor.getColumnIndex(NAME))
                artist = cusor.getString(cusor.getColumnIndex(ARTIST))
                duration = cusor.getInt(cusor.getColumnIndex(DURATION))
                url = cusor.getString(cusor.getColumnIndex(URL))
                thumb = cusor.getString(cusor.getColumnIndex(THUMB))
                isOnline = cusor.getInt(cusor.getColumnIndex(ISONLINE))>0
                songFavouriteList.add(
                    SongFavourite(
                        id,
                        name,
                        artist,
                        duration,
                        url,
                        thumb,
                        isOnline
                    )
                )
            } while (cusor.moveToNext())
        }
        return songFavouriteList
    }

    fun insertSong(song: SongFavourite): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(ID, song.id)
        contentValues.put(NAME, song.name)
        contentValues.put(ARTIST, song.artist)
        contentValues.put(DURATION, song.duration)
        contentValues.put(URL, song.url)
        contentValues.put(THUMB, song.thumb)
        contentValues.put(ISONLINE, song.isOnline)
        val success = db.insert(TBL_SONG, null, contentValues)
        db.close()
        return success
    }

    fun deleteSong(song: SongFavourite): Int {
        val db = this.writableDatabase
        val success =
            db.delete(
                TBL_SONG,
                "$ID = ? and $NAME = ? and $ARTIST = ?",
                arrayOf(song.id, song.name, song.artist)
            )
        db.close()
        return success
    }
}