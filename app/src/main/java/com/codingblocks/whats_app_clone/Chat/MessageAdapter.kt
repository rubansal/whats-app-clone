package com.codingblocks.whats_app_clone.Chat

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.codingblocks.whats_app_clone.R
import com.stfalcon.frescoimageviewer.ImageViewer



class MessageAdapter(val messagesList:ArrayList<MessageObject>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MessageAdapter.MessageViewHolder {
        val layoutView= LayoutInflater.from(parent.context).inflate(R.layout.item_message,null,false)
        val lp= RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutView.layoutParams=lp

        val rcv= MessageAdapter.MessageViewHolder(layoutView)
        return rcv
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun onBindViewHolder(holder: MessageAdapter.MessageViewHolder, position: Int) {
        holder.mMessage.text=messagesList[position].message
        holder.mSender.text=messagesList[position].senderId

        if(messagesList.get(holder.adapterPosition).mediaUrlList.isEmpty())
            holder.mViewMedia.visibility=View.GONE
        holder.mViewMedia.setOnClickListener {
            ImageViewer.Builder(it.context, messagesList.get(holder.adapterPosition).mediaUrlList)
                    .setStartPosition(0)
                    .show()
        }
    }

    class MessageViewHolder(val item: View): RecyclerView.ViewHolder(item) {
        val mMessage=item.findViewById<TextView>(R.id.messageTextView)
        val mSender=item.findViewById<TextView>(R.id.sender)
        val layout=item.findViewById<LinearLayout>(R.id.layout)
        val mViewMedia=item.findViewById<Button>(R.id.viewMedia)
    }
}