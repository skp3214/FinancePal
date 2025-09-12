package com.skp3214.financepal

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.skp3214.financepal.auth.EmailPasswordFirebaseAuth
import com.skp3214.financepal.auth.GoogleSignInAuth
import com.skp3214.financepal.customadapters.CustomAdapter
import com.skp3214.financepal.model.Model
import com.skp3214.financepal.repository.FirebaseRepository
import com.skp3214.financepal.utils.ImageRepository
import com.skp3214.financepal.utils.getCategoryPosition
import com.skp3214.financepal.view.ItemDetailActivity
import com.skp3214.financepal.view.LoginActivity
import com.skp3214.financepal.view.UserProfileActivity
import com.skp3214.financepal.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private var list = mutableListOf<Model>()
    private var allItems = listOf<Model>()
    private var imageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var selectedImage: ImageView
    private lateinit var imageRepository: ImageRepository
    private lateinit var googleSignInAuth: GoogleSignInAuth
    lateinit var firebaseRepository: FirebaseRepository
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var currentSortOrder: String = "none"
    private var currentCategory: String = "All"
    private var searchQuery: String = ""
    private val financePalViewModel: FinanceViewModel by viewModels {
        FinanceViewModel.FinanceViewModelFactory((application as MyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val firebase= EmailPasswordFirebaseAuth()
        if(!firebase.isLoggedIn()){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        firebaseRepository = FirebaseRepository()
        googleSignInAuth = GoogleSignInAuth(this)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        financePalViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }

        setupUI()
        setupNavigationDrawer()
        setupSearch()
        setupFilterChips()
        setupProfileImage()
        observeData()

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        imageRepository = ImageRepository(resources)

        adapter = CustomAdapter(list, { model ->
            deleteItem(model)
        }, { model ->
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("id", model.id)
            startActivity(intent)
        })

        recyclerView.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showAddItemDialog(null,firebaseRepository)
        }

        imagePickerLauncherSetup()
        leftSwipeDeleteAndUndoFeature()
    }

    private fun setupUI() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
    }

    private fun setupNavigationDrawer() {
        val hamburgerMenu = findViewById<ImageView>(R.id.hamburger_menu)
        hamburgerMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_all_transactions -> {
                    currentCategory = "All"
                    applyFilters()
                }
                R.id.nav_sent -> {
                    currentCategory = "Sent"
                    applyFilters()
                }
                R.id.nav_received -> {
                    currentCategory = "Received"
                    applyFilters()
                }
                R.id.nav_sort_amount_asc -> {
                    currentSortOrder = "amount_asc"
                    applyFilters()
                }
                R.id.nav_sort_amount_desc -> {
                    currentSortOrder = "amount_desc"
                    applyFilters()
                }
                R.id.nav_sort_date_asc -> {
                    currentSortOrder = "date_asc"
                    applyFilters()
                }
                R.id.nav_sort_date_desc -> {
                    currentSortOrder = "date_desc"
                    applyFilters()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupProfileImage() {
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        
        // Load user profile image
        val user = EmailPasswordFirebaseAuth().getCurrentUser()
        user?.photoUrl?.let { photoUrl ->
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.baseline_account_circle_24)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .circleCrop()
                .into(profileImage)
        }
        
        // Set click listener to navigate to profile
        profileImage.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setupProfileImage()
    }

    private fun setupSearch() {
        val searchBar = findViewById<TextInputEditText>(R.id.search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.lowercase() ?: ""
                applyFilters()
            }
        })
    }

    private fun setupFilterChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_filter)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentCategory = when (checkedIds[0]) {
                    R.id.chip_all -> "All"
                    R.id.chip_sent -> "Sent"
                    R.id.chip_received -> "Received"
                    else -> "All"
                }
                applyFilters()
            }
        }
    }

    private fun observeData() {
        financePalViewModel.allEntryItems.observe(this) { items ->
            allItems = items ?: emptyList()
            applyFilters()
        }
    }

    private fun applyFilters() {
        var filteredItems = allItems

        // Apply category filter
        if (currentCategory != "All") {
            filteredItems = filteredItems.filter { it.category == currentCategory }
        }

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            filteredItems = filteredItems.filter { item ->
                item.name.lowercase().contains(searchQuery) ||
                item.description.lowercase().contains(searchQuery) ||
                item.amount.toString().contains(searchQuery)
            }
        }

        // Apply sorting
        filteredItems = when (currentSortOrder) {
            "amount_asc" -> filteredItems.sortedBy { it.amount }
            "amount_desc" -> filteredItems.sortedByDescending { it.amount }
            "date_asc" -> filteredItems.sortedBy { it.date }
            "date_desc" -> filteredItems.sortedByDescending { it.date }
            else -> filteredItems
        }

        updateRecyclerView(filteredItems)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView(items: List<Model>) {
        list.clear()
        list.addAll(items)
        adapter.notifyDataSetChanged()
    }

    fun showAddItemDialog(existingModel: Model? = null, firebaseRepository: FirebaseRepository) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogue_form, null)
        val etName = dialogView.findViewById<EditText>(R.id.et_name)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)
        val etDescription = dialogView.findViewById<EditText>(R.id.et_description)
        val tvDate = dialogView.findViewById<TextView>(R.id.tv_date)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tv_due_date)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinner_category)
        selectedImage = dialogView.findViewById(R.id.imageView)
        val btnSelect = dialogView.findViewById<Button>(R.id.select)
        val btnSave = dialogView.findViewById<TextView>(R.id.btn_save)

        // Reset imageUri for new dialog
        imageUri = null
        var existingImageUrl = "no_image_uploaded"

        tvDate.setOnClickListener { showDatePicker(tvDate,"date") }
        tvDueDate.setOnClickListener { showDatePicker(tvDueDate,"duedate") }

        btnSelect.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        existingModel?.let {
            etName.setText(it.name)
            etAmount.setText("${it.amount}")
            etDescription.setText(it.description)
            tvDate.text = it.date
            tvDueDate.text = it.dueDate
            spinnerCategory.setSelection(getCategoryPosition(it.category, resources))
            existingImageUrl = it.image
            
            // Load existing image
            when {
                it.image == "no_image_uploaded" -> {
                    selectedImage.setImageResource(R.drawable.nossuploaded)
                }
                it.image.startsWith("content://") -> {
                    Glide.with(this).load(it.image).into(selectedImage)
                }
                it.image.startsWith("http") -> {
                    Glide.with(this).load(it.image).placeholder(R.drawable.loading).into(selectedImage)
                }
                else -> {
                    selectedImage.setImageResource(R.drawable.nossuploaded)
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val amountText = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val date = tvDate.text.toString().trim()
            val dueDate = tvDueDate.text.toString().trim()
            val category = spinnerCategory.selectedItem?.toString()?.trim() ?: ""

            if (name.isEmpty() || amountText.isEmpty() || description.isEmpty() || date.isEmpty() || dueDate.isEmpty() ||
                (category != "Sent" && category != "Received")
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Incomplete Information")
                    .setMessage("Please fill all fields and select a valid category (Sent or Received) before saving.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                
                // Handle image upload first if new image selected
                // Handle image upload first if new image selected
                if (imageUri != null) {
                    // Show progress while uploading
                    btnSave.isEnabled = false
                    btnSave.text = "Uploading..."
                    
                    lifecycleScope.launch {
                        try {
                            val imageByteArray = imageRepository.bitmapToByteArray(selectedImage.drawable.toBitmap())
                            val cloudImageUrl = firebaseRepository.uploadImage(imageByteArray)
                            
                            // Save with cloud URL
                            saveItemWithImage(existingModel, name, amount, description, date, dueDate, category, cloudImageUrl)
                            dialog.dismiss()
                        } catch (e: Exception) {
                            // If upload fails, save with placeholder
                            saveItemWithImage(existingModel, name, amount, description, date, dueDate, category, "no_image_uploaded")
                            dialog.dismiss()
                        }
                    }
                } else {
                    // No new image, use existing or placeholder
                    saveItemWithImage(existingModel, name, amount, description, date, dueDate, category, existingImageUrl)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun addItem(model: Model) {
        financePalViewModel.addEntry(model)
    }

    private fun deleteItem(model: Model) {
        financePalViewModel.deleteEntry(model)
    }

    private fun leftSwipeDeleteAndUndoFeature() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedItem = list[position]

                financePalViewModel.deleteEntry(deletedItem)

                Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        financePalViewModel.addEntry(deletedItem)
                    }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun saveItemWithImage(existingModel: Model?, name: String, amount: Double, description: String, date: String, dueDate: String, category: String, imageUrl: String) {
        if (existingModel != null) {
            existingModel.apply {
                this.name = name
                this.amount = amount
                this.description = description
                this.date = date
                this.dueDate = dueDate
                this.category = category
                this.image = imageUrl
            }
            financePalViewModel.updateEntry(existingModel)
            recyclerView.postDelayed({ applyFilters() }, 100)
        } else {
            val newItem = Model("", name, amount, description, category, imageUrl, date, dueDate, "")
            addItem(newItem)
        }
    }

    private fun imagePickerLauncherSetup(){
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                selectedImage.setImageURI(it)
            }
        }
    }

    private fun showDatePicker(textView: TextView, type: String) {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            if ((type == "duedate") && calendar.before(today)) {
                AlertDialog.Builder(this)
                    .setTitle("Invalid Date")
                    .setMessage("Due date cannot be in the past.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                textView.text = dateFormatter.format(calendar.time)
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
