package com.example.mymovies.viewmodels.actions

import android.content.Context
import android.net.Uri
import com.example.mymovies.models.Movie
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem

sealed class MovieAction {
    data class FetchTypeMovies(val type: String, val page: Int = 1) : MovieAction()
    data class FetchQueryMovies(val query: String, val page: Int) : MovieAction()
    data class FetchMovieDetails(val id: Int) : MovieAction()
    data class FetchTopRatedMovies(val page: Int) : MovieAction()
    data class FetchNewMovies(val page: Int) : MovieAction()
    data class SaveMovie(val movie: Movie) : MovieAction()
    object FetchSavedMovies : MovieAction()
    data class DeleteSavedMovie(val id: Int) : MovieAction()
    data class CheckSavedMovie(val id: Int) : MovieAction()
    data class FetchMovieSubTitle(val movieName: String) : MovieAction()
    data class DownloadMovieSubTitle(val context: Context, val subtitle: OpenSubtitleItem, val filePath: Uri) : MovieAction()
}
