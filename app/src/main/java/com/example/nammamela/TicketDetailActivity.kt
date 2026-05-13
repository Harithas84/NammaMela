package com.example.nammamela

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nammamela.data.MelaDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class TicketDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_detail)

        val bookingId = intent.getIntExtra("BOOKING_ID", -1)
        if (bookingId == -1) {
            finish()
            return
        }

        findViewById<ImageView>(R.id.btnBackDetail).setOnClickListener { finish() }

        loadBookingDetails(bookingId)
    }

    private fun loadBookingDetails(id: Int) {
        val database = MelaDatabase.getDatabase(this)
        
        CoroutineScope(Dispatchers.IO).launch {
            val bookings = database.melaDao().getAllBookings().first()
            val booking = bookings.find { it.id == id }
            
            booking?.let { b ->
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.detailPlayName).text = b.playName
                    findViewById<TextView>(R.id.detailDate).text = b.date
                    findViewById<TextView>(R.id.detailTime).text = b.time
                    findViewById<TextView>(R.id.detailSeats).text = b.seats
                    findViewById<TextView>(R.id.detailPrice).text = 
                        String.format(Locale.getDefault(), "Total Paid: ₹%.2f", b.totalPrice)
                    
                    generateQRCode("${b.playName}|${b.seats}|${b.timestamp}")
                }
            }
        }
    }

    private fun generateQRCode(content: String) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            findViewById<ImageView>(R.id.imgQRCode).setImageBitmap(bmp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
