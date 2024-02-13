package com.example.assignment

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.assignment.chatFolder.UsersActivity
import com.example.assignment.databinding.ActivityMainBinding
import com.example.assignment.signUp.LoginActivity
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(),DatePickerDialog.OnDateSetListener {

    private lateinit var auth: FirebaseAuth
    private  val PREFS_FILE_NAME = "UserPrefs"
    private  val gmaildetails = "gmail"
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest
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

        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }


        binding.verify.setOnClickListener {
            validation()
        }

        binding.login.setOnClickListener {
            val code = binding.otp.text.toString()
            verificationId?.let { id ->
                val credential = PhoneAuthProvider.getCredential(id, code)
                signInWithPhoneAuthCredential(credential)
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
            binding.signWithgmail.visibility=View.GONE
            binding.or.visibility=View.GONE
            binding.signWithnumber.visibility=View.VISIBLE
        }
        binding.signWithnumber.setOnClickListener {
            binding.gmailDetails.visibility=View.GONE
            binding.numberLogin.visibility=View.VISIBLE
            binding.verify.visibility=View.VISIBLE
            binding.or.visibility=View.GONE
            binding.phone.visibility=View.VISIBLE
            binding.signWithnumber.visibility=View.GONE
            binding.signWithgmail.visibility=View.VISIBLE
        }



        binding.loginEmail.setOnClickListener {
            gmailPassValidation()
        }

        binding.image.setOnClickListener {
            openGallery()
        }
        binding.alreadyUser.setOnClickListener(){
            startActivity(Intent(this@MainActivity,LoginActivity::class.java))
            finish()
        }

    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun uploadImage() {
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                     imageUrl = uri.toString()
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

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        // Update the selected date
        selectedDate.set(year, month, dayOfMonth)

        // Update the TextView with the selected date
        val formattedDate = "${selectedDate.get(Calendar.DAY_OF_MONTH)}-${selectedDate.get(Calendar.MONTH) + 1}-${selectedDate.get(
            Calendar.YEAR
        )}"
        binding.dateOfBirthTextView.text = formattedDate.toString()
    }

    fun getCurrentTime(): String {

        val currentTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(currentTime)
    }


    private fun saveUserDataToSharedPreferences() {
        val time=getCurrentTime().toString()
        val sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("time",time.toString())
        editor.putString("userName", binding.idEdtUserName.text.toString())
        editor.putString("userEmail", binding.gmail.text.toString())
        editor.putString("userAddress", binding.address.text.toString())
        editor.putString("DOB", binding.dateOfBirthTextView.text.toString())
        editor.putString("phone", binding.phone.text.toString())
        // Add more data fields as needed
        editor.apply()
    }

    fun validation() {
        if (binding.idEdtUserName.length() == 0) {
            Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show()
        } else if (binding.idEdtUserName.length() <= 3) {
            Toast.makeText(this, "Please enter the correct name", Toast.LENGTH_SHORT).show()
        }else  if (binding.address.length() == 0) {
        Toast.makeText(this, "Please enter the address", Toast.LENGTH_SHORT).show()
    } else if (binding.address.length() <= 3) {
        Toast.makeText(this, "Please enter the correct address", Toast.LENGTH_SHORT).show()
    }  else if (binding.dateOfBirthTextView.length()==0) {
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
                    saveUserToFirestoreWithPhone()
                    val intent = Intent(this@MainActivity, UsersActivity::class.java)
                    intent.putExtra("IMAGE_URL", imageUrl)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserTofirebaseGoogle(){
        val time = getCurrentTime().toString()

        val user = auth.currentUser
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
        val database = Firebase.database

        val user2 = hashMapOf(
            "lastActiveTime" to time,
            "uid" to user!!.uid,
            "userName" to user.displayName,
            "userEmail" to user.email,
            "userImage" to imageUrl,
            "googleUserId" to googleSignInAccount?.id
        )
        val myRef = database.getReference()
        myRef.child("Xeeshan").child(auth.currentUser!!.uid).setValue(user2)
    }

    private fun saveUserToFirestoreWithPhone() {
        val time = getCurrentTime().toString()
        val db = Firebase.firestore
        val database = Firebase.database
        val user = hashMapOf(
            "lastActiveTime" to time,
            "uid" to auth.currentUser!!.uid,
            "userImage" to imageUrl,
            "userName" to binding.idEdtUserName.text.toString().trim(),
            "userAddress" to binding.address.text.toString().trim(),
            "userPhone" to binding.phone.text.toString().trim(),
            "userDob" to binding.idEdtPassword.text.toString().trim()
            // Add more fields as needed
        )
        val myRef = database.getReference()
        myRef.child("Xeeshan").child(auth.currentUser!!.uid).setValue(user)
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
    private fun saveUserToFirestore() {
        val time = getCurrentTime().toString()
        val db = Firebase.firestore
        val database = Firebase.database
        val user = hashMapOf(
            "lastActiveTime" to time,
            "uid" to auth.currentUser!!.uid,
            "userImage" to imageUrl,
            "userName" to binding.idEdtUserName.text.toString().trim(),
            "userAddress" to binding.address.text.toString().trim(),
            "usergmail" to binding.gmail.text.toString().trim(),
            "userDob" to binding.idEdtPassword.text.toString().trim()
            // Add more fields as needed
        )
        val myRef = database.getReference()
        myRef.child("Xeeshan").child(auth.currentUser!!.uid).setValue(user)
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

    fun gmailPassValidation() {
        val gmail=binding.gmail.text.toString().trim()
        if (binding.idEdtUserName.length() == 0) {
            Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show()
        } else if (binding.idEdtUserName.length() <= 3) {
            Toast.makeText(this, "Please enter the correct name", Toast.LENGTH_SHORT).show()
        } else  if (binding.address.length() == 0) {
            Toast.makeText(this, "Please enter the address", Toast.LENGTH_SHORT).show()
        } else if (binding.address.length() <= 3) {
            Toast.makeText(this, "Please enter the correct address", Toast.LENGTH_SHORT).show()
        } else if (binding.dateOfBirthTextView.length() == 0) {
            Toast.makeText(this, "Please enter the dob", Toast.LENGTH_SHORT).show()
        }else if (!gmail.contains("@gmail")) { // Check if email is valid
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
        } else if (binding.idEdtPassword.length()<=6) {
            Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show()
        } else{
            saveUserDataToSharedPreferences()
            binding.idEdtPassword.visibility=View.VISIBLE
            binding.loginEmail.visibility=View.VISIBLE
            val email = binding.gmail.text.toString().trim()
            val password = binding.idEdtPassword.text.toString().trim()
            signInWithEmailAndPassword(email, password)
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

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    // Proceed with your application logic after 0successful sign-in
                    // For example, you can navigate to the next activity
                    saveUserTofirebaseGoogle()
                    val intent = Intent(this, UsersActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // Show a message to the user indicating the authentication failed
                    Toast.makeText(
                        this@MainActivity,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }



    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
    }
}