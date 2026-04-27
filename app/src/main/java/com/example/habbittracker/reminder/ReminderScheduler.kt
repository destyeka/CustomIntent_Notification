package com.example.habbittracker.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

object ReminderScheduler {

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun schedule(
        context: Context,
        timeMillis: Long,
        habitId: Int,
        habitName: String
    ) {

        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("habitId", habitId)
            putExtra("habitName", habitName)
            putExtra("timeMillis", timeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeMillis,
            pendingIntent
        )
    }

    fun cancel(context: Context, habitId: Long) {

        val intent = Intent(context, HabitReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}