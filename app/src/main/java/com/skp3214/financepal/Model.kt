package com.skp3214.financepal

import android.graphics.Bitmap

class Model(
    var id: Int,
    var name: String,
    var amount: Double,
    var description: String,
    var category: String,
    var image: Bitmap,
    var date: String,
    var dueDate: String
)
