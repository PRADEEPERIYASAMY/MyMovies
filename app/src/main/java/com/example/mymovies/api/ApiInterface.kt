package com.example.mymovies.api

import com.example.mymovies.models.MovieDetails
import com.example.mymovies.models.response.MoviesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET(Routes.LIST_MOVIES.plus("?sort_by=download_count&order_by=desc&limit=50"))
    suspend fun searchMovie(
        @Query("query_term") search: String,
        @Query("page") page: Int
    ): MoviesResponse

    @GET(Routes.LIST_MOVIES.plus("?sort_by=download_count&order_by=desc&limit=50"))
    suspend fun getMoviesByCategory(
        @Query("genre") category: String,
        @Query("page") page: Int
    ): MoviesResponse

    @GET(Routes.MOVIE_DETAILS.plus("?with_images=true&with_cast=true&limit=50"))
    suspend fun getMovieDetails(
        @Query("movie_id") id: Int
    ): MovieDetails

    @GET(Routes.LIST_MOVIES.plus("sort_by=rating&limit=50"))
    suspend fun getMoviesByRank(
        @Query("page") page: Int
    ): MoviesResponse

    @GET(Routes.LIST_MOVIES.plus("sort_by=date_added&order_by=desc&limit=50"))
    suspend fun getMoviesByDate(
        @Query("page") page: Int
    ): MoviesResponse
}
