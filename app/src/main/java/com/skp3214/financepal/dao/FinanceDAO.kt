package com.skp3214.financepal.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.skp3214.financepal.model.Model

@Dao
interface FinanceDAO {
    @Insert
    suspend fun addEntry(entry: Model)

    @Query("SELECT * FROM FinancePal")
    fun getAllEntries(): LiveData<List<Model>>

    @Query("SELECT * FROM FinancePal WHERE category = :category")
    fun getEntriesByCategory(category: String): LiveData<List<Model>>

    @Delete
    suspend fun deleteEntry(entry: Model)

    @Update
    suspend fun updateEntry(entry: Model)
}
