package com.example.assignment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.assignment.databinding.ActivityHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private  val PREFS_FILE_NAME = "UserPrefs"
    private  val gmaildetails = "gmail"
    private var isGoogleSignIn = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)


        if (isGoogleSignIn) {
            googledata()
        } else {
            getEnteredDataFromSharedPreferences()
        }

        binding.signout.setOnClickListener(){
            val prefsUser = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            val prefsGoogle = getSharedPreferences(gmaildetails, Context.MODE_PRIVATE)
            prefsUser.edit().clear().apply()
            prefsGoogle.edit().clear().apply()
            Firebase.auth.signOut()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun getEnteredDataFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
      val name = sharedPreferences.getString("userName", "")
       val email = sharedPreferences.getString("userEmail", "")
       val pass = sharedPreferences.getString("pass", "")
       val phone = sharedPreferences.getString("phone", "")
        binding.name.text=name
        binding.phone.text=phone
        binding.gmail.text=email
        binding.password.text=pass

    }

    private fun googledata() {
        val sharedPreferences = getSharedPreferences(gmaildetails, Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("userName", "")
        val email = sharedPreferences.getString("userEmail", "")
        val pass = sharedPreferences.getString("pass", "")
        val phone = sharedPreferences.getString("phone", "")
        binding.name.text=name
        binding.phone.text=phone
        binding.gmail.text=email
        binding.password.text=pass

    }


}