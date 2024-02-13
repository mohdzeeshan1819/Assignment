package com.example.assignment.chatFolder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.MainActivity
import com.example.assignment.R
import com.example.assignment.chatFolder.chatActivityFolder.Message
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UsersActivity : AppCompatActivity() {
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList:ArrayList<Users>
    private lateinit var messageList:ArrayList<Message>

    private lateinit var adapter:UserAdapter
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        FirebaseApp.initializeApp(this)

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().getReference("Xeeshan")
        userList = ArrayList()
        adapter = UserAdapter(this, userList)
        userRecyclerView = findViewById(R.id.userView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        val signout = findViewById<TextView>(R.id.signout)
        signout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        mDbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(Users::class.java)
                    if (mAuth.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
                Log.d("xeeshan2",  mAuth.uid.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UsersActivity", "Database error: ${error.message}")
            }
        })
    }
}