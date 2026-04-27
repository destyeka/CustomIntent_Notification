package com.example.habbittracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "habits.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                time TEXT,
                has_reminder INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE habit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                habit_id INTEGER,
                date TEXT,
                is_done INTEGER
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS habits")
        onCreate(db)
    }

    fun insertHabit(name: String, time: String?, hasReminder: Boolean): Long {
        val db = writableDatabase
        val values = ContentValues()

        values.put("name", name)
        values.put("time", time)
        values.put("has_reminder", if (hasReminder) 1 else 0)

        return db.insert("habits", null, values)
    }

    fun getAllHabits(): List<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase

        val cursor = db.rawQuery("SELECT * FROM habits", null)

        while (cursor.moveToNext()) {
            list.add(
                Habit(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3) == 1
                )
            )
        }

        cursor.close()
        return list
    }

    fun setHabitDone(habitId: Int, date: String, isDone: Boolean) {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM habit_logs WHERE habit_id=? AND date=?",
            arrayOf(habitId.toString(), date)
        )

        val values = ContentValues()
        values.put("habit_id", habitId)
        values.put("date", date)
        values.put("is_done", if (isDone) 1 else 0)

        if (cursor.count > 0) {
            db.update("habit_logs", values, "habit_id=? AND date=?", arrayOf(habitId.toString(), date))
        } else {
            db.insert("habit_logs", null, values)
        }

        cursor.close()
    }

    fun isHabitDone(habitId: Int, date: String): Boolean {
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT is_done FROM habit_logs WHERE habit_id=? AND date=?",
            arrayOf(habitId.toString(), date)
        )

        val result = if (cursor.moveToFirst()) {
            cursor.getInt(0) == 1
        } else {
            false
        }

        cursor.close()
        return result
    }

    fun getHabitsForDate(date: String): List<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase

        val query = """
        SELECT h.id, h.name, h.time, h.has_reminder
        FROM habits h
        LEFT JOIN habit_logs l
        ON h.id = l.habit_id AND l.date = ?
    """

        val cursor = db.rawQuery(query, arrayOf(date))

        while (cursor.moveToNext()) {
            list.add(
                Habit(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3) == 1
                )
            )
        }

        cursor.close()
        return list
    }

    fun deleteHabit(id: Int) {
        val db = writableDatabase
        db.delete("habits", "id=?", arrayOf(id.toString()))
    }
}
