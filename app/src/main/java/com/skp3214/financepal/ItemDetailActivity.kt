package com.skp3214.financepal

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class ItemDetailActivity : AppCompatActivity() {
    private val financePalViewModel:FinanceViewModel by viewModels {
        FinanceViewModel.FinanceViewModelFactory((application as MyApplication).repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.item_detail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val id = intent.getIntExtra("id",0)

        lifecycleScope.launch {
            financePalViewModel.allEntryItems.asFlow()
                .filter { itemList -> itemList.any { it.id == id } }
                .collect { itemList ->
                    val entry = itemList.find { it.id == id } ?: return@collect

                    val imageRepository = ImageRepository(resources)

                    findViewById<ImageView>(R.id.iv_large_image).setImageBitmap(
                        entry.image.let { imageRepository.byteArrayToBitmap(it) }
                    )
                    findViewById<android.widget.TextView>(R.id.tv_name).text = entry.name
                    "Amount: ${entry.amount}".also { findViewById<android.widget.TextView>(R.id.tv_amount).text = it }
                    findViewById<android.widget.TextView>(R.id.tv_description).text = entry.description
                    "Category: ${entry.category}".also { findViewById<android.widget.TextView>(R.id.tv_category).text = it }
                    "Date: ${entry.date}".also { findViewById<android.widget.TextView>(R.id.tv_date).text = it }
                    "Due Date: ${entry.dueDate}".also { findViewById<android.widget.TextView>(R.id.tv_due_date).text = it }
                }
        }
    }
}