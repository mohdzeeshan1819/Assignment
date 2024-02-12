package com.example.assignment.signUp

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.assignment.MainActivity
import com.example.assignment.R
import com.example.assignment.chatFolder.UsersActivity
import com.example.assignment.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private  lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = Firebase.auth
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // User is already logged in, navigate to UsersActivity
            startActivity(Intent(this, UsersActivity::class.java))
            finish() // Finish LoginActivity
        }
            binding.login.setOnClickListener(){
                val email = binding.etEmailLogin.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                signInWithEmailAndPassword(email, password)
                binding.progressBar2.visibility= View.GONE

            }
        binding.notAUser.setOnClickListener(){
            binding.progressBar2.visibility= View.VISIBLE
            startActivity(Intent(this,MainActivity::class.java))
            finish()
            binding.progressBar2.visibility= View.GONE

        }
    }



    private fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        binding.progressBar2.visibility= View.VISIBLE
                        startActivity(Intent(this,UsersActivity::class.java))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed: User not found",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
        }


}