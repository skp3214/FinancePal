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
import com.skp3214.financepal.auth.LoginResult
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: EmailPasswordFirebaseAuth
    private lateinit var googleSignInAuth: GoogleSignInAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var toggleRegisterButton: TextView
    private lateinit var googleSignInButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = EmailPasswordFirebaseAuth()
        googleSignInAuth = GoogleSignInAuth(this)
        email = findViewById(R.id.et_email)
        password = findViewById(R.id.et_password)
        loginButton = findViewById(R.id.login_btn)
        toggleRegisterButton = findViewById(R.id.toggle_register_action)
        googleSignInButton = findViewById(R.id.btn_google_signin)
        toggleRegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                when (auth.login(emailText, passwordText)) {
                    is LoginResult.Success -> {
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is LoginResult.Failure -> {
                        Toast.makeText(this@LoginActivity, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        googleSignInButton.setOnClickListener {
            lifecycleScope.launch {
                val success = googleSignInAuth.signIn()
                if (success) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Google Sign In failed. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}