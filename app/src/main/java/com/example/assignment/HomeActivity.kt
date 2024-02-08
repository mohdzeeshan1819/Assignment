package com.example.assignment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.assignment.databinding.ActivityHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.io.File

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    private val PREFS_FILE_NAME = "UserPrefs"
    private val gmaildetails = "gmail"
    private var isGoogleSignIn = true // Assume default is not Google sign-in

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        // Check if the user is signed in with Google
        auth = Firebase.auth
        val currentUser = FirebaseAuth.getInstance().currentUser
        isGoogleSignIn = currentUser != null && currentUser.providerData.any { it.providerId == "google.com" }

        if (isGoogleSignIn) {
            showGoogleData()
            deleteCache()
        } else {
            showSharedPreferencesData()
            deleteCache()
        }
        val imageUrl = intent.getStringExtra("IMAGE_URL")


        // Load the image using Glide (or any other image loading library)
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.google)
            .into(binding.imageView)

        binding.signout.setOnClickListener {
            deleteCache()
            signOutAndNavigateToMainActivity()

        }
    }

    private fun showSharedPreferencesData() {
        val sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("userName", "")
        val email = sharedPreferences.getString("userEmail", "")
        val pass = sharedPreferences.getString("pass", "")
        val phone = sharedPreferences.getString("phone", "")
        binding.name.text = name
        binding.phone.text = phone
        binding.gmail.text = email
        binding.password.text = pass
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        // Load the image using Glide (or any other image loading library)
        Glide.with(this)
            .load(imageUrl)
            .into(binding.imageView)
    }

    private fun showGoogleData() {


        val user = auth.currentUser
        val imageUrl = user?.photoUrl
        if (imageUrl != null) {
            // If the imageUrl is not null, load the image using Glide
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.facebook)
                .error(R.drawable.ic_launcher_background) // Optional: specify an error placeholder
                .into(binding.imageView)

        } else {
            // If imageUrl is null, you can set a placeholder image or hide the ImageView
            // For example, to set a placeholder image:
            Glide.with(this)
                .load(R.drawable.facebook) // Placeholder image resource
                .into(binding.imageView)
        }
        binding.name.text = user?.displayName.toString()
        binding.phone.text =  user?.phoneNumber.toString()
        binding.gmail.text =  user?.email.toString()
        binding.password.text =  user?.providerId.toString()
        Log.d("image", user?.photoUrl.toString())
    }

    private fun signOutAndNavigateToMainActivity() {
        // Clear SharedPreferences and Firebase authentication
        val prefsUser = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val prefsGoogle = getSharedPreferences(gmaildetails, Context.MODE_PRIVATE)
        FirebaseAuth.getInstance().signOut()
        prefsUser.edit().clear().apply()
        prefsGoogle.edit().clear().apply()
        deleteCache()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun deleteCache() {
        try {
            val dir: File = cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir.delete()
    }

}
