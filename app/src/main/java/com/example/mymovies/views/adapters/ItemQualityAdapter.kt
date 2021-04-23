package com.example.mymovies.views.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.R
import com.example.mymovies.databinding.ItemQualityBinding
import com.example.mymovies.models.TorrentsDetails
import com.example.mymovies.utils.formatText
import com.example.mymovies.utils.showToast
import com.example.mymovies.views.adapters.listener.ListenerQuality

class ItemQualityAdapter(
    private val torrents: List<TorrentsDetails>,
    private val magnetLinks: MutableList<String>,
    private val type:String,
    private val name: String,
    private val listener: ListenerQuality
) : RecyclerView.Adapter<ItemQualityAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(ItemQualityBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            textQuality.formatText(R.string.movieQualityType, torrents[position].type, torrents[position].quality)
            textMagnetLink.text = magnetLinks[position]
            root.setOnClickListener {
                if (type == "M"){
                    val clipboard: ClipboardManager? = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("MOVIE_LINK", magnetLinks[position])
                    clipboard!!.setPrimaryClip(clip)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(magnetLinks[position]))
                    context.startActivity(intent)
                }else if(type=="P"){
                    listener.selectQuality(torrents[position].url!!, name)
                }
            }
        }
    }

    override fun getItemCount(): Int = torrents.size

    class ViewHolder(val binding: ItemQualityBinding) : RecyclerView.ViewHolder(binding.root)
}
