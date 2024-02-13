package com.example.assignment.notification

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.assignment.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val CHANNEL_ID = 1

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received notification
        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Message Body: ${it.body}")
            // Display the notification to the user
            // You can use NotificationCompat.Builder to build and show the notification
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }
}

