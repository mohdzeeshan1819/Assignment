package com.example.assignment.chatFolder.chatActivityFolder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assignment.R
import com.example.assignment.chatFolder.Users
import com.example.assignment.chatFolder.UsersActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val CHANNEL_ID = "default_channel_id"


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
        FirebaseApp.initializeApp(this)


        val intent=intent
        val recieverUid= intent.getStringExtra("uid").toString()
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
        val recieverUid= intent.getStringExtra("uid").toString()
        super.onResume()
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        val senderName = FirebaseAuth.getInstance().currentUser?.displayName.toString()
        showchat()

        sendBtn.setOnClickListener {
            val message = messageBox.text.toString()

            if(messageBox.length()==0){
                Toast.makeText(this,"Please type a message",Toast.LENGTH_SHORT).show()
            }else{
            val messageObject = Message(message, senderUid)
            mDbRef.child("chat").child(senderRoom!!).child("messages")
                .push()
                .setValue(messageObject)
                .addOnSuccessListener {
                    mDbRef.child("chat").child(recieverRoom!!).child("messages").push()
                        .setValue(messageObject)
                }
            messageBox.setText("")}
            sendNotificationToRecipient(recieverUid, senderName, message)

        }
    }

    private fun sendNotificationToRecipient(recieverUid: String, senderName: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, UsersActivity::class.java)

        intent.putExtra("senderName", senderName)
        intent.putExtra("message", message)

        val pendingIntent = PendingIntent.getActivity(applicationContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get the current time
        val currentTime = getCurrentTime()

        // Append the time to the message body
        val messageWithTime = "$message\n Received at: $currentTime"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "1"
            val description = "Your channel description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, description, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.sound
            notificationManager.createNotificationChannel(notificationChannel)
            Log.d("name", senderName.toString())
            Log.d("uid", recieverUid.toString())

            // Build the notification with the specified channel ID
            val builder = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_logo))
                .setContentIntent(pendingIntent)
                .setContentTitle("New Message from $senderName")
                .setContentText(messageWithTime)
                .setAutoCancel(true) // Dismisses the notification when clicked
            notificationManager.notify(1, builder.build())
        } else {
            // For devices with Android versions below Oreo, create the notification without a channel
            val builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_logo))
                .setContentIntent(pendingIntent)
                .setContentTitle("New Message from $senderName")
                .setContentText(messageWithTime)
                .setAutoCancel(true)
            notificationManager.notify(1, builder.build())
        }
    }


    private fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(currentTime)
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