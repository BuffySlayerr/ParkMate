package com.example.parkmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.parkmate.data.local.dao.ParkingSessionDao
import com.example.parkmate.data.local.dao.SavedLocationDao
import com.example.parkmate.data.local.dao.VehicleDao
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.local.entity.SavedLocationEntity
import com.example.parkmate.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        SavedLocationEntity::class,
        ParkingSessionEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ParkMateDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun parkingSessionDao(): ParkingSessionDao

    companion object {
        @Volatile
        private var INSTANCE: ParkMateDatabase? = null

        fun getDatabase(context: Context): ParkMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkMateDatabase::class.java,
                    "parkmate_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}