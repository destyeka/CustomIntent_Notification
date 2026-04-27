package com.example.habbittracker

data class Habit(
    val id: Int,
    val name: String,
    val time: String?,
    val hasReminder: Boolean
)
