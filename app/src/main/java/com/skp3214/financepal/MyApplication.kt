package com.skp3214.financepal

import android.app.Application
import com.skp3214.financepal.repository.FinancePalRepository
import com.skp3214.financepal.repository.FirebaseRepository

class MyApplication : Application() {
    // Initialize Firebase repository instead of Room database
    private val firebaseRepository by lazy { FirebaseRepository() }

    // Create FinancePalRepository with Firebase
    val repository by lazy { FinancePalRepository(firebaseRepository) }
}