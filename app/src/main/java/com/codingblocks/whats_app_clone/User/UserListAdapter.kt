package com.codingblocks.whats_app_clone.User

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import com.codingblocks.whats_app_clone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserListAdapter(val usersList: ArrayList<UserObject>) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): UserListViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, null, false)
        val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutView.layoutParams = lp

        val rcv = UserListViewHolder(layoutView)
        return rcv
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        holder.name.text = usersList[position].name
        holder.phone.text = usersList[position].phone

        holder.mAdd.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                usersList.get(holder.adapterPosition).selected=isChecked
            }

        })
    }

    class UserListViewHolder(val item: View) : RecyclerView.ViewHolder(item) {
        val name = item.findViewById<TextView>(R.id.name)
        val phone = item.findViewById<TextView>(R.id.phone)
        val mAdd=item.findViewById<CheckBox>(R.id.add)
        val layout = item.findViewById<LinearLayout>(R.id.layout)
    }
}