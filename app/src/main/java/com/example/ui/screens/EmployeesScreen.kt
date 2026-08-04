package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.ui.theme.StatusAvailable
import com.example.ui.theme.StatusOccupied

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    employees: List<EmployeeEntity>,
    desks: List<DeskEntity>,
    selectedEmployee: EmployeeEntity?,
    onSelectEmployee: (EmployeeEntity?) -> Unit,
    onAssignEmployee: (employeeId: String, deskId: String) -> Unit,
    onUnassignEmployee: (employeeId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var deptFilter by remember { mutableStateOf<String?>(null) }
    var showDeskModal by remember { mutableStateOf(false) }

    val filteredEmployees = employees.filter { emp ->
        val matchSearch = searchQuery.isBlank() ||
                emp.name.contains(searchQuery, ignoreCase = true) ||
                emp.email.contains(searchQuery, ignoreCase = true) ||
                emp.role.contains(searchQuery, ignoreCase = true) ||
                (emp.assignedDeskId ?: "").contains(searchQuery, ignoreCase = true)

        val matchDept = deptFilter == null || emp.department.contains(deptFilter!!, ignoreCase = true)
        matchSearch && matchDept
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Employee Directory",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${employees.size} total team members • ${employees.count { it.assignedDeskId != null }} seated",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name, role, desk...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("search_employee_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Department filter chips
                val depts = listOf("Engineering", "Product", "Design", "Sales", "Marketing", "HR", "Finance")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = deptFilter == null,
                            onClick = { deptFilter = null },
                            label = { Text("All Depts") }
                        )
                    }
                    items(depts) { dept ->
                        FilterChip(
                            selected = deptFilter == dept,
                            onClick = { deptFilter = if (deptFilter == dept) null else dept },
                            label = { Text(dept) }
                        )
                    }
                }
            }
        }

        // Employee Cards List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredEmployees) { emp ->
                val desk = desks.find { it.id == emp.assignedDeskId }

                Card(
                    onClick = { onSelectEmployee(emp) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_card_${emp.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(emp.avatarColorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emp.name.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = emp.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${emp.role} • ${emp.department}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = emp.email,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Seat assignment badge
                        if (emp.assignedDeskId != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = StatusOccupied.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Desk,
                                        contentDescription = null,
                                        tint = StatusOccupied,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = emp.assignedDeskId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = StatusOccupied
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = "Unassigned",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Selected Employee Detail Modal
    if (selectedEmployee != null) {
        val emp = selectedEmployee
        val currentDesk = desks.find { it.id == emp.assignedDeskId }

        AlertDialog(
            onDismissRequest = { onSelectEmployee(null) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(emp.avatarColorHex))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emp.name.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(emp.name, fontWeight = FontWeight.Bold)
                        Text(emp.role, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Department: ${emp.department}", fontWeight = FontWeight.Medium)
                    Text("Email: ${emp.email}", fontSize = 13.sp)

                    Divider()

                    Text("Current Seat Assignment:", fontWeight = FontWeight.Bold)
                    if (currentDesk != null) {
                        Text(
                            text = "Desk ${currentDesk.id} (Floor ${currentDesk.floor}, ${currentDesk.zone})",
                            color = StatusOccupied,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text("No seat assigned currently", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showDeskModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("modal_assign_desk_btn")
                    ) {
                        Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (emp.assignedDeskId == null) "Assign Desk" else "Relocate to New Desk")
                    }

                    if (emp.assignedDeskId != null) {
                        OutlinedButton(
                            onClick = {
                                onUnassignEmployee(emp.id)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unassign Current Seat")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onSelectEmployee(null) }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal to pick available desk for employee
    if (showDeskModal && selectedEmployee != null) {
        val emp = selectedEmployee
        var selectedDeskId by remember { mutableStateOf("") }
        val availableDesks = desks.filter { it.status == "AVAILABLE" }

        AlertDialog(
            onDismissRequest = { showDeskModal = false },
            title = { Text("Assign Desk for ${emp.name}") },
            text = {
                Column(modifier = Modifier.heightIn(max = 300.dp)) {
                    Text("Choose an available desk:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (availableDesks.isEmpty()) {
                        Text("No available desks on any floor!", color = MaterialTheme.colorScheme.error)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableDesks) { desk ->
                                val isSelected = desk.id == selectedDeskId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDeskId = desk.id }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Desk ${desk.id} (Floor ${desk.floor})", fontWeight = FontWeight.Bold)
                                            Text(desk.zone, fontSize = 12.sp)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedDeskId.isNotEmpty(),
                    onClick = {
                        onAssignEmployee(emp.id, selectedDeskId)
                        showDeskModal = false
                    }
                ) {
                    Text("Confirm Seat Allocation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeskModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
