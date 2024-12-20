package com.skp3214.financepal
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDAO {
    @Insert
    suspend fun addEntry(entry: Model)

    @Query("SELECT * FROM FinancePal")
    fun getAllEntries(): LiveData<MutableList<Model>>

    @Query("SELECT * FROM FinancePal WHERE category = :category")
    fun getEntriesByCategory(category: String): LiveData<MutableList<Model>>

    @Delete
    suspend fun deleteEntry(entry: Model)

    @Update
    suspend fun updateEntry(entry: Model)
}
