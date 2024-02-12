package com.example.assignment.chatFolder

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.example.assignment.chatFolder.chatActivityFolder.ChatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class UserAdapter(val context: Context, val userList:ArrayList<Users>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
         val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout,parent,false)
        return  UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size

    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser=userList[position]
        holder.textname.text=currentUser.userName.toString()
        holder.itemView.setOnClickListener(){
            val intent= Intent(context,ChatActivity::class.java)
            intent.putExtra("name", currentUser.userName)
            intent.putExtra("uid",currentUser.uid)
            context.startActivity(intent)
        }

    }
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textname=itemView.findViewById<TextView>(R.id.textname)
    }

}