package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String, // e.g. "EMP-001"
    val name: String,
    val email: String,
    val department: String, // "Engineering", "Product", "Design", "Sales", "Marketing", "HR", "Finance"
    val role: String,
    val avatarColorHex: String = "#3B82F6",
    val assignedDeskId: String? = null
)
