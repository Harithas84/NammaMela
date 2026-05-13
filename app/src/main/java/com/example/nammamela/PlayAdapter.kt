package com.example.nammamela

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nammamela.data.PlayEntity

class PlayAdapter(
    private var plays: List<PlayEntity>,
    private val onPlaySelected: (PlayEntity) -> Unit
) : RecyclerView.Adapter<PlayAdapter.PlayViewHolder>() {

    class PlayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPoster: ImageView = view.findViewById(R.id.itemPoster)
        val txtName: TextView = view.findViewById(R.id.itemPlayName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_play, parent, false)
        return PlayViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayViewHolder, position: Int) {
        val play = plays[position]
        holder.txtName.text = play.name
        Glide.with(holder.itemView.context)
            .load(play.posterUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .centerCrop()
            .into(holder.imgPoster)

        holder.itemView.setOnClickListener { onPlaySelected(play) }
    }

    override fun getItemCount(): Int = plays.size

    fun updateData(newPlays: List<PlayEntity>) {
        plays = newPlays
        notifyDataSetChanged()
    }
}
