package com.example.mymovies.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.R
import com.example.mymovies.databinding.ItemCastBinding
import com.example.mymovies.models.CastItem
import com.example.mymovies.utils.downloadImage

class ItemCastAdapter(private val castList: List<CastItem>) : RecyclerView.Adapter<ItemCastAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.imageCast.downloadImage(castList[position].urlSmallImage, true)
        holder.binding.cardImage.setBackgroundResource(R.drawable.ic_launcher_foreground)
        holder.binding.textName.text = castList[position].name
    }

    override fun getItemCount(): Int = castList.size

    class ViewHolder(val binding: ItemCastBinding) : RecyclerView.ViewHolder(binding.root)
}
