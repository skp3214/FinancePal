package com.skp3214.financepal.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseUser
import com.skp3214.financepal.R
import com.skp3214.financepal.auth.EmailPasswordFirebaseAuth
import com.skp3214.financepal.auth.GoogleSignInAuth
import kotlinx.coroutines.launch

class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: EmailPasswordFirebaseAuth
    private lateinit var googleSignInAuth: GoogleSignInAuth
    private lateinit var logoutButton: Button
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = EmailPasswordFirebaseAuth()
        googleSignInAuth = GoogleSignInAuth(this)
        logoutButton = findViewById(R.id.btn_logout)
        userNameTextView = findViewById(R.id.userName)
        userEmailTextView = findViewById(R.id.userEmail)
        profileImageView = findViewById(R.id.profileImage)

        val user = auth.getCurrentUser()
        user?.let {
            updateUI(it)
        }

        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                auth.logout()
                googleSignInAuth.signOut()
                val intent = Intent(this@UserProfileActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateUI(user: FirebaseUser) {
        userNameTextView.text = user.displayName ?: "User Name"
        userEmailTextView.text = user.email ?: "User Email"
        user.photoUrl?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.baseline_account_circle_24)
                .into(profileImageView)
        }
    }
}