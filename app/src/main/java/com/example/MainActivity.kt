package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SeatAssignTheme
import com.example.ui.viewmodel.SeatingTab
import com.example.ui.viewmodel.SeatingViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SeatingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SeatAssignTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val allDesks by viewModel.allDesks.collectAsState()
                val filteredDesks by viewModel.filteredDesks.collectAsState()
                val allEmployees by viewModel.allEmployees.collectAsState()
                val allLogs by viewModel.allLogs.collectAsState()

                val selectedFloor by viewModel.selectedFloor.collectAsState()
                val selectedDeptFilter by viewModel.selectedDeptFilter.collectAsState()
                val selectedAmenityFilter by viewModel.selectedAmenityFilter.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()

                val selectedDesk by viewModel.selectedDesk.collectAsState()
                val selectedEmployee by viewModel.selectedEmployee.collectAsState()

                val aiPromptInput by viewModel.aiPromptInput.collectAsState()
                val aiIsProcessing by viewModel.aiIsProcessing.collectAsState()
                val chatMessages by viewModel.chatMessages.collectAsState()
                val pendingAiActions by viewModel.pendingAiActions.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == SeatingTab.FLOOR_PLAN,
                                onClick = { viewModel.setTab(SeatingTab.FLOOR_PLAN) },
                                icon = {
                                    Icon(
                                        if (currentTab == SeatingTab.FLOOR_PLAN) Icons.Filled.GridOn else Icons.Outlined.GridOn,
                                        contentDescription = "Floor Plan"
                                    )
                                },
                                label = { Text("Floor Plan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_floor_plan")
                            )

                            NavigationBarItem(
                                selected = currentTab == SeatingTab.EMPLOYEES,
                                onClick = { viewModel.setTab(SeatingTab.EMPLOYEES) },
                                icon = {
                                    Icon(
                                        if (currentTab == SeatingTab.EMPLOYEES) Icons.Filled.Badge else Icons.Outlined.Badge,
                                        contentDescription = "Employees"
                                    )
                                },
                                label = { Text("Employees", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_employees")
                            )

                            NavigationBarItem(
                                selected = currentTab == SeatingTab.AI_ASSISTANT,
                                onClick = { viewModel.setTab(SeatingTab.AI_ASSISTANT) },
                                icon = {
                                    BadgedBox(
                                        badge = {
                                            if (pendingAiActions != null) {
                                                Badge { Text("1") }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = "AI Assistant",
                                            tint = AccentPurple
                                        )
                                    }
                                },
                                label = { Text("AI Assistant", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_ai_assistant")
                            )

                            NavigationBarItem(
                                selected = currentTab == SeatingTab.ANALYTICS,
                                onClick = { viewModel.setTab(SeatingTab.ANALYTICS) },
                                icon = {
                                    Icon(
                                        if (currentTab == SeatingTab.ANALYTICS) Icons.Filled.Insights else Icons.Outlined.Insights,
                                        contentDescription = "Analytics"
                                    )
                                },
                                label = { Text("Analytics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_analytics")
                            )

                            NavigationBarItem(
                                selected = currentTab == SeatingTab.AUDIT_LOGS,
                                onClick = { viewModel.setTab(SeatingTab.AUDIT_LOGS) },
                                icon = {
                                    Icon(
                                        if (currentTab == SeatingTab.AUDIT_LOGS) Icons.Filled.History else Icons.Outlined.History,
                                        contentDescription = "Audit Logs"
                                    )
                                },
                                label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_history")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            SeatingTab.FLOOR_PLAN -> {
                                FloorPlanScreen(
                                    desks = filteredDesks,
                                    employees = allEmployees,
                                    selectedFloor = selectedFloor,
                                    selectedDeptFilter = selectedDeptFilter,
                                    selectedAmenityFilter = selectedAmenityFilter,
                                    searchQuery = searchQuery,
                                    selectedDesk = selectedDesk,
                                    onSelectFloor = viewModel::selectFloor,
                                    onSetDeptFilter = viewModel::setDeptFilter,
                                    onSetAmenityFilter = viewModel::setAmenityFilter,
                                    onSearchQueryChange = viewModel::setSearchQuery,
                                    onSelectDesk = viewModel::selectDesk,
                                    onAssignEmployee = viewModel::assignEmployeeToDesk,
                                    onUnassignEmployee = viewModel::unassignEmployee,
                                    onSetDeskStatus = viewModel::setDeskStatus
                                )
                            }
                            SeatingTab.EMPLOYEES -> {
                                EmployeesScreen(
                                    employees = allEmployees,
                                    desks = allDesks,
                                    selectedEmployee = selectedEmployee,
                                    onSelectEmployee = viewModel::selectEmployee,
                                    onAssignEmployee = viewModel::assignEmployeeToDesk,
                                    onUnassignEmployee = viewModel::unassignEmployee
                                )
                            }
                            SeatingTab.AI_ASSISTANT -> {
                                AiAssistantScreen(
                                    chatMessages = chatMessages,
                                    promptInput = aiPromptInput,
                                    isProcessing = aiIsProcessing,
                                    pendingAiActions = pendingAiActions,
                                    onPromptChange = viewModel::updateAiPrompt,
                                    onSendPrompt = viewModel::sendAiPrompt,
                                    onApplyActions = viewModel::applyAiActions,
                                    onDismissActions = viewModel::dismissPendingAiActions
                                )
                            }
                            SeatingTab.ANALYTICS -> {
                                AnalyticsScreen(
                                    desks = allDesks,
                                    employees = allEmployees
                                )
                            }
                            SeatingTab.AUDIT_LOGS -> {
                                AuditLogsScreen(
                                    logs = allLogs,
                                    onResetToBlueprint = viewModel::resetToBlueprint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
