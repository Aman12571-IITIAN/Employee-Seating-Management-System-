package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.SeatingDao
import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.SeatingLogEntity

@Database(
    entities = [DeskEntity::class, EmployeeEntity::class, SeatingLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SeatingDatabase : RoomDatabase() {
    abstract fun seatingDao(): SeatingDao

    companion object {
        @Volatile
        private var INSTANCE: SeatingDatabase? = null

        fun getInstance(context: Context): SeatingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SeatingDatabase::class.java,
                    "seating_management_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
