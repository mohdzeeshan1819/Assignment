package com.example.assignment.chatFolder.chatActivityFolder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assignment.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ChatActivity : AppCompatActivity() {
    private lateinit var  chatRecyclerView: RecyclerView
    private lateinit var messageBox:EditText
    private lateinit var sendBtn:ImageView
    private lateinit var profile:CircleImageView
    private lateinit var username:TextView
    private lateinit var  messageAdapter: MessageAdapter
    private lateinit var messageList:ArrayList<Message>
    private lateinit var mDbRef:DatabaseReference
    var recieverRoom:String?=null
    var senderRoom:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent=intent
        val recieverUid= intent.getStringExtra("uid")
        val senderUid= FirebaseAuth.getInstance().currentUser?.uid

        mDbRef =FirebaseDatabase.getInstance().reference
        senderRoom = recieverUid+senderUid
        recieverRoom = senderUid+recieverUid

        username=findViewById(R.id.username)
        profile=findViewById(R.id.image)
        username.text=intent.getStringExtra("name")
        
        chatRecyclerView=findViewById(R.id.chatRecycler)
        sendBtn = findViewById(R.id.sendBtn)
        messageBox = findViewById(R.id.messagebox)
        messageList= ArrayList()
        messageAdapter= MessageAdapter(this,messageList)
        chatRecyclerView.layoutManager=LinearLayoutManager(this)
        chatRecyclerView.adapter=messageAdapter
        val imageUrl = intent.getStringExtra("image")
        if (imageUrl != null) {
            bind(imageUrl)
        }




    }

    override fun onResume() {
        super.onResume()
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        showchat()

        sendBtn.setOnClickListener {
            if(messageBox.length()==0){
                Toast.makeText(this,"Please type a message",Toast.LENGTH_SHORT).show()
            }else{
            val message = messageBox.text.toString()
            val messageObject = Message(message, senderUid)
            mDbRef.child("chat").child(senderRoom!!).child("messages")
                .push()
                .setValue(messageObject)
                .addOnSuccessListener {
                    mDbRef.child("chat").child(recieverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")}
        }
    }

    private fun showchat() {
        mDbRef.child("chat").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
                    }
                    Log.e("ChatActivity1", "Database error: $snapshot")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "Database error: ${error.message}")
                }
            })
    }
    fun bind(imageUrl: String) {
        // Load image from Firebase Storage using Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_profile) // Placeholder image while loading
            .error(R.drawable.ic_profile) // Error image if loading fails
            .into(profile)
    }

}