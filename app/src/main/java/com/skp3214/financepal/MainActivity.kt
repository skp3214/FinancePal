package com.skp3214.financepal

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.skp3214.financepal.customadapters.CustomAdapter
import com.skp3214.financepal.model.Model
import com.skp3214.financepal.utils.ImageRepository
import com.skp3214.financepal.utils.getCategoryPosition
import com.skp3214.financepal.view.ItemDetailActivity
import com.skp3214.financepal.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private var list = mutableListOf<Model>()
    private lateinit var imageUri: Uri
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var selectedImage: ImageView
    private lateinit var imageRepository: ImageRepository
    private var currentCategory: String? = "All"
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

        observeEntryItems()
        bottomNavigationAndCurrentCategorySetup()
        showDataWithCurrentCategory()

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
            showAddItemDialog(null)
        }

        imagePickerLauncherSetup()
        leftSwipeDeleteAndUndoFeature()
    }

    fun showAddItemDialog(existingModel: Model? = null) {
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

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fun showDatePicker(textView: TextView) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                textView.text = dateFormatter.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvDate.setOnClickListener { showDatePicker(tvDate) }
        tvDueDate.setOnClickListener { showDatePicker(tvDueDate) }

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
            selectedImage.setImageBitmap(imageRepository.byteArrayToBitmap(it.image))
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
                if (selectedImage.drawable == null) {
                    selectedImage.setImageResource(R.drawable.nossuploaded)
                }
                val image = imageRepository.bitmapToByteArray(selectedImage.drawable.toBitmap())
                lifecycleScope.launch {
                    if (existingModel != null) {
                        existingModel.apply {
                            this.name = name
                            this.amount = amount
                            this.description = description
                            this.date = date
                            this.dueDate = dueDate
                            this.category = category
                            this.image = image
                        }
                        financePalViewModel.updateEntry(existingModel)
                    } else {
                        val newItem = Model(0, name, amount, description, category,image,date, dueDate)
                        addItem(newItem)
                    }
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun bottomNavigationAndCurrentCategorySetup() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            currentCategory = when (item.itemId) {
                R.id.item_1 -> "All"
                R.id.item_2 -> "Sent"
                R.id.item_3 -> "Received"
                else -> null
            }
            showDataWithCurrentCategory()
            true
        }
    }

    private fun showDataWithCurrentCategory() {
        financePalViewModel.allEntryItems.removeObservers(this)
        financePalViewModel.getEntriesByCategory("Sent").removeObservers(this)
        financePalViewModel.getEntriesByCategory("Received").removeObservers(this)

        when (currentCategory) {
            "All" -> {
                financePalViewModel.allEntryItems.observe(this) { items ->
                    updateRecyclerView(items)
                }
            }
            else -> {
                financePalViewModel.getEntriesByCategory(currentCategory!!).observe(this) { items ->
                    updateRecyclerView(items)
                }
            }
        }
    }

    private fun updateRecyclerView(items: List<Model>) {
        list.clear()
        list.addAll(items)
        adapter.notifyDataSetChanged()
    }

    private fun deleteItem(model: Model) {
        lifecycleScope.launch {
            financePalViewModel.deleteEntry(model)
        }
    }

    private fun addItem(model: Model){
        lifecycleScope.launch {
            financePalViewModel.addEntry(model)
        }
    }

    private fun observeEntryItems() {
        financePalViewModel.allEntryItems.observe(this) { entryItems ->
            entryItems?.let {
                updateRecyclerView(it)
            }
        }
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
}
