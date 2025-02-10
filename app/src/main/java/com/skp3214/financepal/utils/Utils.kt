package com.skp3214.financepal.utils

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import com.skp3214.financepal.MainActivity
import com.skp3214.financepal.R
import com.skp3214.financepal.customadapters.CustomAdapter
import com.skp3214.financepal.model.Model
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                        existingModel = model,
                        firebaseRepository = (holder.itemView.context as MainActivity).firebaseRepository
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



