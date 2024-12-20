package com.skp3214.financepal

import android.app.Application

class MyApplication:Application() {
    private val database by lazy{DatabaseProvider.getDatabase(this)}
    val repository by lazy{FinancePalRepository(database.financepalDao())}
}