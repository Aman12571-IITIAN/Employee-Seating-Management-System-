package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seating_logs")
data class SeatingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val employeeName: String,
    val oldDeskId: String? = null,
    val newDeskId: String? = null,
    val actionType: String, // "ASSIGN", "RELOCATE", "UNASSIGN", "SWAP", "RESERVE"
    val performedBy: String = "Admin via AI Assistant",
    val note: String? = null
)
