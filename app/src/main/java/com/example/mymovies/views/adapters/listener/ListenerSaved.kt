package com.example.mymovies.views.adapters.listener

import android.widget.ImageView

interface ListenerSaved {
    fun onDeleteSavedMovie(id: Int)
    fun onMovieClicked(id: Int, imageView: ImageView)
}
