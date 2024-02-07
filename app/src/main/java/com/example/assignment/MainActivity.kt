package com.example.assignment

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_EMAIL
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.assignment.databinding.ActivityMainBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.gson.Gson
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(),DatePickerDialog.OnDateSetListener {

    private lateinit var auth: FirebaseAuth
    private val REQ_ONE_TAP = 2
    private  val PREFS_FILE_NAME = "UserPrefs"
    private  val gmaildetails = "gmail"


    private var showOneTapUI = true
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private var verificationId: String? = null
    private lateinit var selectedDate: Calendar
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(34)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        auth = Firebase.auth
        auth = FirebaseAuth.getInstance()
        selectedDate = Calendar.getInstance()

        callbackManager = CallbackManager.Factory.create()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already authenticated, navigate to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish() // Finish MainActivity to prevent going back to it when pressing back button
        }


////////////////////////////
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set click listener for Google Sign-In button
        binding.googleSignInButton.setOnClickListener {
            signIn()
        }


        binding.verify.setOnClickListener {
            validation()
        }

        binding.login.setOnClickListener {
            val code = binding.otp.text.toString()

            verificationId?.let { id ->
                val credential = PhoneAuthProvider.getCredential(id, code)
                signInWithPhoneAuthCredential(credential)
                startActivity(Intent(this,HomeActivity::class.java))

            } ?: run {
                Toast.makeText(
                    this@MainActivity,
                    "Verification ID is null.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.dateOfBirthTextView.setOnClickListener {
            showDatePickerDialog()
        }
        binding.signWithgmail.setOnClickListener {
            binding.gmailDetails.visibility=View.VISIBLE
            binding.numberLogin.visibility=View.GONE
            binding.signWithnumber.visibility=View.VISIBLE
        }
        binding.signWithnumber.setOnClickListener {
            binding.gmailDetails.visibility=View.GONE
            binding.numberLogin.visibility=View.VISIBLE
            binding.signWithnumber.visibility=View.VISIBLE


        }
        binding.verifyEmail.setOnClickListener {
            gmailPassValidation()
            binding.verifyEmail.visibility=View.GONE

        }
        binding.loginEmail.setOnClickListener {
            // Retrieve the intent and its data
            val intent = intent
            val emailLink = intent.data.toString()

            // Check if the link is a sign-in with email link
            if (auth.isSignInWithEmailLink(emailLink)) {
                // Retrieve the email from the link
                val email = "mohdzeeshansmartitventures@gmail.com" // Replace this with the user's email

                // Sign in with the email link and email
                auth.signInWithEmailLink(email, emailLink)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Successfully signed in with email link!")
                            val result = task.result
                            // You can access the new user via result.getUser()
                            // Additional user info profile *not* available via:
                            // result.getAdditionalUserInfo().getProfile() == null
                            // You can check if the user is new or existing:
                            // result.getAdditionalUserInfo().isNewUser()
                            Log.d("xeeshan",task.exception?.message.toString())

                            // Proceed to the next activity, HomeActivity in your case
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish() // Finish MainActivity to prevent going back to it when pressing back button
                        } else {
                            Log.e(TAG, "Error signing in with email link", task.exception)
                            // Handle sign-in error
                            Log.d("xeeshan",task.exception?.message.toString())
                            Toast.makeText(this@MainActivity, "Error signing in with email link", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


    }


    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            this,
            this,
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Callback method when a date is set in the DatePickerDialog
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Update the selected date
        selectedDate.set(year, month, dayOfMonth)

        // Update the TextView with the selected date
        val formattedDate = "${selectedDate.get(Calendar.DAY_OF_MONTH)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(
            Calendar.YEAR
        )}"
        binding.dateOfBirthTextView.text = formattedDate.toString()
    }


    private fun saveUserDataToSharedPreferences() {
        val sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userName", binding.idEdtUserName.text.toString())
        editor.putString("userEmail", binding.gmail.text.toString())
        editor.putString("pass", binding.idEdtPassword.text.toString())
        editor.putString("phone", binding.phone.text.toString())
        // Add more data fields as needed
        editor.apply()
    }


//    else if (binding.idEdtPassword.length() <= 6 && binding.idEdtPassword.length() == 0) {
//        Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show()
//    }
    fun validation() {
        if (binding.idEdtUserName.length() <= 3) {
            Toast.makeText(this, "Please enter correct name", Toast.LENGTH_SHORT).show()
        } else if (binding.idEdtUserName.length() == 0) {
            Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show()
        } else if (binding.dateOfBirthTextView.length()==0) {
            Toast.makeText(this, "Please enter the dob", Toast.LENGTH_SHORT).show()
        }  else if (binding.phone.length() <= 9 && binding.phone.length() == 0) {
            Toast.makeText(this, "Please enter the correct number", Toast.LENGTH_SHORT).show()
        } else {
            saveUserDataToSharedPreferences()
            val phoneNumber = "+91" + binding.phone.text.toString()

            binding.otp.visibility = View.VISIBLE
            binding.login.visibility = View.VISIBLE
            binding.verify.visibility = View.GONE

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // Automatically handle verification if the SMS code can be detected
                        signInWithPhoneAuthCredential(credential)
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(
                            this@MainActivity,
                            "Verification failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(verificationId, token)
                        // Save the verification ID somewhere for later use
                        this@MainActivity.verificationId = verificationId
                    }
                }
            )
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    // Continue with your app logic, such as launching a new activity
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    // Sign in failed, display a message to the user.
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    //////


    fun gmailPassValidation() {
        val gmail=binding.gmail.text.toString().trim()
        if (binding.idEdtUserName.length() == 0) {
            Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show()
        } else if (binding.idEdtUserName.length() <= 3) {
            Toast.makeText(this, "Please enter the correct name", Toast.LENGTH_SHORT).show()
        } else if (binding.dateOfBirthTextView.length() == 0) {
            Toast.makeText(this, "Please enter the dob", Toast.LENGTH_SHORT).show()
        }else if (!gmail.contains("gmail")) { // Check if email is valid
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
        } else {
            // Email and password are valid, proceed with Firebase email/password authentication

            binding.idEdtPassword.visibility=View.VISIBLE
            binding.loginEmail.visibility=View.VISIBLE
            val email = binding.gmail.text.toString()
            val actionCodeSettings = ActionCodeSettings.newBuilder()
//            "https://example.page.link/verify-email"
                .setUrl("https://example.page.link/verify-email")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                    packageName,
                    false,
                    null /* minimumVersion */)
                .build()

            FirebaseAuth.getInstance().sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("xeeshan", "Email link sent.")
                        Log.d("xeeshan",task.exception?.message.toString())

                        // Handle email link sent successfully
                    } else {
                        Log.d("xeeshan",task.exception?.message.toString())

                        // Handle error
                    }
                }



        }
    }

    // Function to validate email format




//////////////////

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode) {
            val task = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            }
            if (task != null) {
                if (task.isSuccess) {
                    val account = task.signInAccount
                    firebaseAuthWithGoogle(account!!)
                    startActivity(Intent(this,HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val user = auth.currentUser
                val phoneNumber = user?.phoneNumber

                val sharedPreferences = getSharedPreferences(gmaildetails, Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userName",user?.displayName)
                editor.putString("userEmail", user?.email)
                editor.putString("pass", user?.providerData.toString())
                editor.putString("phone", phoneNumber)
                editor.apply()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }
}

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}