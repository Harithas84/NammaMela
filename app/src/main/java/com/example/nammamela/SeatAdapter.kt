package com.example.nammamela

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class SeatAdapter(
    private val seats: List<Seat>,
    private val onChange: () -> Unit
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

    class SeatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.seatButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {

        val seat = seats[position]

        // 🎨 UI STATES
        when {
            seat.isBooked -> {
                holder.button.setBackgroundColor(Color.parseColor("#9E9E9E"))
                holder.button.isEnabled = false
            }

            seat.isSelected -> {
                holder.button.setBackgroundColor(Color.parseColor("#F44336"))
                holder.button.isEnabled = true
            }

            else -> {
                holder.button.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.button.isEnabled = true
            }
        }

        holder.button.text = seat.id

        // 🖱 CLICK
        holder.button.setOnClickListener {

            if (seat.isBooked) return@setOnClickListener

            // animation
            holder.button.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(80)
                .withEndAction {
                    holder.button.animate().scaleX(1f).scaleY(1f).start()
                }.start()

            seat.isSelected = !seat.isSelected
            notifyItemChanged(position)

            onChange()
        }
    }

    override fun getItemCount(): Int = seats.size
}