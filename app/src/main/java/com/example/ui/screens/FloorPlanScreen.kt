package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanScreen(
    desks: List<DeskEntity>,
    employees: List<EmployeeEntity>,
    selectedFloor: Int,
    selectedDeptFilter: String?,
    selectedAmenityFilter: String?,
    searchQuery: String,
    selectedDesk: DeskEntity?,
    onSelectFloor: (Int) -> Unit,
    onSetDeptFilter: (String?) -> Unit,
    onSetAmenityFilter: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectDesk: (DeskEntity?) -> Unit,
    onAssignEmployee: (employeeId: String, deskId: String) -> Unit,
    onUnassignEmployee: (employeeId: String) -> Unit,
    onSetDeskStatus: (deskId: String, status: String) -> Unit
) {
    var showAssignModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Office Floor Plan",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Floor $selectedFloor • ${desks.count { it.status == "AVAILABLE" }} Desks Free",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Floor Selector Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        listOf(1, 2, 3).forEach { floor ->
                            val isSelected = selectedFloor == floor
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { onSelectFloor(floor) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("floor_pill_$floor"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "L$floor",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search desk ID or zone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("search_desk_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedDeptFilter == "Engineering",
                            onClick = { onSetDeptFilter("Engineering") },
                            label = { Text("Engineering") },
                            leadingIcon = { Icon(Icons.Outlined.Code, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("filter_engineering")
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedDeptFilter == "Product",
                            onClick = { onSetDeptFilter("Product") },
                            label = { Text("Product & Design") },
                            leadingIcon = { Icon(Icons.Outlined.Brush, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedDeptFilter == "Sales",
                            onClick = { onSetDeptFilter("Sales") },
                            label = { Text("Sales & Mktg") },
                            leadingIcon = { Icon(Icons.Outlined.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedAmenityFilter == "STANDING",
                            onClick = { onSetAmenityFilter("STANDING") },
                            label = { Text("Standing Desk") },
                            leadingIcon = { Icon(Icons.Outlined.Height, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedAmenityFilter == "DUAL_MONITOR",
                            onClick = { onSetAmenityFilter("DUAL_MONITOR") },
                            label = { Text("Dual Monitors") },
                            leadingIcon = { Icon(Icons.Outlined.DesktopWindows, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedAmenityFilter == "WINDOW",
                            onClick = { onSetAmenityFilter("WINDOW") },
                            label = { Text("Window View") },
                            leadingIcon = { Icon(Icons.Outlined.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        // Legend Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendIndicator(color = StatusAvailable, label = "Available")
            LegendIndicator(color = StatusOccupied, label = "Occupied")
            LegendIndicator(color = StatusReserved, label = "Reserved")
            LegendIndicator(color = StatusMaintenance, label = "Maintenance")
        }

        // Floor Grid
        if (desks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Desk,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No desks found matching filters on Floor $selectedFloor",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Group desks by Zone
            val groupedZones = desks.groupBy { it.zone }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                groupedZones.forEach { (zoneName, zoneDesks) ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Domain,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = zoneName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Text(
                                        text = "${zoneDesks.count { it.status == "AVAILABLE" }}/${zoneDesks.size} Free",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Grid of desks in this zone
                                val columns = 2
                                val rows = (zoneDesks.size + columns - 1) / columns
                                for (r in 0 until rows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        for (c in 0 until columns) {
                                            val index = r * columns + c
                                            if (index < zoneDesks.size) {
                                                val desk = zoneDesks[index]
                                                val assignedEmp = employees.find { it.id == desk.assignedEmployeeId }

                                                Box(modifier = Modifier.weight(1f)) {
                                                    DeskItemCard(
                                                        desk = desk,
                                                        assignedEmployee = assignedEmp,
                                                        onClick = { onSelectDesk(desk) }
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                    if (r < rows - 1) Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Selected Desk Details Dialog / Bottom Sheet
    if (selectedDesk != null) {
        val desk = selectedDesk
        val assignedEmp = employees.find { it.id == desk.assignedEmployeeId }

        AlertDialog(
            onDismissRequest = { onSelectDesk(null) },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when (desk.status) {
                                        "AVAILABLE" -> StatusAvailable
                                        "OCCUPIED" -> StatusOccupied
                                        "RESERVED" -> StatusReserved
                                        else -> StatusMaintenance
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desk ${desk.id}", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Floor ${desk.floor}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = desk.zone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    // Occupancy Status
                    if (assignedEmp != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(assignedEmp.avatarColorHex))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = assignedEmp.name.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = assignedEmp.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${assignedEmp.role} • ${assignedEmp.department}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Status: ${desk.status}",
                            fontWeight = FontWeight.SemiBold,
                            color = when (desk.status) {
                                "AVAILABLE" -> StatusAvailable
                                "RESERVED" -> StatusReserved
                                else -> StatusMaintenance
                            }
                        )
                    }

                    // Desk Amenities
                    Text("Desk Amenities", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (desk.hasStandingDesk) AmenityBadge(icon = Icons.Outlined.Height, label = "Standing")
                        if (desk.hasDualMonitors) AmenityBadge(icon = Icons.Outlined.DesktopWindows, label = "Dual 4K")
                        if (desk.isNearWindow) AmenityBadge(icon = Icons.Outlined.WbSunny, label = "Window")
                        if (desk.isErgonomic) AmenityBadge(icon = Icons.Outlined.Chair, label = "Ergo Chair")
                    }
                }
            },
            confirmButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (assignedEmp != null) {
                        Button(
                            onClick = {
                                showAssignModal = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("relocate_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Relocate Employee")
                        }
                        OutlinedButton(
                            onClick = {
                                onUnassignEmployee(assignedEmp.id)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PersonRemove, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unassign Seat")
                        }
                    } else {
                        Button(
                            onClick = { showAssignModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("assign_seat_btn")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Assign Employee Here")
                        }
                        if (desk.status == "AVAILABLE") {
                            OutlinedButton(
                                onClick = { onSetDeskStatus(desk.id, "RESERVED") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Bookmark, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reserve Desk")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSetDeskStatus(desk.id, "AVAILABLE") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mark as Available")
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onSelectDesk(null) }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal to pick employee for assignment
    if (showAssignModal && selectedDesk != null) {
        val desk = selectedDesk
        var selectedEmpId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAssignModal = false },
            title = { Text("Assign Seat: ${desk.id}") },
            text = {
                Column(modifier = Modifier.heightIn(max = 300.dp)) {
                    Text("Select an employee to allocate to desk ${desk.id}:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(employees) { emp ->
                            val isSelected = emp.id == selectedEmpId
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedEmpId = emp.id }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(emp.name, fontWeight = FontWeight.Bold)
                                        Text("${emp.department} • ${emp.assignedDeskId ?: "Unassigned"}", fontSize = 12.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedEmpId.isNotEmpty(),
                    onClick = {
                        onAssignEmployee(selectedEmpId, desk.id)
                        showAssignModal = false
                    }
                ) {
                    Text("Confirm Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DeskItemCard(
    desk: DeskEntity,
    assignedEmployee: EmployeeEntity?,
    onClick: () -> Unit
) {
    val statusColor = when (desk.status) {
        "AVAILABLE" -> StatusAvailable
        "OCCUPIED" -> StatusOccupied
        "RESERVED" -> StatusReserved
        else -> StatusMaintenance
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("desk_card_${desk.id}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = desk.id,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = desk.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Employee Avatar or Available placeholder
            if (assignedEmployee != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(assignedEmployee.avatarColorHex))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = assignedEmployee.name.take(2).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assignedEmployee.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = if (desk.status == "AVAILABLE") "Vacant Desk" else "No Employee",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Amenity Icon Dots
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (desk.hasStandingDesk) Icon(Icons.Outlined.Height, contentDescription = "Standing", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                if (desk.hasDualMonitors) Icon(Icons.Outlined.DesktopWindows, contentDescription = "Dual Monitors", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                if (desk.isNearWindow) Icon(Icons.Outlined.WbSunny, contentDescription = "Window", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AmenityBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
