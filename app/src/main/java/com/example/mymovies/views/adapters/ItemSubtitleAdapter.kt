package com.example.mymovies.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.databinding.ItemSubtitleBinding
import com.example.mymovies.views.adapters.listener.ListenerSubtitle
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem

class ItemSubtitleAdapter(private val subtitleList: Array<OpenSubtitleItem>, private val listenerSubtitle: ListenerSubtitle) : RecyclerView.Adapter<ItemSubtitleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSubtitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textSubTitle.text = subtitleList[position].SubFileName
        holder.binding.textSubTitleLanguage.text = subtitleList[position].LanguageName
        holder.binding.root.setOnClickListener {
            listenerSubtitle.onSubtitleClicked(subtitleList[position])
        }
    }

    override fun getItemCount(): Int = 0

    class ViewHolder(val binding: ItemSubtitleBinding) : RecyclerView.ViewHolder(binding.root)
}
