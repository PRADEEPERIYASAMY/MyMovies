package com.example.mymovies.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mymovies.models.*
import com.example.mymovies.models.response.MoviesItem
import com.mhmdawad.torrentmovies.data.source.cache.FavoriteDao
import com.mhmdawad.torrentmovies.data.source.cache.MoviesDao

@Database(entities = [MoviesItem::class, Movie::class, FavoriteMovie::class], version = 1, exportSchema = false)
@TypeConverters(
    StringTypeConverter::class, CastTypeConverter::class,
    TorrentTypeConverter::class, TorrentsDetailsTypeConverter::class
)
abstract class MoviesDatabase : RoomDatabase() {
    abstract fun getMoviesDao(): MoviesDao
    abstract fun getFavoriteDao(): FavoriteDao
}
