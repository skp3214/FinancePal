package com.skp3214.financepal

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Model::class], version = 1)
abstract class FinancePalDatabase : RoomDatabase() {
    abstract fun financepalDao(): FinanceDAO
}
