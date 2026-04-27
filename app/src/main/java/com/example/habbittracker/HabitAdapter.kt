package com.example.habbittracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitAdapter(
    private val list: List<Habit>,
    private val db: DatabaseHelper,
    private val selectedDate: String,
    private val onDelete: (Habit) -> Unit
) :
    RecyclerView.Adapter<HabitAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvName)
        val time: TextView = view.findViewById(R.id.tvTime)
        val check: CheckBox = view.findViewById(R.id.checkDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val habit = list[position]

        holder.name.text = habit.name

        if (habit.time.isNullOrEmpty()) {
            holder.time.visibility = View.GONE
        } else {
            holder.time.visibility = View.VISIBLE
            holder.time.text = habit.time
        }

        val date = selectedDate

        holder.check.setOnCheckedChangeListener(null)

        holder.check.isChecked = db.isHabitDone(habit.id, date)

        holder.check.setOnCheckedChangeListener { _, isChecked ->
            db.setHabitDone(habit.id, date, isChecked)
        }

        holder.itemView.setOnLongClickListener {
            onDelete(habit)
            true
        }
    }
}