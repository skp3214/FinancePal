package com.skp3214.financepal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FinancePal")
data class Model(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var amount: Double,
    var description: String,
    var category: String,
    var image: ByteArray,
    var date: String,
    var dueDate: String
)
