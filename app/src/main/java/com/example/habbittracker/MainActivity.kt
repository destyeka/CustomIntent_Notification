package com.example.habbittracker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.habbittracker.reminder.ReminderScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitAdapter
    private val list = mutableListOf<Habit>()

    private var selectedDate: String = ""

    @SuppressLint("ScheduleExactAlarm")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        setContentView(R.layout.activity_main)
        window.statusBarColor = getColor(R.color.bg_main)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true

        val root = findViewById<View>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets


        }

        db = DatabaseHelper(this)

        val habits = db.getAllHabits()

        for (habit in habits) {
            if (habit.hasReminder && habit.time != null) {

                val timeMillis = getTimeInMillis(habit.time)

                ReminderScheduler.schedule(
                    context = this,
                    timeMillis = timeMillis,
                    habitId = habit.id,
                    habitName = habit.name
                )
            }
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HabitAdapter(list, db, selectedDate) { habit ->
            showDeleteDialog(habit)
        }
        recyclerView.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fabAdd)
        fab.setOnClickListener {
            startActivity(Intent(this, AddHabitActivity::class.java))
        }

        generateCalendar()
    }

    override fun onResume() {
        super.onResume()

        if (selectedDate.isNotEmpty()) {
            loadHabitsForDate(selectedDate)
        } else {
            loadData()
        }
    }

    private fun showDeleteDialog(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete this habit?")
            .setPositiveButton("Yes") { _, _ ->
                ReminderScheduler.cancel(this, habit.id.toLong())
                db.deleteHabit(habit.id)

                if (selectedDate.isNotEmpty()) {
                    loadHabitsForDate(selectedDate)
                } else {
                    loadData()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadData() {
        list.clear()
        list.addAll(db.getAllHabits())
        adapter.notifyDataSetChanged()
    }

    private fun loadHabitsForDate(date: String) {
        list.clear()
        list.addAll(db.getAllHabits())

        adapter = HabitAdapter(list, db, date) { habit ->
            showDeleteDialog(habit)
        }

        recyclerView.adapter = adapter
    }

    private fun generateCalendar() {

        val calendarLayout = findViewById<LinearLayout>(R.id.calendarLayout)
        calendarLayout.removeAllViews()

        val sdfFull = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in -3..3) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, i)

            val view = layoutInflater.inflate(R.layout.item_calendar, calendarLayout, false)

            val tvDay = view.findViewById<TextView>(R.id.tvDay)
            val tvDate = view.findViewById<TextView>(R.id.tvDate)

            val dateStr = sdfFull.format(cal.time)

            tvDay.text = sdfDay.format(cal.time)
            tvDate.text = cal.get(Calendar.DAY_OF_MONTH).toString()

            if (i == 0) {
                view.isSelected = true
                selectedDate = dateStr
            }

            view.setOnClickListener {

                for (j in 0 until calendarLayout.childCount) {
                    calendarLayout.getChildAt(j).isSelected = false
                }

                view.isSelected = true
                selectedDate = dateStr

                loadHabitsForDate(selectedDate)
            }

            calendarLayout.addView(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "habit_channel",
            "Habit Reminder",
            NotificationManager.IMPORTANCE_HIGH
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun getTimeInMillis(time: String): Long {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }


}