package com.example.mymovies.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.databinding.ItemMovieBinding
import com.example.mymovies.models.response.MoviesItem
import com.example.mymovies.utils.addList
import com.example.mymovies.utils.distinctList
import com.example.mymovies.views.adapters.listener.ListenerAdapter
import com.squareup.picasso.Picasso
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ItemMovieAdapter(private val listenerAdapter: ListenerAdapter) : RecyclerView.Adapter<ItemMovieAdapter.ViewHolder>() {

    private val moviesList: MutableList<MoviesItem> = mutableListOf()
    var currentPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        currentPosition = position
        if (position <moviesList.size) {
            holder.binding.textName.text = moviesList[position].titleEnglish
            holder.binding.ratingMovie.rating = (moviesList[position].rating!!.div(2).toFloat())
            holder.binding.textRating.text = moviesList[position].rating.toString()
            holder.binding.imageMovieCover.apply {
                Picasso.get().load(moviesList[position].mediumCoverImage).into(this)
                transitionName = moviesList[position].backgroundImageOriginal
            }
            holder.binding.root.setOnClickListener {
                listenerAdapter.openMovie(moviesList[position].id!!, holder.binding.imageMovieCover)
            }
        }
    }

    fun addList(list: List<MoviesItem>) {
        this.moviesList.addList(list)
        notifyDataSetChanged()
    }

    fun updateList(list: List<MoviesItem>) {
        GlobalScope.launch { moviesList.distinctList(list) }
        notifyItemInserted(moviesList.size)
    }

    fun clearList() {
        moviesList.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = moviesList.size

    inner class ViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)
}
