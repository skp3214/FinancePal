package com.skp3214.financepal

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun loadAllDataFromDatabase(db: SQLiteDBHelper, imageRepository: ImageRepository, list: MutableList<Model>) {
    list.clear()
    val cursor = db.getData()
    cursor?.let {
        while (it.moveToNext()) {
            val id = it.getInt(it.getColumnIndexOrThrow(SQLiteDBHelper.ID_COL))
            val name = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.NAME_COL))
            val amount = it.getDouble(it.getColumnIndexOrThrow(SQLiteDBHelper.AMOUNT_COL))
            val description = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.DESCRIPTION_COL))
            val category = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.CATEGORY_COL))
            val image = it.getBlob(it.getColumnIndexOrThrow(SQLiteDBHelper.IMAGE_COL))
            val date = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.DATE_COL))
            val dueDate = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.DUEDATE_COL))
            val bitmap = imageRepository.byteArrayToBitmap(image)
            list.add(Model(id, name, amount, description, category, bitmap, date, dueDate))
        }
        it.close()
    }
}

fun loadAllCategoryDataFromDatabase(db: SQLiteDBHelper, imageRepository: ImageRepository, list: MutableList<Model>, category: String) {
    list.clear()
    val cursor = db.getData(category)
    cursor?.let {
        while (it.moveToNext()) {
            val id = it.getInt(it.getColumnIndexOrThrow(SQLiteDBHelper.ID_COL))
            val name = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.NAME_COL))
            val amount = it.getDouble(it.getColumnIndexOrThrow(SQLiteDBHelper.AMOUNT_COL))
            val description = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.DESCRIPTION_COL))
            val image = it.getBlob(it.getColumnIndexOrThrow(SQLiteDBHelper.IMAGE_COL))
            val date = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.DATE_COL))
            val dueDate = it.getString(it.getColumnIndexOrThrow(SQLiteDBHelper.DUEDATE_COL))
            val bitmap = imageRepository.byteArrayToBitmap(image)
            list.add(Model(id, name, amount, description, category, bitmap, date, dueDate))
        }
        it.close()
    }
}

fun setupDeadlineBar(view: View, startDate: String, dueDate: String) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    try {
        val start = dateFormat.parse(startDate) ?: Date()
        val due = dateFormat.parse(dueDate) ?: Date()
        val today = Date()

        val totalDuration = due.time - start.time
        val elapsedTime = today.time - start.time

        val progress = when {
            elapsedTime <= 0 -> 0f
            elapsedTime >= totalDuration -> 100f
            else -> (elapsedTime.toFloat() / totalDuration.toFloat() * 100)
        }

        val backgroundDrawable = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 8f
        }

        val progressDrawable = GradientDrawable().apply {
            val color = when {
                progress < 50 -> Color.rgb(76, 175, 80)
                progress < 75 -> Color.rgb(255, 152, 0)
                else -> Color.rgb(244, 67, 54)
            }
            setColor(color)
            cornerRadius = 8f
        }

        val clipDrawable = ClipDrawable(progressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)

        view.background = backgroundDrawable
        view.foreground = clipDrawable
        clipDrawable.level = (progress * 100).toInt()

    } catch (e: Exception) {
        view.setBackgroundColor(Color.LTGRAY)
    }
}

fun showMenuIcon(holder: CustomAdapter.ModelViewHolder, menuIcon: ImageView, model: Model, onDelete: (Model) -> Unit) {
    menuIcon.setOnClickListener {
        val popup = PopupMenu(holder.itemView.context, menuIcon)
        popup.inflate(R.menu.options_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    onDelete(model)
                    true
                }
                R.id.action_edit -> {
                    (holder.itemView.context as MainActivity).showAddItemDialog(
                        imageRepository = ImageRepository(holder.itemView.resources),
                        databaseHelper = SQLiteDBHelper(holder.itemView.context, null, ImageRepository(holder.itemView.resources)),
                        existingModel = model
                    )
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}

fun getCategoryPosition(category: String, resources: Resources): Int {
    val categories = resources.getStringArray(R.array.category_options)
    return categories.indexOf(category)
}

var currentTab: Int = R.id.item_1

fun filterDataWithCategory(bottomNavigationView: BottomNavigationView, databaseHelper: SQLiteDBHelper, imageRepository: ImageRepository, list: MutableList<Model>, adapter: CustomAdapter) {
    bottomNavigationView.setOnItemSelectedListener { item ->
        currentTab = item.itemId
        filterData(databaseHelper, imageRepository, list, adapter)
        true
    }
}

fun filterData(databaseHelper: SQLiteDBHelper, imageRepository: ImageRepository, list: MutableList<Model>, adapter: CustomAdapter) {
    list.clear()

    when (currentTab) {
        R.id.item_1 -> loadAllDataFromDatabase(databaseHelper, imageRepository, list)
        R.id.item_2 -> loadAllCategoryDataFromDatabase(databaseHelper, imageRepository, list, "Sent")
        R.id.item_3 -> loadAllCategoryDataFromDatabase(databaseHelper, imageRepository, list, "Received")
    }

    adapter.notifyDataSetChanged()
}



