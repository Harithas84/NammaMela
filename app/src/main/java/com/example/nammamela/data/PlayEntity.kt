package com.example.nammamela.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_info")
data class PlayEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val posterUrl: String,
    val duration: String,
    val date: String,
    val time: String,
    val pricePerSeat: Double,
    val leadActorName: String,
    val leadActorPhotoUrl: String,
    val comedianName: String,
    val comedianPhotoUrl: String,
    val singerName: String,
    val singerPhotoUrl: String,
    val villainName: String = "",
    val villainPhotoUrl: String = "",
    val dancerName: String = "",
    val dancerPhotoUrl: String = ""
)
