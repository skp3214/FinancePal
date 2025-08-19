package com.skp3214.financepal

import android.app.Application
import com.google.firebase.FirebaseApp
import com.skp3214.financepal.repository.FinancePalRepository
import com.skp3214.financepal.repository.FirebaseRepository

class MyApplication : Application() {
    private val firebaseRepository by lazy { FirebaseRepository() }

    val repository by lazy { FinancePalRepository(firebaseRepository) }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}