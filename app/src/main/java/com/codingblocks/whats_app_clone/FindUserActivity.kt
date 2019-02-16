package com.codingblocks.whats_app_clone

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.widget.LinearLayoutManager
import android.telephony.TelephonyManager
import android.widget.LinearLayout
import com.codingblocks.whats_app_clone.User.UserListAdapter
import com.codingblocks.whats_app_clone.User.UserObject
import com.codingblocks.whats_app_clone.Utils.CountryToPhonePrefix
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_find_user.*
import java.lang.String.valueOf

class FindUserActivity : AppCompatActivity() {

    lateinit var mUserListAdapter: UserListAdapter
    val usersList:ArrayList<UserObject> = ArrayList()
    val contactList:ArrayList<UserObject> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_user)

        create.setOnClickListener {
            createChat()
        }

        initializeRecyclerView()
        getContactList()
    }

    private fun createChat() {
        lateinit var key: String
        key = FirebaseDatabase.getInstance().reference.child("chat").push().key!!

        var chatInfoDb = FirebaseDatabase.getInstance().reference.child("chat").child(key).child("info")
        var mUserDb = FirebaseDatabase.getInstance().reference.child("user")

        var newChatMap = HashMap<String, String>()
        newChatMap.put("id", key)
        newChatMap.put("users/" + FirebaseAuth.getInstance().uid, true.toString())

        var validChat=false

        usersList.forEach {
            if (it.selected){
                validChat=true
                newChatMap.put("users/" + it.uid, true.toString())
                mUserDb.child(it.uid).child("chat").child(key).setValue(true)
            }
        }

        if (validChat) {
            chatInfoDb.updateChildren(newChatMap as Map<String, String>)
            mUserDb.child(FirebaseAuth.getInstance().uid!!).child("chat").child(key).setValue(true)
        }
    }

    private fun getContactList(){

        val ISOPrefix=getCountryISO()
        lateinit var name:String
        lateinit var phone:String

        val phones=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
        while (phones.moveToNext()){
            name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            phone=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            phone=phone.replace(" ","")
            phone=phone.replace("-","")
            phone=phone.replace("(","")
            phone=phone.replace(")","")

            if(!valueOf(phone.get(0)).equals("+"))
                phone=ISOPrefix+phone

            val mContact= UserObject("",name, phone)
            contactList.add(mContact)
            getUserDetails(mContact)
        }
    }

    private fun getUserDetails(mContact: UserObject) {
        val mUserDB=FirebaseDatabase.getInstance().reference.child("user")
        val query=mUserDB.orderByChild("phone").equalTo(mContact.phone)
        query.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    lateinit var phone:String
                    lateinit var name:String
                    dataSnapshot.children.forEach{
                        if (it.child("phone").getValue()!=null)
                            phone=it.child("phone").getValue().toString()
                        if(it.child("name").getValue()!=null)
                            name=it.child("phone").getValue().toString()

                        val mUser= UserObject(it.key!!,name, phone)
                        if(name.equals(phone)){
                            contactList.forEach {
                                if(it.phone.equals(mUser.phone)){
                                    mUser.name=it.name
                                }
                            }
                        }
                        usersList.add(mUser)
                        mUserListAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun getCountryISO():String{
        lateinit var iso:String
        var telephonyManager = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if(telephonyManager.networkCountryIso!=null){
            if(!telephonyManager.networkCountryIso.toString().equals(""))
                iso=telephonyManager.networkCountryIso.toString()
        }
        return CountryToPhonePrefix.getPhone(iso)
    }

    private fun initializeRecyclerView() {
        userList.isNestedScrollingEnabled=false
        userList.setHasFixedSize(false)
        val mUserListLayoutManager=LinearLayoutManager(applicationContext,LinearLayout.VERTICAL,false)
        userList.layoutManager=mUserListLayoutManager
        mUserListAdapter= UserListAdapter(usersList)
        userList.adapter=mUserListAdapter
    }
}
