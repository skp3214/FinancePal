package com.skp3214.financepal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FinancePal")
data class Model(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val description: String,
    val category: String,
    val image: ByteArray,
    val date: String,
    val dueDate: String
)
