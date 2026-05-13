package com.example.nammamela.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fan_wall")
data class FanWallEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
