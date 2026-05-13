package com.example.nammamela

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nammamela.data.FanWallEntity

class CommentAdapter(private var comments: List<FanWallEntity>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUser: TextView = view.findViewById(android.R.id.text1)
        val txtComment: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.txtUser.text = comment.userName
        holder.txtUser.setTextColor(android.graphics.Color.parseColor("#C19A3E"))
        holder.txtComment.text = comment.comment
        holder.txtComment.setTextColor(android.graphics.Color.WHITE)
    }

    override fun getItemCount(): Int = comments.size

    fun updateData(newComments: List<FanWallEntity>) {
        comments = newComments
        notifyDataSetChanged()
    }
}
