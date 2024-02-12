package com.example.assignment.chatFolder.chatActivityFolder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

class ChatActivity : AppCompatActivity() {
    private lateinit var  chatRecyclerView: RecyclerView
    private lateinit var messageBox:EditText
    private lateinit var sendBtn:ImageView
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
        username.text=intent.getStringExtra("name")
        
        chatRecyclerView=findViewById(R.id.chatRecycler)
        sendBtn = findViewById(R.id.sendBtn)
        messageBox = findViewById(R.id.messagebox)
        messageList= ArrayList()
        messageAdapter= MessageAdapter(this,messageList)
        chatRecyclerView.layoutManager=LinearLayoutManager(this)
        chatRecyclerView.adapter=messageAdapter

      showchat()
      sendBtn.setOnClickListener{
          val message=messageBox.text.toString()
          val messageObject = Message(message,senderUid)
          mDbRef.child("chat").child(senderRoom!!).child("messages")
              .push()
              .setValue(messageObject)
              .addOnSuccessListener {
                  mDbRef.child("chat").child(recieverRoom!!).child("messages").push()
                      .setValue(messageObject)
              }
          messageBox.setText("")
          chatRecyclerView.smoothScrollToPosition(messageList.size - 1)

      }

        // Replace "path_to_your_gif" with the actual path of your GIF file
        val gifFile = File("path_to_your_gif")

// Create the share intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/gif"

// If the GIF file is saved locally
        val uri = FileProvider.getUriForFile(this, "com.example.myapp.fileprovider", gifFile)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

// Add a message to accompany the shared GIF (optional)
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome GIF!")

// Start the activity to share the GIF
        startActivity(Intent.createChooser(shareIntent, "Share GIF via"))





    }
    fun showchat(){
        mDbRef.child("chat").child(senderRoom!!).child("messages")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for(postSnapshot in snapshot.children){
                        val message=postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                    chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
                    Log.e("ChatActivity", "Database error: ${snapshot}")

                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "Database error: ${error.message}")

                }

            })
    }
}