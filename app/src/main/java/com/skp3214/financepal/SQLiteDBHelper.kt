package com.skp3214.financepal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteDBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?, private val imageRepository: ImageRepository) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE $TABLE_NAME (" +
                "$ID_COL INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$NAME_COL TEXT, " +
                "$AMOUNT_COL INTEGER, " +
                "$DESCRIPTION_COL TEXT, " +
                "$CATEGORY_COL TEXT, " +
                "$IMAGE_COL BLOB, " +
                "$DATE_COL TEXT, " +
                "$DUEDATE_COL TEXT" +
                ")"
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addItem(
        name: String,
        amount: Double,
        description: String,
        category: String,
        image: ByteArray,
        date: String,
        dueDate: String
    ) {
        val values = ContentValues()
        values.put(NAME_COL, name)
        values.put(AMOUNT_COL, amount)
        values.put(DESCRIPTION_COL, description)
        values.put(CATEGORY_COL, category)
        values.put(IMAGE_COL, image)
        values.put(DATE_COL, date)
        values.put(DUEDATE_COL, dueDate)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getData(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun getData(category: String): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $CATEGORY_COL = '$category'", null)
    }

    fun delItemByID(id: Int) {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE $ID_COL = $id")
        db.close()
    }
    fun updateItem(model: Model) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", model.name)
            put("amount", model.amount)
            put("description", model.description)
            put("category", model.category)
            put("image", imageRepository.bitmapToByteArray(model.image))
            put("date", model.date)
            put("dueDate", model.dueDate)
        }
        db.update(TABLE_NAME, contentValues, "ID = ${model.id}", null)
        db.close()
    }


    companion object {
        const val DATABASE_NAME = "ModelDB"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "FinancePal"
        const val ID_COL = "ID"
        const val NAME_COL = "NAME"
        const val AMOUNT_COL = "AMOUNT"
        const val DESCRIPTION_COL = "DESCRIPTION"
        const val CATEGORY_COL = "CATEGORY"
        const val IMAGE_COL = "IMAGE"
        const val DATE_COL = "DATE"
        const val DUEDATE_COL = "DUEDATE"

    }
}