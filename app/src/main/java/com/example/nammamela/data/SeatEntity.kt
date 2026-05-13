package com.example.nammamela.data

import androidx.room.Entity

@Entity(tableName = "seats", primaryKeys = ["playId", "id"])
data class SeatEntity(
    val playId: Int,
    val id: String,
    val isBooked: Boolean = false
)
