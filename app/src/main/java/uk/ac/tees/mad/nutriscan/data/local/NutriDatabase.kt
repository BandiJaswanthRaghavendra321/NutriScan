package uk.ac.tees.mad.nutriscan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProductLocal::class], version = 1, exportSchema = false)
abstract class NutriDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}