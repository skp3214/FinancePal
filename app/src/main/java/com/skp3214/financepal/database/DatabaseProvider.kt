package com.skp3214.financepal.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var INSTANCE: FinancePalDatabase? = null

    fun getDatabase(context: Context): FinancePalDatabase {
        if (INSTANCE == null) {
            synchronized(FinancePalDatabase::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        FinancePalDatabase::class.java,
                        "FinancePalDB"
                    ).build()
                }
            }
        }
        return INSTANCE!!
    }
}
