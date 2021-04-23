package com.example.mymovies.views.adapters.listener

import android.widget.ImageView

interface ListenerAdapter {
    fun itemClicked(pos: Int)
    fun openMovie(movieID: Int, imageView: ImageView)
}
