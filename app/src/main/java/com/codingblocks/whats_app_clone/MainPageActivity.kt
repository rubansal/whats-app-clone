package com.codingblocks.whats_app_clone

import android.Manifest
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import com.codingblocks.whats_app_clone.Chat.ChatListAdapter
import com.codingblocks.whats_app_clone.Chat.ChatObject
import com.codingblocks.whats_app_clone.User.UserObject
import com.codingblocks.whats_app_clone.Utils.SendNotification
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_main_page.*

class MainPageActivity : AppCompatActivity() {

    lateinit var mChatListAdapter: ChatListAdapter
    val chatsList:ArrayList<ChatObject> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        OneSignal.startInit(this).init()
        OneSignal.setSubscription(true)
        OneSignal.idsAvailable { userId, registrationId ->
            FirebaseDatabase.getInstance().reference.child("user").child(FirebaseAuth.getInstance().uid!!).child("notificationKey").setValue(userId)
        }
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)

        Fresco.initialize(this)

        findUser.setOnClickListener {
            startActivity(Intent(this,FindUserActivity::class.java))
        }

        logout.setOnClickListener {
            OneSignal.setSubscription(false)
            FirebaseAuth.getInstance().signOut()
            val i: Intent = Intent(this, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i);
            finish();
        }

        getPermissions();
        initializeRecyclerView()
        getUserChatList()
    }

    private fun getUserChatList(){
        val mUserChatDB=FirebaseDatabase.getInstance().reference.child("user").child(FirebaseAuth.getInstance().uid!!).child("chat")

        mUserChatDB.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    dataSnapshot.children.forEach {
                        val mChat=ChatObject(it.key!!)
                        var exists=false
                        chatsList.forEach {
                            if(it.chatId.equals(mChat.chatId))
                                exists=true
                        }
                        if(!exists) {
                            chatsList.add(mChat)
                            getChatData(mChat.chatId)
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getChatData(chatId: String) {
        val mChatDB=FirebaseDatabase.getInstance().reference.child("chat").child(chatId).child("info")
        mChatDB.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    var chatId=""

                    if(dataSnapshot.child("id").getValue()!=null)
                        chatId=dataSnapshot.child("id").getValue().toString()

                    dataSnapshot.child("users").children.forEach {userSnapshot ->
                        chatsList.forEach {
                            if(it.chatId.equals(chatId)){
                                var mUser=UserObject(userSnapshot.key!!)
                                it.addUserToArrayList(mUser)

                                getUserData(mUser)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun getUserData(mUser: UserObject) {
        var mUserDb=FirebaseDatabase.getInstance().reference.child("user").child(mUser.uid)
        mUserDb.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var mUser=UserObject(dataSnapshot.key)

                if(dataSnapshot.child("notificationKey").getValue()!=null)
                    mUser.notificationKey=dataSnapshot.child("notificationKey").getValue().toString()

                chatsList.forEach { mChat ->
                    mChat.userObjectArrayList.forEach {
                        if(it.uid.equals(mUser.uid))
                            it.notificationKey=mUser.notificationKey
                    }
                }
                mChatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun initializeRecyclerView() {
        chatList.isNestedScrollingEnabled=false
        chatList.setHasFixedSize(false)
        val mChatListLayoutManager= LinearLayoutManager(applicationContext, LinearLayout.VERTICAL,false)
        chatList.layoutManager=mChatListLayoutManager
        mChatListAdapter= ChatListAdapter(chatsList)
        chatList.adapter=mChatListAdapter
    }

    private fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),1)
        }
    }
}
