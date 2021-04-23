package com.example.mymovies.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.R
import com.example.mymovies.databinding.ItemMovieTypeBinding
import com.example.mymovies.models.response.MoviesItem
import com.example.mymovies.utils.constants.Constants
import com.example.mymovies.views.adapters.listener.ListenerAdapter

class ItemMovieTypeAdapter : RecyclerView.Adapter<ItemMovieTypeAdapter.ViewHolder>() {

    private var mutableSelectedIndex = MutableLiveData<String>()
    val selectedIndex: LiveData<String>
        get() = mutableSelectedIndex

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        mutableSelectedIndex.postValue(Constants.typeList[0])
        return ViewHolder(ItemMovieTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textType.text = Constants.typeList[position]
        holder.binding.root.setOnClickListener {
            mutableSelectedIndex.postValue(Constants.typeList[position])
        }
        holder.binding.cardMovieType.setCardBackgroundColor(context.getColor(R.color.green))
    }

    fun reset() {
        mutableSelectedIndex.postValue(Constants.typeList[0])
    }

    override fun getItemCount(): Int = Constants.typeList.size

    class ViewHolder(val binding: ItemMovieTypeBinding) : RecyclerView.ViewHolder(binding.root)
}
