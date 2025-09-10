package com.skp3214.financepal.repository

import com.skp3214.financepal.model.Model
import kotlinx.coroutines.flow.Flow

class FinancePalRepository(private val firebaseDB: FirebaseRepository) {

    fun getAllEntries(): Flow<List<Model>> = firebaseDB.getAllTransactionsFlow()

    fun addEntry(entry: Model, onResult: (Boolean) -> Unit) {
        firebaseDB.addTransaction(entry, onResult)
    }

    fun deleteEntry(entry: Model, onResult: (Boolean) -> Unit) {
        firebaseDB.deleteTransaction(entry.id, onResult)
    }

    fun updateEntry(entry: Model, onResult: (Boolean) -> Unit) {
        firebaseDB.updateTransaction(entry.id, entry, onResult)
    }
}
