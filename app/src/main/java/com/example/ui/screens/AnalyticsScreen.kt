package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusAvailable
import com.example.ui.theme.StatusOccupied
import com.example.ui.theme.StatusReserved

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    desks: List<DeskEntity>,
    employees: List<EmployeeEntity>
) {
    val totalCapacity = desks.size
    val occupiedCount = desks.count { it.status == "OCCUPIED" }
    val availableCount = desks.count { it.status == "AVAILABLE" }
    val reservedCount = desks.count { it.status == "RESERVED" }
    val maintenanceCount = desks.count { it.status == "MAINTENANCE" }

    val occupancyPercentage = if (totalCapacity > 0) (occupiedCount.toFloat() / totalCapacity) else 0f

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
                    text = "Seating Analytics & Insights",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Real-time occupancy and amenity metrics across 3 floors",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Summary Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Occupancy Rate",
                        value = "${(occupancyPercentage * 100).toInt()}%",
                        subtitle = "$occupiedCount / $totalCapacity Desks",
                        color = StatusOccupied,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Available Desks",
                        value = "$availableCount",
                        subtitle = "Ready for allocation",
                        color = StatusAvailable,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Reserved",
                        value = "$reservedCount",
                        subtitle = "Upcoming hires/guests",
                        color = StatusReserved,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Maintenance",
                        value = "$maintenanceCount",
                        subtitle = "Out of service",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Overall Occupancy Progress Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Overall Facility Occupancy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = occupancyPercentage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = StatusOccupied,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${occupiedCount} Occupied", fontSize = 12.sp, color = StatusOccupied, fontWeight = FontWeight.Bold)
                            Text("${availableCount} Vacant", fontSize = 12.sp, color = StatusAvailable, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Department Breakdown
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Department Seat Distribution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val departments = listOf("Engineering", "Product", "Design", "Sales", "Marketing", "HR", "Finance")
                        departments.forEach { dept ->
                            val empInDept = employees.filter { it.department.contains(dept, ignoreCase = true) }
                            val seatedCount = empInDept.count { it.assignedDeskId != null }

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(dept, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("$seatedCount / ${empInDept.size} Seated", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val deptRatio = if (empInDept.isNotEmpty()) seatedCount.toFloat() / empInDept.size else 0f
                                LinearProgressIndicator(
                                    progress = deptRatio,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = PrimaryBlue,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Amenity Utilization
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Desk Amenity Utilization",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val standingDesks = desks.filter { it.hasStandingDesk }
                        val standingOccupied = standingDesks.count { it.status == "OCCUPIED" }

                        val dualMonitors = desks.filter { it.hasDualMonitors }
                        val dualOccupied = dualMonitors.count { it.status == "OCCUPIED" }

                        val windowDesks = desks.filter { it.isNearWindow }
                        val windowOccupied = windowDesks.count { it.status == "OCCUPIED" }

                        AmenityUsageRow(
                            icon = Icons.Outlined.Height,
                            name = "Standing Desks",
                            used = standingOccupied,
                            total = standingDesks.size
                        )
                        AmenityUsageRow(
                            icon = Icons.Outlined.DesktopWindows,
                            name = "Dual Monitors",
                            used = dualOccupied,
                            total = dualMonitors.size
                        )
                        AmenityUsageRow(
                            icon = Icons.Outlined.WbSunny,
                            name = "Window View Seats",
                            used = windowOccupied,
                            total = windowDesks.size
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun AmenityUsageRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    used: Int,
    total: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(name, fontWeight = FontWeight.Medium)
        }
        Text("$used / $total Occupied", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 13.sp)
    }
}
