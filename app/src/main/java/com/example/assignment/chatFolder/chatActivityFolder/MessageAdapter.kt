package com.example.assignment.chatFolder.chatActivityFolder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.assignment.R
import com.example.assignment.chatFolder.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

class MessageAdapter(val context: Context, val messageList:ArrayList<Message>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    val item_recieve = 1
    val item_send = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == item_recieve) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.recieve_message, parent, false)
            return RecieveViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.send_message, parent, false)
            return SendViewHolder(view)
        }
    }


    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderID)) {
            return item_send
    } else {
        return item_recieve
    }
}

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage=messageList[position]
        if(holder.javaClass==SendViewHolder::class.java){
            val viewHolder=holder as SendViewHolder
            holder.sendMessage.text=currentMessage.message

        }else{

            val viewHolder=holder as RecieveViewHolder
            holder.recieveMessage.text=currentMessage.message
        }
    }

    class SendViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        val sendMessage=itemView.findViewById<TextView>(R.id.sendmessage)

    }
    class RecieveViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val recieveMessage=itemView.findViewById<TextView>(R.id.recievemessage)

    }

}