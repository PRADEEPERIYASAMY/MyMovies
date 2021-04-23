package com.example.mymovies.repository

import android.content.Context
import android.net.Uri
import com.example.mymovies.api.DataSource
import com.example.mymovies.cache.CacheSource
import com.example.mymovies.models.FavoriteMovie
import com.example.mymovies.models.Movie
import com.example.mymovies.models.Result
import com.example.mymovies.models.response.MoviesItem
import com.example.mymovies.models.response.MoviesResponse
import com.example.mymovies.utils.changeCategory
import com.example.mymovies.utils.convertToFavorite
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
import java.lang.Exception
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val dataSource: DataSource,
    private val cacheSource: CacheSource,
    private val subtitleService: OpenSubtitlesService
) {

    suspend fun fetchTypeMovies(type: String, page: Int): Result<List<MoviesItem>> = try {
        getNetworkCategory(type, page)
        val result = cacheSource.getCacheMoviesList(type, 20, page.times(10))
        if (result.isEmpty())
            throw IllegalArgumentException()
        Result.build { result }
    } catch (e: Exception) {
        throw e
    }

    suspend fun fetchQueryMovies(search: String, page: Int): Result<MoviesResponse> = try {
        Result.build { dataSource.searchMovie(search, page) }
    } catch (e: Exception) {
        throw e
    }

    suspend fun fetchMovieDetails(id: Int): Result<Movie> = try {
        var result: Movie? = cacheSource.getSpecificMovie(id)
        if (result == null) result = getNetworkDetails(id)
        Result.build { result }
    } catch (e: Exception) {
        throw e
    }

    suspend fun fetchTopRatedMovies(page: Int): Result<List<MoviesItem>> = try {
        var result = dataSource.rankMovies(page).data!!.movies
        if (result.isEmpty()) result = getCacheRanking(page)
        if (result.isEmpty()) throw IllegalArgumentException()
        Result.build { result }
    } catch (e: Exception) {
        throw e
    }

    suspend fun fetchNewMovies(page: Int): Result<MoviesResponse> = try {
        /*var result = dataSource.rankMovies(page).data!!.movies
        if (result.isEmpty()) result = getCacheRanking(page)
        if (result.isEmpty()) throw IllegalArgumentException()*/
        Result.build { dataSource.newMovies(page) }
    } catch (e: Exception) {
        throw e
    }

    suspend fun saveMovie(movie: Movie): Result<String> = try {
        cacheSource.saveFavMovie(movie.convertToFavorite())
        checkFavMovieExist(movie.id!!)
        Result.build { "SuccessFully saved" }
    } catch (e: Exception) {
        throw e
    }

    suspend fun fetchSavedMovies(): Result<List<FavoriteMovie>> = try {
        Result.build { cacheSource.getAllFavMovies() }
    } catch (e: Exception) {
        throw e
    }

    suspend fun deleteSavedMovie(id: Int): Result<String> = try {
        cacheSource.deleteFavMovie(id)
        Result.build { "SuccessFully Deleted" }
    } catch (e: Exception) {
        throw e
    }

    suspend fun checkSavedMovie(id: Int): Result<Boolean> = try {
        Result.build { checkFavMovieExist(id) }
    } catch (e: Exception) {
        throw e
    }

    suspend fun fetchMovieSubTitle(userAgent: String, url: String): Result<Array<OpenSubtitleItem>> = try {
        Result.build { subtitleService.search(userAgent, url) }
    } catch (e: Exception) {
        throw e
    }

    suspend fun downloadMovieSubTitle(context: Context, subtitle: OpenSubtitleItem, filePath: Uri): Result<String> = try {
        subtitleService.downloadSubtitle(context, subtitle, filePath)
        Result.build { "Success" }
    } catch (e: Exception) {
        throw e
    }
    private suspend fun checkFavMovieExist(id: Int): Boolean = cacheSource.checkFavMovieExist(id)

    private suspend fun getCacheRanking(page: Int): List<MoviesItem> = cacheSource.getRankMovies(20, page.times(10))

    private suspend fun getNetworkCategory(type: String, page: Int) {
        val result = dataSource.getMoviesCategory(type, page).data!!.movies
        if (result.isNotEmpty()) {
            deleteLastCache()
            result.changeCategory(type)
            cacheSource.saveCacheMoviesList(result)
        }
    }

    private suspend fun deleteLastCache() {
        if (firstLoad) {
            cacheSource.deleteAllCacheMovies()
            firstLoad = false
        }
    }

    private suspend fun getNetworkDetails(id: Int): Movie {
        val result = dataSource.movieDetails(id).data!!.movie!!
        if (result.cast == null) result.cast = emptyList()
        cacheSource.saveSpecificMovie(result)
        return result
    }

    companion object {
        private var firstLoad = true
    }
}
