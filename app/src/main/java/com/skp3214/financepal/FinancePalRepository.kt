package com.skp3214.financepal

class FinancePalRepository(private val financepalDao: FinanceDAO) {

    suspend fun addEntry(entry: Model) {
        financepalDao.addEntry(entry)
    }

    suspend fun getAllEntries(): List<Model> {
        return financepalDao.getAllEntries()
    }

    suspend fun getEntriesByCategory(category: String): List<Model> {
        return financepalDao.getEntriesByCategory(category)
    }

    suspend fun deleteEntry(entry: Model) {
        financepalDao.deleteEntry(entry)
    }

    suspend fun updateEntry(entry: Model) {
        financepalDao.updateEntry(entry)
    }
}
