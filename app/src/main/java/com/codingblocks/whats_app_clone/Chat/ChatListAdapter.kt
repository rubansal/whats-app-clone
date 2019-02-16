package com.codingblocks.whats_app_clone.Chat

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.codingblocks.whats_app_clone.ChatActivity
import com.codingblocks.whats_app_clone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChatListAdapter(val chatsList:ArrayList<ChatObject>): RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ChatListViewHolder {
        val layoutView= LayoutInflater.from(parent.context).inflate(R.layout.item_chat,null,false)
        val lp= RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutView.layoutParams=lp

        val rcv= ChatListViewHolder(layoutView)
        return rcv
    }

    override fun getItemCount(): Int {
        return chatsList.size
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.title.text=chatsList[position].chatId

        holder.layout.setOnClickListener {
            val intent= Intent(it.context,ChatActivity::class.java)
            intent.putExtra("chatObject",chatsList.get(holder.adapterPosition))
            it.context.startActivity(intent)
        }
    }

    class ChatListViewHolder(val item: View): RecyclerView.ViewHolder(item) {
        val title=item.findViewById<TextView>(R.id.title)
        val layout=item.findViewById<LinearLayout>(R.id.layout)
    }
}