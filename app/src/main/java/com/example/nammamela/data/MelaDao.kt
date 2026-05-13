package com.example.nammamela.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MelaDao {
    @Query("SELECT * FROM seats WHERE playId = :playId")
    fun getSeatsForPlay(playId: Int): Flow<List<SeatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeats(seats: List<SeatEntity>)

    @Query("UPDATE seats SET isBooked = :isBooked WHERE id = :seatId AND playId = :playId")
    suspend fun updateSeatBooking(playId: Int, seatId: String, isBooked: Boolean)

    @Query("SELECT * FROM play_info")
    fun getAllPlays(): Flow<List<PlayEntity>>

    @Query("SELECT * FROM play_info WHERE id = :playId")
    fun getPlayById(playId: Int): Flow<PlayEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayInfo(play: PlayEntity)

    @Query("DELETE FROM play_info")
    suspend fun deleteAllPlays()

    @Query("SELECT * FROM fan_wall ORDER BY timestamp DESC")
    fun getAllComments(): Flow<List<FanWallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: FanWallEntity)

    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)
}
