package com.example.nammamela

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nammamela.data.BookingEntity
import java.util.Locale

class BookingAdapter(
    private var bookings: List<BookingEntity>,
    private val onBookingClick: (BookingEntity) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPlayName: TextView = view.findViewById(R.id.bookingPlayName)
        val txtDateTime: TextView = view.findViewById(R.id.bookingDateTime)
        val txtSeats: TextView = view.findViewById(R.id.bookingSeats)
        val txtPrice: TextView = view.findViewById(R.id.bookingPrice)
        val txtPayment: TextView = view.findViewById(R.id.bookingPayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.txtPlayName.text = booking.playName
        holder.txtDateTime.text = "${booking.date} | ${booking.time}"
        holder.txtSeats.text = "Seats: ${booking.seats}"
        holder.txtPrice.text = String.format(Locale.getDefault(), "Total: ₹%.2f", booking.totalPrice)
        holder.txtPayment.text = "PAID via ${booking.paymentMethod} 🎟️"
        
        holder.itemView.setOnClickListener { onBookingClick(booking) }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateData(newBookings: List<BookingEntity>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}
