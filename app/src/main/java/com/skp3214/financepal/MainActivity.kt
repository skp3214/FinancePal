package com.skp3214.financepal

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.skp3214.financepal.customadapters.CustomAdapter
import com.skp3214.financepal.model.Model
import com.skp3214.financepal.auth.EmailPasswordFirebaseAuth
import com.skp3214.financepal.auth.GoogleSignInAuth
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
import androidx.core.graphics.drawable.toDrawable

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
        setupSearch()
        setupFilterChips()
        observeData()

        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

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
        // Any additional UI setup can go here
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
                
                // Determine final image URL
                val finalImageUrl = if (imageUri != null) {
                    // User selected new image
                    imageUri.toString()
                } else {
                    // Keep existing image or use placeholder
                    existingImageUrl
                }
                
                if (existingModel != null) {
                    existingModel.apply {
                        this.name = name
                        this.amount = amount
                        this.description = description
                        this.date = date
                        this.dueDate = dueDate
                        this.category = category
                        this.image = finalImageUrl
                    }
                    financePalViewModel.updateEntry(existingModel)
                    // Force immediate refresh for updates with slight delay
                    recyclerView.postDelayed({ applyFilters() }, 100)
                } else {
                    val newItem = Model("", name, amount, description, category, finalImageUrl, date, dueDate, "")
                    addItem(newItem)
                }
                
                dialog.dismiss()
                
                // Only try to upload if user selected a new image
                if (imageUri != null) {
                    lifecycleScope.launch {
                        try {
                            val imageByteArray = imageRepository.bitmapToByteArray(selectedImage.drawable.toBitmap())
                            val cloudImageUrl = firebaseRepository.uploadImage(imageByteArray)
                            
                            // Update with cloud URL when upload succeeds
                            if (existingModel != null) {
                                existingModel.image = cloudImageUrl
                                financePalViewModel.updateEntry(existingModel)
                            } else {
                                // For new items, find and update with cloud URL
                                // This will be handled by repository sync
                            }
                        } catch (e: Exception) {
                            // Keep local URI - will show local image
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun deleteItem(model: Model) {
        financePalViewModel.deleteEntry(model)
        // Force immediate refresh with slight delay
        recyclerView.postDelayed({ applyFilters() }, 100)
    }

    private fun addItem(model: Model){
        financePalViewModel.addEntry(model)
        // Force immediate refresh with slight delay
        recyclerView.postDelayed({ applyFilters() }, 100)
    }

    private fun leftSwipeDeleteAndUndoFeature(){
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.layoutPosition
                val deletedModel = list[position]
                deleteItem(deletedModel)
                adapter.notifyItemRemoved(position)

                Snackbar.make(
                    recyclerView,
                    "Item deleted",
                    Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    addItem(deletedModel)
                    adapter.notifyDataSetChanged()
                }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun imagePickerLauncherSetup(){
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                selectedImage.setImageURI(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_profile, menu)
        val user = EmailPasswordFirebaseAuth().getCurrentUser()
        user?.photoUrl?.let { photoUrl ->
            val menuItem = menu?.findItem(R.id.action_profile)
            Glide.with(this)
                .asBitmap()
                .load(photoUrl)
                .placeholder(R.drawable.baseline_account_circle_24)
                .circleCrop()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        menuItem?.icon = resource.toDrawable(resources)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        menuItem?.icon = placeholder
                    }
                })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
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
