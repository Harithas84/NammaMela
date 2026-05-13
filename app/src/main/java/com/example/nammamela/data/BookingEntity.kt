package com.example.nammamela.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playId: Int,
    val playName: String,
    val date: String,
    val time: String,
    val seats: String,
    val totalPrice: Double,
    val paymentMethod: String = "UPI / Card",
    val timestamp: Long = System.currentTimeMillis()
)
