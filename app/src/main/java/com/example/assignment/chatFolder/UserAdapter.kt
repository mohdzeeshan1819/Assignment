package com.example.assignment.chatFolder

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.assignment.R
import com.example.assignment.chatFolder.chatActivityFolder.ChatActivity
import com.example.assignment.chatFolder.chatActivityFolder.Message
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

        // Append the time to the message body
        val currentUser = userList[position]
        holder.textname.text = currentUser.userName.toString()
        val imageUrl = currentUser.userImage.toString()
        holder.bind(imageUrl)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", currentUser.userName)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("image",currentUser.userImage)
            context.startActivity(intent)
        }
    }



    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val textname=itemView.findViewById<TextView>(R.id.textname)
        val time=itemView.findViewById<TextView>(R.id.time)
        val image=itemView.findViewById<CircleImageView>(R.id.profile)


        fun bind(imageUrl: String) {
            Log.d("UserAdapter", "Image URL: $imageUrl") // Log the image URL for debugging
            Glide.with(itemView)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile) // Placeholder image while loading
                .error(R.drawable.ic_profile) // Error image if loading fails
                .into(image)
        }

    }


}