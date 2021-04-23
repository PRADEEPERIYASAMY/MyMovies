package com.example.mymovies.viewmodels

import android.content.Context
import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.mymovies.models.FavoriteMovie
import com.example.mymovies.models.Movie
import com.example.mymovies.models.Result
import com.example.mymovies.models.response.MoviesItem
import com.example.mymovies.models.response.MoviesResponse
import com.example.mymovies.repository.MovieRepository
import com.example.mymovies.viewmodels.actions.MovieAction
import com.masterwok.opensubtitlesandroid.OpenSubtitlesUrlBuilder
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import com.masterwok.opensubtitlesandroid.services.OpenSubtitlesService
import kotlinx.coroutines.launch

class CommonViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : BaseViewModel<MovieAction>() {

    private var mutableListTypeMovies = MutableLiveData<List<MoviesItem>>()
    val listTypeMovies: LiveData<List<MoviesItem>>
        get() = mutableListTypeMovies

    private var mutableMovieResponse = MutableLiveData<MoviesResponse>()
    val moviesResponse: LiveData<MoviesResponse>
        get() = mutableMovieResponse

    private var mutableMovieDetails = MutableLiveData<Movie?>()
    val movieDetails: LiveData<Movie?>
        get() = mutableMovieDetails

    private var mutableListTopRatedMovies = MutableLiveData<List<MoviesItem>>()
    val listTopRatedMovies: LiveData<List<MoviesItem>>
        get() = mutableListTopRatedMovies

    private var mutableListSavedMovie = MutableLiveData<List<FavoriteMovie>>()
    val listSavedMovie: LiveData<List<FavoriteMovie>>
        get() = mutableListSavedMovie

    private var mutableIsSaved = MutableLiveData<Boolean?>()
    val isSaved: LiveData<Boolean?>
        get() = mutableIsSaved

    private val mutableSubtitleStatus = MutableLiveData<Uri>()
    val subtitleStatus: LiveData<Uri>
        get() = mutableSubtitleStatus
    private val mutableSubtitleData = MutableLiveData<Array<OpenSubtitleItem>>()
    val subtitleData: LiveData<Array<OpenSubtitleItem>>
        get() = mutableSubtitleData

    init {
        mutableIsSaved.postValue(null)
    }

    override fun doAction(action: MovieAction): Any =
        when (action) {
            is MovieAction.FetchTypeMovies -> fetchTypeMovies(action.type, action.page)
            is MovieAction.FetchQueryMovies -> fetchQueryMovies(action.query, action.page)
            is MovieAction.FetchMovieDetails -> fetchMovieDetails(action.id)
            is MovieAction.FetchTopRatedMovies -> fetchTopRatedMovies(action.page)
            is MovieAction.FetchNewMovies -> fetchNewMovies(action.page)
            is MovieAction.SaveMovie -> saveMovie(action.movie)
            is MovieAction.FetchSavedMovies -> fetchSavedMovies()
            is MovieAction.DeleteSavedMovie -> deleteSavedMovie(action.id)
            is MovieAction.CheckSavedMovie -> checkSavedMovie(action.id)
            is MovieAction.FetchMovieSubTitle -> fetchMovieSubTitle(action.movieName)
            is MovieAction.DownloadMovieSubTitle -> downloadMovieSubTitle(action.context, action.subtitle, action.filePath)
        }

    private fun fetchTypeMovies(type: String, page: Int) = launch {
        when (val result = repository.fetchTypeMovies(type, page)) {
            is Result.Value -> {
                mutableListTypeMovies.postValue(result.value!!)
            }
        }
    }

    private fun fetchQueryMovies(query: String, page: Int) = launch {
        when (val result = repository.fetchQueryMovies(query, page)) {
            is Result.Value -> {
                mutableMovieResponse.postValue(result.value!!)
            }
        }
    }

    private fun fetchMovieDetails(id: Int) = launch {
        when (val result = repository.fetchMovieDetails(id)) {
            is Result.Value -> {
                mutableMovieDetails.postValue(result.value!!)
            }
        }
    }

    private fun fetchTopRatedMovies(page: Int) = launch {
        when (val result = repository.fetchTopRatedMovies(page)) {
            is Result.Value -> {
                mutableListTopRatedMovies.postValue(result.value!!)
            }
        }
    }

    private fun fetchNewMovies(page: Int) = launch {
        when (val result = repository.fetchNewMovies(page)) {
            is Result.Value -> {
                mutableMovieResponse.postValue(result.value!!)
            }
        }
    }

    private fun saveMovie(movie: Movie) = launch {
        when (val result = repository.saveMovie(movie)) {
            is Result.Value -> {
            }
        }
    }

    private fun fetchSavedMovies() = launch {
        when (val result = repository.fetchSavedMovies()) {
            is Result.Value -> {
                mutableListSavedMovie.postValue(result.value!!)
            }
        }
    }

    private fun deleteSavedMovie(id: Int) = launch {
        when (val result = repository.deleteSavedMovie(id)) {
            is Result.Value -> {
            }
        }
    }

    private fun checkSavedMovie(id: Int) = launch {
        when (val result = repository.checkSavedMovie(id)) {
            is Result.Value -> {
                mutableIsSaved.postValue(result.value!!)
            }
        }
    }

    private fun fetchMovieSubTitle(movieName: String) = launch {
        val url = OpenSubtitlesUrlBuilder().query(movieName).build()
        when (val result = repository.fetchMovieSubTitle(OpenSubtitlesService.TemporaryUserAgent, url)) {
            is Result.Value -> {
                mutableSubtitleData.postValue(result.value!!)
            }
        }
    }

    private fun downloadMovieSubTitle(context: Context, subtitle: OpenSubtitleItem, filePath: Uri) = launch {
        when (val result = repository.downloadMovieSubTitle(context, subtitle, filePath)) {
            is Result.Value -> {
                mutableSubtitleStatus.postValue(filePath)
            }
        }
    }

    fun setSavedNull() {
        mutableIsSaved.postValue(null)
    }

    fun setMovieDetailsNull() {
        mutableMovieDetails.postValue(null)
    }
}
