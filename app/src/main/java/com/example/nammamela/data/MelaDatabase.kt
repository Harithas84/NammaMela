package com.example.nammamela.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SeatEntity::class, PlayEntity::class, FanWallEntity::class, BookingEntity::class], version = 7, exportSchema = false)
abstract class MelaDatabase : RoomDatabase() {
    abstract fun melaDao(): MelaDao

    companion object {
        @Volatile
        private var INSTANCE: MelaDatabase? = null

        fun getDatabase(context: Context): MelaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MelaDatabase::class.java,
                    "mela_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
