package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.ai.ProposedAction
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StatusAvailable
import com.example.ui.viewmodel.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    chatMessages: List<ChatMessage>,
    promptInput: String,
    isProcessing: Boolean,
    pendingAiActions: List<ProposedAction>?,
    onPromptChange: (String) -> Unit,
    onSendPrompt: (String) -> Unit,
    onApplyActions: (List<ProposedAction>, String?) -> Unit,
    onDismissActions: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Panel Top Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI Seating Command Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Powered by Gemini 3.5 Flash",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Admin Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Chat Message Thread
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(chatMessages) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onApplyActions = { actions -> onApplyActions(actions, msg.id) }
                )
            }

            if (isProcessing) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini is optimizing seating allocations...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Suggested Prompts:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            val suggestions = listOf(
                "Move Sarah Jenkins to a standing desk near window",
                "Assign Alex Rivera to desk A-105",
                "Swap seats between David Chen and Michael Chang",
                "Reserve desk C-105 for incoming intern",
                "Unassign Michael Chang"
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionChip(
                        onClick = { onSendPrompt(suggestion) },
                        label = { Text(suggestion, fontSize = 12.sp) },
                        icon = { Icon(Icons.Outlined.TipsAndUpdates, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }

            Divider()

            // Prompt Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = onPromptChange,
                    placeholder = { Text("Type seating instruction (e.g. Move John to B-102)...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    trailingIcon = {
                        if (promptInput.isNotEmpty()) {
                            IconButton(onClick = { onPromptChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = { onSendPrompt(promptInput) },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_send_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send prompt")
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onApplyActions: (List<ProposedAction>) -> Unit
) {
    Column(
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    if (!message.isUser && message.isGemini) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✨ Powered by Gemini 3.5 Flash",
                            fontSize = 10.sp,
                            color = AccentPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Proposed Actions Card
        if (!message.isUser && message.proposedActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(start = 40.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Proposed Allocation Actions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (message.isApplied) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusAvailable.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Applied ✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusAvailable,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    message.proposedActions.forEach { action ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (action.type) {
                                            "ASSIGN" -> StatusAvailable
                                            "RELOCATE" -> PrimaryBlue
                                            "SWAP" -> AccentPurple
                                            else -> MaterialTheme.colorScheme.error
                                        }
                                    ) {
                                        Text(
                                            text = action.type,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = action.targetEmployeeName ?: "Employee",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                val deskChangeText = when (action.type) {
                                    "RELOCATE" -> "From: ${action.fromDeskId ?: "Unassigned"} ➔ To: ${action.toDeskId}"
                                    "ASSIGN" -> "Assigned to Desk: ${action.toDeskId}"
                                    "SWAP" -> "Swapped with ${action.secondEmployeeName ?: "Other Employee"}"
                                    "UNASSIGN" -> "Unassigned from ${action.fromDeskId}"
                                    else -> "Desk: ${action.toDeskId}"
                                }

                                Text(
                                    text = deskChangeText,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue,
                                    fontSize = 12.sp
                                )

                                Text(
                                    text = action.reason,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!message.isApplied) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onApplyActions(message.proposedActions) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_ai_actions_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm & Apply Allocation")
                        }
                    }
                }
            }
        }
    }
}
