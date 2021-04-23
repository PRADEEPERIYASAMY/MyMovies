package com.example.mymovies.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.databinding.ItemScreenShotBinding
import com.squareup.picasso.Picasso

class ItemScreenShotAdapter(private val screenShotList: List<String>) : RecyclerView.Adapter<ItemScreenShotAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemScreenShotBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(screenShotList[position]).into(holder.binding.imageScreenshot)
    }

    override fun getItemCount(): Int = screenShotList.size

    class ViewHolder(val binding: ItemScreenShotBinding) : RecyclerView.ViewHolder(binding.root)
}
