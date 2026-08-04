package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desks")
data class DeskEntity(
    @PrimaryKey val id: String, // e.g. "A-101"
    val floor: Int, // 1, 2, 3
    val zone: String, // "Zone A - Engineering", "Zone B - Product", etc.
    val row: Int,
    val col: Int,
    val status: String, // "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE"
    val assignedEmployeeId: String? = null,
    val hasStandingDesk: Boolean = false,
    val hasDualMonitors: Boolean = false,
    val isNearWindow: Boolean = false,
    val isErgonomic: Boolean = false
)
