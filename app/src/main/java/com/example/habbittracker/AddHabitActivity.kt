package com.example.habbittracker

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Switch
import java.util.Calendar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.habbittracker.reminder.ReminderScheduler

class AddHabitActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    @SuppressLint("ServiceCast", "UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        window.statusBarColor = getColor(R.color.bg_main)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true

        val root = findViewById<View>(R.id.addRoot)

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

        val etName = findViewById<EditText>(R.id.etName)

        val etTime = findViewById<EditText>(R.id.etTime)

        val switchReminder = findViewById<Switch>(R.id.switchReminder)

        etTime.isEnabled = false
        etTime.alpha = 0.5f

        switchReminder.setOnCheckedChangeListener { _, isChecked ->
            etTime.isEnabled = isChecked
            etTime.alpha = if (isChecked) 1f else 0.5f

            if (!isChecked) {
                etTime.text.clear()
            }
        }

        etTime.setOnClickListener {

            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(
                this,
                R.style.CustomTimePickerTheme,
                { _, selectedHour, selectedMinute ->

                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    etTime.setText(formattedTime)

                },
                hour,
                minute,
                true // true = 24h format, false = AM/PM
            )

            timePicker.show()
        }


        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {

            val name = etName.text.toString()
            val isReminderOn = switchReminder.isChecked
            val time = if (isReminderOn) etTime.text.toString() else null

            if (isReminderOn && time.isNullOrEmpty()) {
                etTime.error = "Pick a time"
                return@setOnClickListener
            }

            val habitId = db.insertHabit(name, time, isReminderOn).toInt()

            if (isReminderOn && time != null) {

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms()
                ) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    return@setOnClickListener
                }

                val timeMillis = getTimeInMillis(time)

                ReminderScheduler.schedule(
                    context = this,
                    timeMillis = timeMillis,
                    habitId = habitId,
                    habitName = name
                )
            }

            finish()
        }

        val btnCancel = findViewById<Button>(R.id.btnCancel)

        btnCancel.setOnClickListener {
            finish()
        }
    }

    fun getTimeInMillis(time: String): Long {
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