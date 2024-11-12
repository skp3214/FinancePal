package com.skp3214.financepal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(
    private val models: MutableList<Model>,
    private val onDelete: (Model) -> Unit,
    private val onItemClicked: (Model) -> Unit
) : RecyclerView.Adapter<CustomAdapter.ModelViewHolder>() {

    class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_image)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_name)
        val amountTextView: TextView = itemView.findViewById(R.id.tv_amount)
        val descriptionTextView: TextView = itemView.findViewById(R.id.tv_description)
        val dateTextView: TextView = itemView.findViewById(R.id.tv_date)
        val dueDateTextView: TextView = itemView.findViewById(R.id.tv_due_date)
        val deadlineBar: View = itemView.findViewById(R.id.view_deadline_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_listview, parent, false)
        return ModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val model = models[position]

        holder.nameTextView.text = model.name
        ("Amount: " + model.amount.toFixed(2)).also { holder.amountTextView.text = it }
        holder.descriptionTextView.text = model.description
        "Date: ${model.date}".also { holder.dateTextView.text = it }
        "Due Date: ${model.dueDate}".also { holder.dueDateTextView.text = it }
        holder.imageView.setImageBitmap(model.image)

        holder.itemView.setOnClickListener { onItemClicked(model) }

        setupDeadlineBar(holder.deadlineBar, model.date, model.dueDate)

        val menuIcon = holder.itemView.findViewById<ImageView>(R.id.iv_menu)
        showMenuIcon(holder, menuIcon, model, onDelete)
    }

    override fun getItemCount(): Int = models.size

    private fun Double.toFixed(numDigits: Int): String = String.format("%.${numDigits}f", this)
}
