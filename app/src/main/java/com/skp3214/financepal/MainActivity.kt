package com.skp3214.financepal

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomAdapter
    private val list = mutableListOf<Model>()
    private lateinit var imageUri: Uri
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var selectedImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val imageRepository = ImageRepository(resources)
        val databaseHelper = SQLiteDBHelper(this, null,imageRepository)

        loadAllDataFromDatabase(databaseHelper, imageRepository, list)

        adapter = CustomAdapter(list, { model ->

            databaseHelper.delItemByID(model.id)
            loadAllDataFromDatabase(databaseHelper, imageRepository, list)
            adapter.notifyDataSetChanged()
        }, { model ->
            val intent = Intent(this, ItemDetailActivity::class.java)
            intent.putExtra("id", model.id)
            intent.putExtra("name", model.name)
            intent.putExtra("amount", model.amount.toString())
            intent.putExtra("description", model.description)
            intent.putExtra("category", model.category)
            intent.putExtra("date", model.date)
            intent.putExtra("dueDate", model.dueDate)
            intent.putExtra("image", imageRepository.bitmapToByteArray(model.image))
            startActivity(intent)
        })

        recyclerView.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showAddItemDialog(imageRepository, databaseHelper)
        }

        val bottomNavigationView= findViewById<BottomNavigationView>(R.id.bottom_navigation)
        filterDataWithCategory( bottomNavigationView, databaseHelper, imageRepository,list,adapter)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                selectedImage.setImageURI(it)
            }
        }
    }

    fun showAddItemDialog(imageRepository: ImageRepository, databaseHelper: SQLiteDBHelper, existingModel: Model? = null) {
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
            spinnerCategory.setSelection(getCategoryPosition(it.category,resources))
            selectedImage.setImageBitmap(it.image)
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

                if (existingModel != null) {
                    existingModel.apply {
                        this.name = name
                        this.amount = amount
                        this.description = description
                        this.date = date
                        this.dueDate = dueDate
                        this.category = category
                        this.image = imageRepository.byteArrayToBitmap(image)
                    }
                    databaseHelper.updateItem(existingModel)
                } else {
                    databaseHelper.addItem(name, amount, description, category, image, date, dueDate)
                }

                loadAllDataFromDatabase(databaseHelper, imageRepository, list)
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}
