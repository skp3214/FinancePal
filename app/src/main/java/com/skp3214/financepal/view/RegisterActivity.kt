package com.skp3214.financepal.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.skp3214.financepal.MainActivity
import com.skp3214.financepal.R
import com.skp3214.financepal.auth.EmailPasswordFirebaseAuth
import com.skp3214.financepal.auth.GoogleSignInAuth
import com.skp3214.financepal.auth.RegistrationResult
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: EmailPasswordFirebaseAuth
    private lateinit var googleSignInAuth: GoogleSignInAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var toggleLoginButton: TextView
    private lateinit var googleSignUpButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = EmailPasswordFirebaseAuth()
        googleSignInAuth = GoogleSignInAuth(this)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        registerButton = findViewById(R.id.register_btn)
        toggleLoginButton = findViewById(R.id.toggle_login_action)
        googleSignUpButton = findViewById(R.id.btn_google_signup)

        toggleLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                when (auth.register(emailText, passwordText)) {
                    is RegistrationResult.Success -> {
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is RegistrationResult.UserExists -> {
                        Toast.makeText(this@RegisterActivity, "User already exists with this email.", Toast.LENGTH_SHORT).show()
                    }
                    is RegistrationResult.Failure -> {
                        Toast.makeText(this@RegisterActivity, "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        googleSignUpButton.setOnClickListener {
            lifecycleScope.launch {
                val success = googleSignInAuth.signIn()
                if (success) {
                    Toast.makeText(this@RegisterActivity, "Google Sign-In successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Google Sign-In failed. You can try Email Password.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}