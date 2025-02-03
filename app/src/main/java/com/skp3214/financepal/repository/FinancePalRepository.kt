package com.skp3214.financepal.repository

import com.skp3214.financepal.model.Model
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinancePalRepository(private val firebaseDB: FirebaseRepository) {

    suspend fun getAllEntries(): List<Model> = withContext(Dispatchers.IO) {
        firebaseDB.getAllTransactions()
    }

//    suspend fun getEntriesByCategory(category: String): List<Model> {
//        return firebaseDB.getTransactionsByCategory(category)
//    }

    suspend fun addEntry(entry: Model) {
        firebaseDB.addTransaction(entry)
    }

    suspend fun deleteEntry(entry: Model) {
        firebaseDB.deleteTransaction(entry.id)
    }

    suspend fun updateEntry(entry: Model) {
        firebaseDB.updateTransaction(entry.id, entry)
    }

//    suspend fun getEntryById(id: String): Model? {
//        return firebaseDB.getTransaction(id)
//    }
}