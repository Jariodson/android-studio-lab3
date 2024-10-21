package com.lab1.lab3

import android.app.Application
import androidx.room.Room
import com.lab1.lab3.MainActivity.AppDatabase

class DataBaseInit: Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).fallbackToDestructiveMigration().build()
    }
}