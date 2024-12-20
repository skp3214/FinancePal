package com.skp3214.financepal.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skp3214.financepal.dao.FinanceDAO
import com.skp3214.financepal.model.Model

@Database(entities = [Model::class], version = 1)
abstract class FinancePalDatabase : RoomDatabase() {
    abstract fun financepalDao(): FinanceDAO
}
