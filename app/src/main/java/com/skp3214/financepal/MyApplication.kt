package com.skp3214.financepal

import android.app.Application
import com.skp3214.financepal.database.DatabaseProvider
import com.skp3214.financepal.repository.FinancePalRepository

class MyApplication:Application() {
    private val database by lazy{ DatabaseProvider.getDatabase(this)}
    val repository by lazy{ FinancePalRepository(database.financepalDao()) }
}