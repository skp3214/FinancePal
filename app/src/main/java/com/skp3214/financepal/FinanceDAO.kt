package com.skp3214.financepal
import androidx.room.*

@Dao
interface FinanceDAO {
    @Insert
    suspend fun addEntry(entry: Model)

    @Query("SELECT * FROM FinancePal")
    suspend fun getAllEntries(): List<Model>

    @Query("SELECT * FROM FinancePal WHERE category = :category")
    suspend fun getEntriesByCategory(category: String): List<Model>

    @Delete
    suspend fun deleteEntry(entry: Model)

    @Update
    suspend fun updateEntry(entry: Model)
}
