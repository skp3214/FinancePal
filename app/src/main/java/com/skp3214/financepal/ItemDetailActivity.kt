package com.skp3214.financepal

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ItemDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.item_detail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val name= intent.getStringExtra("name")
        val amount= intent.getStringExtra("amount")
        val description= intent.getStringExtra("description")
        val image= intent.getByteArrayExtra("image")
        val category= intent.getStringExtra("category")
        val date= intent.getStringExtra("date")
        val dueDate= intent.getStringExtra("dueDate")

        val imageRepository = ImageRepository(resources)

        findViewById<ImageView>(R.id.iv_large_image).setImageBitmap(image?.let {
            imageRepository.byteArrayToBitmap(
                it
            )
        })
        findViewById<android.widget.TextView>(R.id.tv_name).text = name
        "Amount: ${amount.toString()}".also { findViewById<android.widget.TextView>(R.id.tv_amount).text = it }
        findViewById<android.widget.TextView>(R.id.tv_description).text = description
        "Category: $category".also { findViewById<android.widget.TextView>(R.id.tv_category).text = it }
        "Date: $date".also { findViewById<android.widget.TextView>(R.id.tv_date).text = it }
        "Due Date: $dueDate".also { findViewById<android.widget.TextView>(R.id.tv_due_date).text = it }
    }
}