package com.skp3214.financepal.model

data class Model(
    val id: String,
    var name: String,
    var amount: Double,
    var description: String,
    var category: String,
    var image: ByteArray,
    var date: String,
    var dueDate: String,
    var userId: String
)

