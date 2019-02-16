package com.codingblocks.whats_app_clone.Chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.codingblocks.whats_app_clone.R

class MediaAdapter(val mediaList:ArrayList<String>, val context: Context): RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MediaAdapter.MediaViewHolder {
        val layoutView= LayoutInflater.from(parent.context).inflate(R.layout.item_media,null,false)
        val mediaViewHolder= MediaViewHolder(layoutView)

        return mediaViewHolder
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        Glide.with(context).load(Uri.parse(mediaList.get(position))).into(holder.mMedia)
    }

    class MediaViewHolder(val item: View): RecyclerView.ViewHolder(item) {
        val mMedia=item.findViewById<ImageView>(R.id.media)
    }
}