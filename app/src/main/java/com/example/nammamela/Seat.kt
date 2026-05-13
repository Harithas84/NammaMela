package com.example.nammamela

data class Seat(
    val id: String,
    var isSelected: Boolean = false,
    var isBooked: Boolean = false
)