package com.example.mymovies.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.databinding.ItemTopRatedBinding
import com.example.mymovies.models.response.MoviesItem
import com.example.mymovies.utils.addList
import com.example.mymovies.utils.distinctList
import com.example.mymovies.utils.downloadImage
import com.example.mymovies.views.adapters.listener.ListenerAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ItemTopRatedAdapter(private var listenerAdapter: ListenerAdapter) : RecyclerView.Adapter<ItemTopRatedAdapter.ViewHolder>() {

    private val moviesList: MutableList<MoviesItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTopRatedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            textRating.text = moviesList[position].rating.toString()
            ratingMovie.rating = (moviesList[position].rating?.div(2))?.toFloat()!!
            textName.text = moviesList[position].titleEnglish
            textSort.text = if (position > 98) "99+" else (position + 1).toString().format(2)
            textType.text = moviesList[position].genres?.get(0)
            textYear.text = moviesList[position].year.toString()
            imageMovieBackground.downloadImage(moviesList[position].mediumCoverImage)
            imageMovieBackground.transitionName = moviesList[position].backgroundImageOriginal
            root.setOnClickListener {
                listenerAdapter.openMovie(moviesList[position].id!!, imageMovieBackground)
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

    override fun getItemCount(): Int = moviesList.size

    class ViewHolder(val binding: ItemTopRatedBinding) : RecyclerView.ViewHolder(binding.root)
}
