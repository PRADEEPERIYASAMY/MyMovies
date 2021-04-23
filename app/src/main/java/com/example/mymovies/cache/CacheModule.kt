package com.example.mymovies.cache

import android.content.Context
import androidx.room.Room
import com.mhmdawad.torrentmovies.data.source.cache.FavoriteDao
import com.mhmdawad.torrentmovies.data.source.cache.MoviesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object CacheModule {
    @Provides
    @Singleton
    fun getFavouriteDao(moviesDatabase: MoviesDatabase): FavoriteDao {
        return moviesDatabase.getFavoriteDao()
    }

    @Provides
    @Singleton
    fun getMovieDao(moviesDatabase: MoviesDatabase): MoviesDao {
        return moviesDatabase.getMoviesDao()
    }

    @Provides
    @Singleton
    fun getMovieDatabase(@ApplicationContext appContext: Context): MoviesDatabase {
        return Room.databaseBuilder(
            appContext,
            MoviesDatabase::class.java,
            "MyMovies"
        ).build()
    }
}
