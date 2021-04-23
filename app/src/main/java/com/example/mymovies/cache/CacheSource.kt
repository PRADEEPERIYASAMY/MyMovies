package com.example.mymovies.cache

import com.example.mymovies.models.FavoriteMovie
import com.example.mymovies.models.Movie
import com.example.mymovies.models.response.MoviesItem
import com.mhmdawad.torrentmovies.data.source.cache.FavoriteDao
import com.mhmdawad.torrentmovies.data.source.cache.MoviesDao
import javax.inject.Inject

class CacheSource @Inject constructor(private val moviesDao: MoviesDao, private val favoriteDao: FavoriteDao) {

    suspend fun getCacheMoviesList(category: String, limit: Int, page: Int): List<MoviesItem> =
        moviesDao.getAllMovies(category, limit, page)

    suspend fun saveCacheMoviesList(list: List<MoviesItem>) =
        moviesDao.saveMovies(list)

    suspend fun deleteAllCacheMovies() =
        moviesDao.deleteMoviesList()

    suspend fun getSpecificMovie(id: Int): Movie =
        moviesDao.getSpecificMovie(id)

    suspend fun saveSpecificMovie(movie: Movie) =
        moviesDao.saveSpecificMovie(movie)

    suspend fun saveFavMovie(movie: FavoriteMovie) =
        favoriteDao.saveFavoriteMovie(movie)

    suspend fun getAllFavMovies(): List<FavoriteMovie> =
        favoriteDao.getAllFavMovies()

    suspend fun deleteFavMovie(id: Int) =
        favoriteDao.deleteFavMovie(id)

    suspend fun checkFavMovieExist(id: Int) =
        favoriteDao.checkMovieExist(id)

    suspend fun getRankMovies(limit: Int, page: Int): List<MoviesItem> =
        moviesDao.getTopRankMovies(limit, page)
}
