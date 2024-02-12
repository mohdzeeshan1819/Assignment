package com.example.assignment

import android.annotation.SuppressLint
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.assignment.chatFolder.UsersActivity
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
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.google.firestore.v1.StructuredAggregationQuery.Aggregation.Count
import com.google.gson.Gson
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(),DatePickerDialog.OnDateSetListener {

    private lateinit var auth: FirebaseAuth
    private  val PREFS_FILE_NAME = "UserPrefs"
    private  val gmaildetails = "gmail"
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private var verificationId: String? = null
    private lateinit var selectedDate: Calendar
    private lateinit var binding: ActivityMainBinding
    private val PICK_IMAGE_REQUEST = 123
    var imageUrl=""
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageUri: Uri
    private lateinit var storageRef: StorageReference
    private lateinit var mDbRef:DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        FirebaseApp.initializeApp(this);
        auth = Firebase.auth
        auth = FirebaseAuth.getInstance()
        selectedDate = Calendar.getInstance()
        storageRef = Firebase.storage.reference


        callbackManager = CallbackManager.Factory.create()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already authenticated, navigate to UsersActivity
            startActivity(Intent(this, UsersActivity::class.java))
            finish() // Finish MainActivity to prevent going back to it when pressing back button
        }

         pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data: Intent? = result.data
                if (data != null) {
                    // Get the Uri of the selected image
                    imageUri = data.data!!
                    uploadImage()
                }
            }
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set click listener for Google Sign-In button
        binding.googleSignInButton.setOnClickListener {
            signIn()
            saveUserToFirestore()

        }


        binding.verify.setOnClickListener {
            validation()
        }

        binding.login.setOnClickListener {
            val code = binding.otp.text.toString()
            verificationId?.let { id ->
                val credential = PhoneAuthProvider.getCredential(id, code)
                signInWithPhoneAuthCredential(credential)
                saveUserToFirestore()
                val intent = Intent(this@MainActivity, UsersActivity::class.java)
                intent.putExtra("IMAGE_URL", imageUrl)
                startActivity(intent)
                finish()

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



        binding.loginEmail.setOnClickListener {
            gmailPassValidation()
            val email = binding.gmail.text.toString().trim()
            val password = binding.idEdtPassword.text.toString().trim()
            signInWithEmailAndPassword(email, password)
        }

        binding.image.setOnClickListener {
            openGallery()
        }

    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }



    // Function to upload the selected image to Firebase Storage
// Function to upload the selected image to Firebase Storage
    private fun uploadImage() {
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                     imageUrl = uri.toString()
                    // Set the uploaded image URL to ImageView
                    binding.image.setImageURI(Uri.parse(imageUrl))
                    Glide.with(this)
                        .load(imageUrl)
                        .into(binding.image)
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    // Handle any errors that may occur while retrieving the download URL
                    Toast.makeText(this, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful uploads
                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
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
        editor.putString("pass", binding.dateOfBirthTextView.text.toString())
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
            val phoneNumber = binding.phone.text.toString()

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
                    val intent = Intent(this@MainActivity, UsersActivity::class.java)
                    intent.putExtra("IMAGE_URL", imageUrl)
                    startActivity(intent)
                    finish()
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
    private fun saveUserToFirestore() {
        val db = Firebase.firestore
        val database = Firebase.database


        val user = hashMapOf(
            "uid" to auth.currentUser!!.uid,
            "userImage" to imageUrl,
            "userName" to binding.idEdtUserName.text.toString().trim(),
            "userEmail" to binding.gmail.text.toString().trim(),
            "userPhone" to binding.phone.text.toString().trim(),
            "userDob" to binding.idEdtPassword.text.toString().trim()
            // Add more fields as needed
        )
        val user2 = hashMapOf(
            "uid" to auth.currentUser!!.uid,
            "userName" to binding.idEdtUserName.text.toString().trim(),
            "userEmail" to binding.gmail.text.toString().trim(),
            // Add more fields as needed
        )
        val myRef = database.getReference()
        myRef.child("Xeeshan").child(auth.currentUser!!.uid).setValue(user2)
//        mDbRef=FirebaseDatabase.getInstance().getReference()
//        mDbRef.child("zeeshan").setValue(user)
        db.collection("zeeshan")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

//    private fun addUsertoDatabase(userName:String, email:String, uid:String){
//        mDbRef=FirebaseDatabase.getInstance().getReference()
//        mDbRef.child("zeeshan").child(uid).setValue(User(userName,email,uid))
//
//    }


    //////


    fun gmailPassValidation() {
        val gmail=binding.gmail.text.toString().trim()
        if (binding.idEdtUserName.length() == 0) {
            Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show()
        } else if (binding.idEdtUserName.length() <= 3) {
            Toast.makeText(this, "Please enter the correct name", Toast.LENGTH_SHORT).show()
        } else if (binding.dateOfBirthTextView.length() == 0) {
            Toast.makeText(this, "Please enter the dob", Toast.LENGTH_SHORT).show()
        }else if (!gmail.contains("@gmail")) { // Check if email is valid
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
        } else if (binding.idEdtPassword.length()<=6) { // Check if email is valid
            Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show()
        } else{
            // Email and password are valid, proceed with Firebase email/password authentication
            saveUserDataToSharedPreferences()
            binding.idEdtPassword.visibility=View.VISIBLE
            binding.loginEmail.visibility=View.VISIBLE
        }
    }

    // Function to validate email format

    private fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        saveUserToFirestore()
                        val intent = Intent(this@MainActivity, UsersActivity::class.java)
                        intent.putExtra("IMAGE_URL", imageUrl)
                        startActivity(intent)
                        finish() // Finish MainActivity
                    } else {
                        // Sign in failed, display a message to the user.
                        Toast.makeText(this@MainActivity, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT
                        ).show()
                        Log.d("gmail",task.exception?.message.toString())
                    }
                }
        } else {
            // Display error message if email or password fields are empty
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
        }
    }

//////////////////

    private fun signIn() {

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // If the user is signed in, sign them out first
            FirebaseAuth.getInstance().signOut()
        }
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(data) }
            if (task != null) {
                if (task.isSuccess) {
                    val account = task.signInAccount
                    firebaseAuthWithGoogle(account!!)
                    val intent = Intent(this@MainActivity, UsersActivity::class.java)
                    intent.putExtra("IMAGE_URL", imageUrl)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


//    fun googledataSave(){
//        val user = auth.currentUser
//        val sharedPreferences = getSharedPreferences(gmaildetails, Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putString("userName",user?.displayName)
//        editor.putString("userEmail", user?.email)
//        editor.putString("pass", user?.providerData.toString())
//        editor.putString("phone", user?.phoneNumber.toString())
//        editor.apply()
//    }

private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val user = auth.currentUser

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