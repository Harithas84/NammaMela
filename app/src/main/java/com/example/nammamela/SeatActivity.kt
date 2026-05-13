package com.example.nammamela

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nammamela.data.MelaDatabase
import com.example.nammamela.data.SeatEntity
import com.example.nammamela.data.BookingEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class SeatActivity : AppCompatActivity() {

    private lateinit var database: MelaDatabase
    private var seatList: MutableList<Seat> = mutableListOf()
    private lateinit var adapter: SeatAdapter
    private var playId: Int = 1
    private var playName: String = ""
    private var playDate: String = ""
    private var playTime: String = ""
    private var pricePerSeat: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat)

        playId = intent.getIntExtra("PLAY_ID", 1)
        playName = intent.getStringExtra("PLAY_NAME") ?: ""
        playDate = intent.getStringExtra("PLAY_DATE") ?: ""
        playTime = intent.getStringExtra("PLAY_TIME") ?: ""
        pricePerSeat = intent.getDoubleExtra("PLAY_PRICE", 0.0)
        
        database = MelaDatabase.getDatabase(this)

        val recycler = findViewById<RecyclerView>(R.id.seatRecycler)
        val selectedText = findViewById<TextView>(R.id.selectedCount)
        val confirmBtn = findViewById<Button>(R.id.confirmButton)
        val backBtn = findViewById<ImageView>(R.id.btnBack)

        backBtn.setOnClickListener {
            finish()
        }

        adapter = SeatAdapter(seatList) {
            updateUI(selectedText)
        }

        recycler.layoutManager = GridLayoutManager(this, 5)
        recycler.adapter = adapter

        loadSeats()

        confirmBtn.setOnClickListener {
            processPayment()
        }
    }

    private fun loadSeats() {
        lifecycleScope.launch {
            val dbSeats = database.melaDao().getSeatsForPlay(playId).first()
            if (dbSeats.isEmpty()) {
                val initialSeats = mutableListOf<SeatEntity>()
                for (i in 1..40) {
                    val row = if (i <= 10) "A" else if (i <= 20) "B" else if (i <= 30) "C" else "D"
                    val num = if (i % 10 == 0) 10 else i % 10
                    initialSeats.add(SeatEntity(playId = playId, id = "$row$num", isBooked = false))
                }
                database.melaDao().insertSeats(initialSeats)
            }

            database.melaDao().getSeatsForPlay(playId).collect { entities ->
                seatList.clear()
                entities.forEach { entity ->
                    seatList.add(Seat(id = entity.id, isBooked = entity.isBooked))
                }
                adapter.notifyDataSetChanged()
                updateUI(findViewById(R.id.selectedCount))
            }
        }
    }

    private fun processPayment() {
        val selectedSeats = seatList.filter { it.isSelected }
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Please select a seat first!", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = selectedSeats.size * pricePerSeat
        
        val options = arrayOf("UPI (PhonePe/GPay)", "Credit/Debit Card", "Net Banking", "Cash at Counter")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Payment Method")
            .setItems(options) { _, which ->
                val method = options[which]
                completeBooking(selectedSeats, totalAmount, method)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun completeBooking(selectedSeats: List<Seat>, amount: Double, method: String) {
        lifecycleScope.launch {
            selectedSeats.forEach {
                database.melaDao().updateSeatBooking(playId, it.id, true)
            }
            
            val seatString = selectedSeats.joinToString(", ") { it.id }
            val booking = BookingEntity(
                playId = playId,
                playName = playName,
                date = playDate,
                time = playTime,
                seats = seatString,
                totalPrice = amount,
                paymentMethod = method
            )
            database.melaDao().insertBooking(booking)
            
            showTicketConfirmation(selectedSeats.map { it.id }, amount, method)
        }
    }

    private fun showTicketConfirmation(seatIds: List<String>, amount: Double, method: String) {
        val seatString = seatIds.joinToString(", ")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Booking Confirmed! 🎟️")
            .setMessage("Play: $playName\nSeats: $seatString\nTotal Paid: ₹$amount\nMethod: $method\n\nShow this ticket at the theater entrance.")
            .setPositiveButton("Awesome!") { _, _ -> 
                finish() 
            }
            .setCancelable(false)
            .show()
    }

    private fun updateUI(textView: TextView) {
        val count = seatList.count { it.isSelected }
        val total = count * pricePerSeat
        textView.text = String.format(Locale.getDefault(), "Selected: %d | Total: ₹%.2f", count, total)
    }
}
