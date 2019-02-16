package com.codingblocks.whats_app_clone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.codingblocks.whats_app_clone.Chat.*
import com.codingblocks.whats_app_clone.R.id.mediaList
import com.codingblocks.whats_app_clone.R.id.messageList
import com.codingblocks.whats_app_clone.Utils.SendNotification
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main_page.*
import kotlinx.android.synthetic.main.item_media.*

class ChatActivity : AppCompatActivity() {

    lateinit var mChatAdapter: MessageAdapter
    lateinit var mMediaAdapter: MediaAdapter
    lateinit var mChatLayoutManager:RecyclerView.LayoutManager
    lateinit var mMediaLayoutManager:RecyclerView.LayoutManager
    val messagesList:ArrayList<MessageObject> = ArrayList()
    lateinit var mChatObject:ChatObject
    lateinit var mChatMessageDb:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mChatObject= intent.getSerializableExtra("chatObject") as ChatObject

        mChatMessageDb=FirebaseDatabase.getInstance().reference.child("chat").child(mChatObject.chatId).child("messages")

        send.setOnClickListener {
            sendMessage()
        }

        addMedia.setOnClickListener {
            openGallery()
        }


        initializeMedia()
        initializeMessage()
        getChatMessages()
    }

    private fun getChatMessages() {
        mChatMessageDb.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                if(dataSnapshot.exists()){
                    var text:String=" "
                    lateinit var creatorID:String
                    var mediaUrlList:ArrayList<String> = ArrayList()

                    if(dataSnapshot.child("text").getValue()!=null)
                        text=dataSnapshot.child("text").getValue().toString()
                    if(dataSnapshot.child("creator").getValue()!=null)
                        creatorID=dataSnapshot.child("creator").getValue().toString()

                    if(dataSnapshot.child("media").childrenCount>0){
                        dataSnapshot.child("media").children.forEach {
                            mediaUrlList.add(it.getValue().toString())
                        }
                    }

                    val mMessage=MessageObject(dataSnapshot.key!!, creatorID, text, mediaUrlList)
                    messagesList.add(mMessage)
                    mChatLayoutManager.scrollToPosition(messagesList.size-1)
                    mChatAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
    }

    var totalMediaUploaded=0
    val mediaIdList:ArrayList<String> = ArrayList()
    private fun sendMessage(){

            val messageId=mChatMessageDb.push().key
            val newMessageDb=mChatMessageDb.child(messageId!!)

            var newMessageMap=HashMap<String,String>()
            newMessageMap.put("creator",FirebaseAuth.getInstance().uid!!)

            if(!message.text.toString().isEmpty())
                newMessageMap.put("text",message.text.toString())

            if(!mediaUriList.isEmpty()){
                mediaUriList.forEach {
                    var mediaId=newMessageDb.child("media").push().key
                    mediaIdList.add(mediaId!!)
                    val filePath=FirebaseStorage.getInstance().reference.child("chat").child(mChatObject.chatId).child(messageId).child(mediaId)

                    val uploadTask=filePath.putFile(Uri.parse(it))

                    uploadTask.addOnSuccessListener { taskSnapshot ->

                            filePath.downloadUrl.addOnSuccessListener {it ->
                                Toast.makeText(this,"successfully uploaded",Toast.LENGTH_SHORT).show()
                                newMessageMap.put("/media/"+mediaIdList.get(totalMediaUploaded)+"/",it.toString())

                                totalMediaUploaded++
                                if(totalMediaUploaded==mediaUriList.size)
                                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap)
                            }


                     }

                }
            }
            else{
                if(!message.text.toString().isEmpty())
                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap)
            }

    }

    private fun updateDatabaseWithNewMessage(newMessageDb: DatabaseReference, newMessageMap: Map<String,String>){
        Toast.makeText(this,"media",Toast.LENGTH_SHORT)
        newMessageDb.updateChildren(newMessageMap)
        message.text=null
        mediaUriList.clear()
        mediaIdList.clear()
        mMediaAdapter.notifyDataSetChanged()

        var message:String

        if (newMessageMap.get("text")!=null)
            message=newMessageMap.get("text").toString()
        else
            message="Sent Media"

        mChatObject.userObjectArrayList.forEach {
            if(!it.uid.equals(FirebaseAuth.getInstance().uid)){
                SendNotification(message,"New Message",it.notificationKey)
            }
        }
    }

    private fun initializeMessage(){
        messageList.isNestedScrollingEnabled=false
        messageList.setHasFixedSize(false)
        mChatLayoutManager= LinearLayoutManager(applicationContext, LinearLayout.VERTICAL,false)
        messageList.layoutManager=mChatLayoutManager
        mChatAdapter= MessageAdapter(messagesList)
        messageList.adapter=mChatAdapter
    }

    val PICK_IMAGE_INTENT=1;
    val mediaUriList:ArrayList<String> = ArrayList()

    private fun initializeMedia(){
        mediaList.isNestedScrollingEnabled=false
        mediaList.setHasFixedSize(false)
        mMediaLayoutManager= LinearLayoutManager(applicationContext, LinearLayout.HORIZONTAL,false)
        mediaList.layoutManager=mMediaLayoutManager
        mMediaAdapter= MediaAdapter(mediaUriList, applicationContext)
        mediaList.adapter=mMediaAdapter
    }


    private fun openGallery(){
        val intent=Intent()
        intent.type="image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Picture(s)"), PICK_IMAGE_INTENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK){
            if(requestCode==PICK_IMAGE_INTENT) {
                if (data!!.clipData == null) {
                    mediaUriList.add(data!!.data.toString())
                } else {
                    for (i in 0..data.clipData.itemCount-1) {
                        mediaUriList.add(data.clipData.getItemAt(i).uri.toString())
                    }
                }

                mMediaAdapter.notifyDataSetChanged()

            }
        }
    }
}
