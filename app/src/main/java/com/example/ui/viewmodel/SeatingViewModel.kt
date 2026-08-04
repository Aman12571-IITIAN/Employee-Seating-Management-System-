package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiResponse
import com.example.ai.GeminiSeatingAssistant
import com.example.ai.ProposedAction
import com.example.data.database.SeatingDatabase
import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.SeatingLogEntity
import com.example.data.repository.SeatingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SeatingTab {
    FLOOR_PLAN,
    EMPLOYEES,
    AI_ASSISTANT,
    ANALYTICS,
    AUDIT_LOGS
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val proposedActions: List<ProposedAction> = emptyList(),
    val isApplied: Boolean = false,
    val isGemini: Boolean = false
)

class SeatingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SeatingDatabase.getInstance(application)
    private val repository = SeatingRepository(db.seatingDao())

    val allDesks: StateFlow<List<DeskEntity>> = repository.allDesks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEmployees: StateFlow<List<EmployeeEntity>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<SeatingLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation state
    private val _currentTab = MutableStateFlow(SeatingTab.FLOOR_PLAN)
    val currentTab: StateFlow<SeatingTab> = _currentTab.asStateFlow()

    // Filters
    private val _selectedFloor = MutableStateFlow(1)
    val selectedFloor: StateFlow<Int> = _selectedFloor.asStateFlow()

    private val _selectedDeptFilter = MutableStateFlow<String?>(null)
    val selectedDeptFilter: StateFlow<String?> = _selectedDeptFilter.asStateFlow()

    private val _selectedAmenityFilter = MutableStateFlow<String?>(null)
    val selectedAmenityFilter: StateFlow<String?> = _selectedAmenityFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selection dialog states
    private val _selectedDesk = MutableStateFlow<DeskEntity?>(null)
    val selectedDesk: StateFlow<DeskEntity?> = _selectedDesk.asStateFlow()

    private val _selectedEmployee = MutableStateFlow<EmployeeEntity?>(null)
    val selectedEmployee: StateFlow<EmployeeEntity?> = _selectedEmployee.asStateFlow()

    // AI Assistant States
    private val _aiPromptInput = MutableStateFlow("")
    val aiPromptInput: StateFlow<String> = _aiPromptInput.asStateFlow()

    private val _aiIsProcessing = MutableStateFlow(false)
    val aiIsProcessing: StateFlow<Boolean> = _aiIsProcessing.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "Hello! I'm your AI Seating Allocation Assistant. You can ask me to move employees, reserve desks, or swap seat assignments. Try prompts like:\n• 'Move Sarah Jenkins to a standing desk near window'\n• 'Assign Alex Rivera to desk A-105'\n• 'Swap seats between David and Michael'",
                proposedActions = emptyList()
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _pendingAiActions = MutableStateFlow<List<ProposedAction>?>(null)
    val pendingAiActions: StateFlow<List<ProposedAction>?> = _pendingAiActions.asStateFlow()

    private val _pendingChatMessageId = MutableStateFlow<String?>(null)

    // Filtered Desks computed flow
    val filteredDesks: StateFlow<List<DeskEntity>> = combine(
        allDesks,
        _selectedFloor,
        _selectedDeptFilter,
        _selectedAmenityFilter,
        _searchQuery
    ) { desks, floor, dept, amenity, query ->
        desks.filter { desk ->
            val matchFloor = desk.floor == floor
            val matchDept = dept == null || desk.zone.contains(dept, ignoreCase = true)
            val matchAmenity = when (amenity) {
                "STANDING" -> desk.hasStandingDesk
                "DUAL_MONITOR" -> desk.hasDualMonitors
                "WINDOW" -> desk.isNearWindow
                "ERGONOMIC" -> desk.isErgonomic
                else -> true
            }
            val matchQuery = query.isBlank() || desk.id.contains(query, ignoreCase = true) || desk.zone.contains(query, ignoreCase = true)
            matchFloor && matchDept && matchAmenity && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    fun setTab(tab: SeatingTab) {
        _currentTab.value = tab
    }

    fun selectFloor(floor: Int) {
        _selectedFloor.value = floor
    }

    fun setDeptFilter(dept: String?) {
        _selectedDeptFilter.value = if (_selectedDeptFilter.value == dept) null else dept
    }

    fun setAmenityFilter(amenity: String?) {
        _selectedAmenityFilter.value = if (_selectedAmenityFilter.value == amenity) null else amenity
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectDesk(desk: DeskEntity?) {
        _selectedDesk.value = desk
    }

    fun selectEmployee(emp: EmployeeEntity?) {
        _selectedEmployee.value = emp
    }

    fun updateAiPrompt(text: String) {
        _aiPromptInput.value = text
    }

    fun sendAiPrompt(promptText: String) {
        if (promptText.isBlank()) return

        val userMsg = ChatMessage(isUser = true, text = promptText)
        _chatMessages.value = _chatMessages.value + userMsg
        _aiPromptInput.value = ""
        _aiIsProcessing.value = true

        viewModelScope.launch {
            val response = GeminiSeatingAssistant.processPrompt(
                userPrompt = promptText,
                allDesks = allDesks.value,
                allEmployees = allEmployees.value
            )

            val botMsg = ChatMessage(
                isUser = false,
                text = response.summaryText,
                proposedActions = response.proposedActions,
                isGemini = response.isGeminiPowered
            )

            _chatMessages.value = _chatMessages.value + botMsg
            _aiIsProcessing.value = false

            if (response.proposedActions.isNotEmpty()) {
                _pendingAiActions.value = response.proposedActions
                _pendingChatMessageId.value = botMsg.id
            }
        }
    }

    fun applyAiActions(actions: List<ProposedAction>, messageId: String? = null) {
        viewModelScope.launch {
            actions.forEach { action ->
                when (action.type) {
                    "ASSIGN", "RELOCATE" -> {
                        if (action.targetEmployeeId != null && action.toDeskId != null) {
                            repository.assignOrRelocateEmployee(
                                employeeId = action.targetEmployeeId,
                                newDeskId = action.toDeskId,
                                performedBy = "Admin via AI Assistant",
                                note = action.reason
                            )
                        }
                    }
                    "UNASSIGN" -> {
                        if (action.targetEmployeeId != null) {
                            repository.unassignEmployee(
                                employeeId = action.targetEmployeeId,
                                performedBy = "Admin via AI Assistant",
                                note = action.reason
                            )
                        }
                    }
                    "SWAP" -> {
                        if (action.targetEmployeeId != null && action.secondEmployeeId != null) {
                            repository.swapSeats(
                                empId1 = action.targetEmployeeId,
                                empId2 = action.secondEmployeeId,
                                performedBy = "Admin via AI Assistant",
                                note = action.reason
                            )
                        }
                    }
                    "RESERVE" -> {
                        if (action.toDeskId != null) {
                            repository.setDeskStatus(
                                deskId = action.toDeskId,
                                newStatus = "RESERVED",
                                performedBy = "Admin via AI Assistant",
                                note = action.reason
                            )
                        }
                    }
                }
            }

            // Mark message as applied in chat history
            val targetId = messageId ?: _pendingChatMessageId.value
            if (targetId != null) {
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == targetId) msg.copy(isApplied = true) else msg
                }
            }

            _pendingAiActions.value = null
            _pendingChatMessageId.value = null
        }
    }

    fun dismissPendingAiActions() {
        _pendingAiActions.value = null
        _pendingChatMessageId.value = null
    }

    fun assignEmployeeToDesk(employeeId: String, deskId: String) {
        viewModelScope.launch {
            repository.assignOrRelocateEmployee(employeeId, deskId, "Admin Manual")
            _selectedDesk.value = null
            _selectedEmployee.value = null
        }
    }

    fun unassignEmployee(employeeId: String) {
        viewModelScope.launch {
            repository.unassignEmployee(employeeId, "Admin Manual")
            _selectedDesk.value = null
            _selectedEmployee.value = null
        }
    }

    fun setDeskStatus(deskId: String, status: String) {
        viewModelScope.launch {
            repository.setDeskStatus(deskId, status, "Admin Manual")
            _selectedDesk.value = null
        }
    }

    fun resetToBlueprint() {
        viewModelScope.launch {
            repository.resetToInitialBlueprint()
            _chatMessages.value = listOf(
                ChatMessage(
                    isUser = false,
                    text = "System reset to initial office seating blueprint. How can I help you today?",
                    proposedActions = emptyList()
                )
            )
        }
    }
}
