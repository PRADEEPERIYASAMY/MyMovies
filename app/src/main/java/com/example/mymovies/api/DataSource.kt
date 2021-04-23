package com.example.mymovies.api

import com.example.mymovies.models.MovieDetails
import com.example.mymovies.models.response.MoviesResponse
import javax.inject.Inject

class DataSource @Inject constructor(private val apiService: ApiInterface) {

    suspend fun searchMovie(search: String, page: Int): MoviesResponse =
        apiService.searchMovie(search, page)

    suspend fun getMoviesCategory(category: String, page: Int): MoviesResponse =
        apiService.getMoviesByCategory(category, page)

    suspend fun movieDetails(id: Int): MovieDetails =
        apiService.getMovieDetails(id)

    suspend fun rankMovies(page: Int): MoviesResponse =
        apiService.getMoviesByRank(page)

    suspend fun newMovies(page: Int): MoviesResponse =
        apiService.getMoviesByDate(page)
}
