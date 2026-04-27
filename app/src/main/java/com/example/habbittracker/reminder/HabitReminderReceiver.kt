package com.example.habbittracker.reminder

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.habbittracker.R

class HabitReminderReceiver : BroadcastReceiver() {

    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {

        val habitId = intent.getIntExtra("habitId", -1)
        val habitName = intent.getStringExtra("habitName") ?: "Your habit"

        val channelId = "habit_channel"

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Habit Reminder")
            .setContentText("Time for: $habitName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(habitId, notification)

        val timeMillis = intent.getLongExtra("timeMillis", -1)

        if (timeMillis != -1L) {
            val nextDay = timeMillis + (24 * 60 * 60 * 1000)

            ReminderScheduler.schedule(
                context,
                nextDay,
                habitId,
                habitName
            )
        }
        Log.d("REMINDER_DEBUG", "Receiver triggered!")
    }
}