package com.skp3214.financepal.repository

import androidx.lifecycle.LiveData
import com.skp3214.financepal.dao.FinanceDAO
import com.skp3214.financepal.model.Model

class FinancePalRepository(private val financepalDao: FinanceDAO) {

    val allEntries: LiveData<MutableList<Model>> = financepalDao.getAllEntries()

    fun getEntriesByCategory(category: String): LiveData<MutableList<Model>> {
        return financepalDao.getEntriesByCategory(category)
    }

    suspend fun addEntry(entry: Model) {
        financepalDao.addEntry(entry)
    }

    suspend fun deleteEntry(entry: Model) {
        financepalDao.deleteEntry(entry)
    }

    suspend fun updateEntry(entry: Model) {
        financepalDao.updateEntry(entry)
    }
}