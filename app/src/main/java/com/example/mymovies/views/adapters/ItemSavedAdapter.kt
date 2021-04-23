package com.example.mymovies.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.databinding.ItemSavedBinding
import com.example.mymovies.models.FavoriteMovie
import com.example.mymovies.utils.downloadImage
import com.example.mymovies.views.adapters.listener.ListenerSaved

class ItemSavedAdapter(private val listenerSaved: ListenerSaved) : RecyclerView.Adapter<ItemSavedAdapter.ViewHolder>() {

    private val savedList: MutableList<FavoriteMovie> = mutableListOf()
    var lastPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.imageMovieCover.downloadImage(savedList[position].mediumCoverImage)
        holder.binding.imageMovieCover.transitionName = savedList[position].backgroundImageOriginal
        holder.binding.textName.text = savedList[position].titleEnglish
        holder.binding.buttonMovie.tag = "liked"
        holder.binding.buttonMovie.setOnClickListener {
            if (it.tag != "liked") {
                lastPosition = position
                listenerSaved.onDeleteSavedMovie(savedList[position].id!!)
            }
        }
        holder.binding.root.setOnClickListener {
            listenerSaved.onMovieClicked(savedList[position].id!!, holder.binding.imageMovieCover)
        }
    }

    fun addSaveList(list: List<FavoriteMovie>) {
        savedList.apply {
            clear()
            addAll(list)
        }
        savedList.clear()
        savedList.addAll(list)
        savedList
        notifyItemRangeChanged(lastPosition, savedList.size)
    }

    fun getMovieCover(pos: Int): String {
        return try {
            savedList[pos].mediumCoverImage!!
        } catch (e: IndexOutOfBoundsException) {
            savedList[0].mediumCoverImage!!
        }
    }

    override fun getItemCount(): Int = savedList.size

    class ViewHolder(val binding: ItemSavedBinding) : RecyclerView.ViewHolder(binding.root)
}
