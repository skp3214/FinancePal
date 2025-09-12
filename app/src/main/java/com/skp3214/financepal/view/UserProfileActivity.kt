package com.skp3214.financepal.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.skp3214.financepal.R
import com.skp3214.financepal.auth.EmailPasswordFirebaseAuth
import com.skp3214.financepal.auth.GoogleSignInAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Locale
import androidx.core.graphics.toColorInt
import kotlin.math.abs

class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: EmailPasswordFirebaseAuth
    private lateinit var googleSignInAuth: GoogleSignInAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    // UI Components
    private lateinit var toolbar: MaterialToolbar
    private lateinit var profileImage: ShapeableImageView
    private lateinit var editProfileFab: FloatingActionButton
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var totalEntriesChip: com.google.android.material.chip.Chip
    private lateinit var receivedChip: com.google.android.material.chip.Chip
    private lateinit var sentChip: com.google.android.material.chip.Chip
    private lateinit var netBalanceChip: com.google.android.material.chip.Chip
    private lateinit var changePasswordLayout: LinearLayout
    private lateinit var privacyLayout: LinearLayout
    private lateinit var logoutButton: MaterialButton

    // Image picker
    private var selectedImageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                // Update UI immediately with local URI
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade(200))
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(profileImage)
                
                // Then upload to Firebase
                uploadProfileImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        initializeComponents()
        setupToolbar()
        loadUserData()
        setupClickListeners()
        loadFinancialStatistics()
    }

    private fun initializeComponents() {
        auth = EmailPasswordFirebaseAuth()
        googleSignInAuth = GoogleSignInAuth(this)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        
        // Initialize UI components
        toolbar = findViewById(R.id.toolbar)
        profileImage = findViewById(R.id.profileImage)
        editProfileFab = findViewById(R.id.editProfileFab)
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        totalEntriesChip = findViewById(R.id.totalEntriesChip)
        receivedChip = findViewById(R.id.receivedChip)
        sentChip = findViewById(R.id.sentChip)
        netBalanceChip = findViewById(R.id.netBalanceChip)
        changePasswordLayout = findViewById(R.id.changePasswordLayout)
        privacyLayout = findViewById(R.id.privacyLayout)
        logoutButton = findViewById(R.id.btn_logout)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadUserData() {
        val user = auth.getCurrentUser()
        user?.let { firebaseUser ->
            updateUserUI(firebaseUser)
        }
    }

    private fun updateUserUI(user: FirebaseUser) {
        userName.text = user.displayName ?: "User"
        userEmail.text = user.email ?: "No email"
        
        // Load profile image immediately with cache for fast loading
        user.photoUrl?.let { photoUrl ->
            Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.baseline_account_circle_24)
                .error(R.drawable.baseline_account_circle_24)
                .into(profileImage)
        }
    }

    private fun loadFinancialStatistics() {
        val userId = auth.getCurrentUser()?.uid ?: return
        
        lifecycleScope.launch {
            try {
                val entries = firestore.collection("transactions")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                var totalEntriesCount = 0
                var totalReceivedAmount = 0.0
                var totalSentAmount = 0.0

                for (document in entries.documents) {
                    val data = document.data
                    data?.let {
                        totalEntriesCount++
                        val amount = (it["amount"] as? Double) ?: 0.0
                        val category = (it["category"] as? String)?.lowercase()
                        
                        when (category) {
                            "received" -> totalReceivedAmount += amount
                            "sent" -> totalSentAmount += amount
                        }
                    }
                }

                updateStatisticsUI(totalEntriesCount, totalReceivedAmount, totalSentAmount)
                
            } catch (e: Exception) {
                showError("Failed to load statistics: ${e.message}")
            }
        }
    }

    private fun updateStatisticsUI(entries: Int, received: Double, sent: Double) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        "$entries Entries".also { totalEntriesChip.text = it }
        "Total Received: ${currencyFormat.format(received)}".also { receivedChip.text = it }
        "Total Sent: ${currencyFormat.format(sent)}".also { sentChip.text = it }
        
        // Calculate and display difference amount with +/- and color coding
        val netBalance = received - sent
        val balanceText = if (netBalance >= 0) {
            "+ ${currencyFormat.format(netBalance)}"
        } else {
            val newNetBalance = abs(netBalance)
            "- ${currencyFormat.format(newNetBalance)}"
        }
        netBalanceChip.text = balanceText
        
        // Change color based on positive/negative balance
        val balanceColor = if (netBalance >= 0) {
            "#4CAF50".toColorInt() // Green for surplus
        } else {
            "#F44336".toColorInt() // Red for loss
        }
        netBalanceChip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(balanceColor)
    }

    private fun setupClickListeners() {
        editProfileFab.setOnClickListener {
            showEditProfileDialog()
        }

        changePasswordLayout.setOnClickListener {
            showChangePasswordDialog()
        }

        privacyLayout.setOnClickListener {
            showPrivacySettings()
        }

        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val dialogProfileImage = dialogView.findViewById<ShapeableImageView>(R.id.dialogProfileImage)
        val changePhotoFab = dialogView.findViewById<FloatingActionButton>(R.id.changePhotoFab)
        val editName = dialogView.findViewById<TextInputEditText>(R.id.editName)
        val editEmail = dialogView.findViewById<TextInputEditText>(R.id.editEmail)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<MaterialButton>(R.id.saveButton)

        // Pre-fill current data
        val currentUser = auth.getCurrentUser()
        currentUser?.let { user ->
            editName.setText(user.displayName)
            editEmail.setText(user.email)
            
            user.photoUrl?.let { photoUrl ->
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .into(dialogProfileImage)
            }
        }

        changePhotoFab.setOnClickListener {
            openImagePicker()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val newName = editName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateUserProfile(newName)
                dialog.dismiss()
            } else {
                showError("Name cannot be empty")
            }
        }

        dialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val userId = auth.getCurrentUser()?.uid ?: return
        
        lifecycleScope.launch {
            try {
                val storageRef = storage.reference.child("profile_images/$userId.jpg")
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                
                updateUserProfileImage(downloadUrl)
                
            } catch (e: Exception) {
                showError("Failed to upload image: ${e.message}")
            }
        }
    }

    private fun updateUserProfile(newName: String) {
        val user = auth.getCurrentUser() ?: return
        
        lifecycleScope.launch {
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                
                user.updateProfile(profileUpdates).await()
                
                // Update UI
                userName.text = newName
                showSuccess("Profile updated successfully")
                
            } catch (e: Exception) {
                showError("Failed to update profile: ${e.message}")
            }
        }
    }

    private fun updateUserProfileImage(imageUrl: Uri) {
        val user = auth.getCurrentUser() ?: return
        
        lifecycleScope.launch {
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(imageUrl)
                    .build()
                
                user.updateProfile(profileUpdates).await()
                
                // Update profile image with timestamp only after upload to bypass cache
                val timestampedUrl = "$imageUrl?t=${System.currentTimeMillis()}"
                Glide.with(this@UserProfileActivity)
                    .load(timestampedUrl)
                    .centerCrop()
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                    .into(profileImage)
                
                // Send result back to main activity to refresh
                setResult(RESULT_OK)
                
                showSuccess("Profile image updated successfully")
                
            } catch (e: Exception) {
                showError("Failed to update profile image: ${e.message}")
            }
        }
    }

    private fun showChangePasswordDialog() {
        // TODO: Implement change password functionality
        Snackbar.make(findViewById(android.R.id.content), "Change Password - Coming Soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showPrivacySettings() {
        // TODO: Implement privacy settings
        Snackbar.make(findViewById(android.R.id.content), "Privacy Settings - Coming Soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                auth.logout()
                googleSignInAuth.signOut()
                
                val intent = Intent(this@UserProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                
            } catch (e: Exception) {
                showError("Logout failed: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
