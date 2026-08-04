package com.example.ai

import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ProposedAction(
    val type: String, // "ASSIGN", "RELOCATE", "UNASSIGN", "SWAP", "RESERVE"
    val targetEmployeeId: String? = null,
    val targetEmployeeName: String? = null,
    val secondEmployeeId: String? = null,
    val secondEmployeeName: String? = null,
    val fromDeskId: String? = null,
    val toDeskId: String? = null,
    val reason: String
)

data class AiResponse(
    val summaryText: String,
    val proposedActions: List<ProposedAction>,
    val isGeminiPowered: Boolean
)

object GeminiSeatingAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun processPrompt(
        userPrompt: String,
        allDesks: List<DeskEntity>,
        allEmployees: List<EmployeeEntity>
    ): AiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val isValidKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (isValidKey) {
            try {
                val geminiResult = callGeminiApi(userPrompt, apiKey, allDesks, allEmployees)
                if (geminiResult != null) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                // Fall back to heuristic engine on network or quota error
            }
        }

        // Fallback or offline smart NLP heuristic parser
        return@withContext parseHeuristically(userPrompt, allDesks, allEmployees)
    }

    private fun callGeminiApi(
        userPrompt: String,
        apiKey: String,
        desks: List<DeskEntity>,
        employees: List<EmployeeEntity>
    ): AiResponse? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val desksJson = JSONArray()
        desks.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("floor", d.floor)
            obj.put("zone", d.zone)
            obj.put("status", d.status)
            obj.put("assignedEmpId", d.assignedEmployeeId ?: "NONE")
            obj.put("standingDesk", d.hasStandingDesk)
            obj.put("dualMonitors", d.hasDualMonitors)
            obj.put("nearWindow", d.isNearWindow)
            desksJson.put(obj)
        }

        val empJson = JSONArray()
        employees.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("name", e.name)
            obj.put("dept", e.department)
            obj.put("role", e.role)
            obj.put("deskId", e.assignedDeskId ?: "UNASSIGNED")
            empJson.put(obj)
        }

        val systemInstruction = """
            You are an intelligent Office Seating Management AI Assistant for an enterprise facility manager.
            Your task is to understand user prompts requesting changes to employee seating assignments or desk statuses, and generate structured JSON allocation actions.
            
            Current Office Employees:
            $empJson
            
            Current Office Desks:
            $desksJson
            
            You MUST respond in strict JSON format with two keys:
            1. "summaryText": A polite, clear explanation of the proposed changes.
            2. "actions": An array of action objects. Each action object has:
               - "type": "ASSIGN" | "RELOCATE" | "UNASSIGN" | "SWAP" | "RESERVE"
               - "targetEmployeeId": string or null
               - "targetEmployeeName": string or null
               - "secondEmployeeId": string or null (only for SWAP)
               - "secondEmployeeName": string or null (only for SWAP)
               - "fromDeskId": string or null
               - "toDeskId": string or null
               - "reason": string explanation of why this desk was chosen
            
            If the user prompt asks a question or asks to find a suitable seat, pick the best matching AVAILABLE desk based on department or requested amenities (standing desk, dual monitors, window seat), and propose the assignment/relocation.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", "Admin Prompt: $userPrompt"))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemInstruction))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: return null

        if (!response.isSuccessful) return null

        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        val firstCandidate = candidates.optJSONObject(0) ?: return null
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        val text = parts.optJSONObject(0)?.optString("text") ?: return null

        val parsedObj = JSONObject(text)
        val summary = parsedObj.optString("summaryText", "Calculated seating optimization.")
        val actionsArray = parsedObj.optJSONArray("actions") ?: JSONArray()

        val actionsList = mutableListOf<ProposedAction>()
        for (i in 0 until actionsArray.length()) {
            val a = actionsArray.getJSONObject(i)
            actionsList.add(
                ProposedAction(
                    type = a.optString("type", "RELOCATE"),
                    targetEmployeeId = a.optString("targetEmployeeId", null),
                    targetEmployeeName = a.optString("targetEmployeeName", null),
                    secondEmployeeId = a.optString("secondEmployeeId", null),
                    secondEmployeeName = a.optString("secondEmployeeName", null),
                    fromDeskId = a.optString("fromDeskId", null),
                    toDeskId = a.optString("toDeskId", null),
                    reason = a.optString("reason", "Requested by AI prompt")
                )
            )
        }

        return AiResponse(
            summaryText = summary,
            proposedActions = actionsList,
            isGeminiPowered = true
        )
    }

    private fun parseHeuristically(
        prompt: String,
        desks: List<DeskEntity>,
        employees: List<EmployeeEntity>
    ): AiResponse {
        val lower = prompt.lowercase()
        val actions = mutableListOf<ProposedAction>()
        val explanation: String

        // Match employee in prompt
        val matchedEmp = employees.find { lower.contains(it.name.lowercase()) || lower.contains(it.name.split(" ").first().lowercase()) }
        val matchedDesk = desks.find { lower.contains(it.id.lowercase()) }

        when {
            // Swap action
            lower.contains("swap") -> {
                val foundEmps = employees.filter { lower.contains(it.name.lowercase()) || lower.contains(it.name.split(" ").first().lowercase()) }
                if (foundEmps.size >= 2) {
                    val e1 = foundEmps[0]
                    val e2 = foundEmps[1]
                    actions.add(
                        ProposedAction(
                            type = "SWAP",
                            targetEmployeeId = e1.id,
                            targetEmployeeName = e1.name,
                            secondEmployeeId = e2.id,
                            secondEmployeeName = e2.name,
                            fromDeskId = e1.assignedDeskId,
                            toDeskId = e2.assignedDeskId,
                            reason = "Swapping seat assignments between ${e1.name} (${e1.assignedDeskId ?: "Unassigned"}) and ${e2.name} (${e2.assignedDeskId ?: "Unassigned"})."
                        )
                    )
                    explanation = "I identified a swap command between ${e1.name} and ${e2.name}."
                } else {
                    explanation = "Please specify two employees to swap seating assignments."
                }
            }

            // Unassign action
            lower.contains("unassign") || lower.contains("remove") || lower.contains("clear seat") -> {
                if (matchedEmp != null) {
                    actions.add(
                        ProposedAction(
                            type = "UNASSIGN",
                            targetEmployeeId = matchedEmp.id,
                            targetEmployeeName = matchedEmp.name,
                            fromDeskId = matchedEmp.assignedDeskId,
                            reason = "Unassigning ${matchedEmp.name} from desk ${matchedEmp.assignedDeskId}."
                        )
                    )
                    explanation = "I found the request to unassign ${matchedEmp.name} from desk ${matchedEmp.assignedDeskId ?: "N/A"}."
                } else {
                    explanation = "Could not find the employee name to unassign."
                }
            }

            // Reserve desk
            lower.contains("reserve") -> {
                val targetDesk = matchedDesk ?: desks.find { it.status == "AVAILABLE" }
                if (targetDesk != null) {
                    actions.add(
                        ProposedAction(
                            type = "RESERVE",
                            toDeskId = targetDesk.id,
                            reason = "Reserving desk ${targetDesk.id} (${targetDesk.zone}, Floor ${targetDesk.floor}) for upcoming assignment."
                        )
                    )
                    explanation = "I will mark desk ${targetDesk.id} as RESERVED."
                } else {
                    explanation = "No available desk found to reserve."
                }
            }

            // Move / Assign / Find desk with criteria
            matchedEmp != null || matchedDesk != null || lower.contains("move") || lower.contains("assign") || lower.contains("find") -> {
                val emp = matchedEmp ?: employees.firstOrNull { it.assignedDeskId == null } ?: employees.first()
                val targetDesk = matchedDesk ?: findBestAvailableDesk(lower, emp.department, desks)

                if (targetDesk != null) {
                    val actionType = if (emp.assignedDeskId == null) "ASSIGN" else "RELOCATE"
                    var reasonDetail = "Selected available desk ${targetDesk.id} in ${targetDesk.zone} (Floor ${targetDesk.floor})"
                    if (targetDesk.hasStandingDesk) reasonDetail += " with Standing Desk"
                    if (targetDesk.hasDualMonitors) reasonDetail += " & Dual Monitors"
                    if (targetDesk.isNearWindow) reasonDetail += " near Window"

                    actions.add(
                        ProposedAction(
                            type = actionType,
                            targetEmployeeId = emp.id,
                            targetEmployeeName = emp.name,
                            fromDeskId = emp.assignedDeskId,
                            toDeskId = targetDesk.id,
                            reason = reasonDetail
                        )
                    )
                    explanation = "I analyzed your seating prompt for ${emp.name} and found a suitable desk at ${targetDesk.id} on Floor ${targetDesk.floor}."
                } else {
                    explanation = "No available desks matching your criteria were found."
                }
            }

            else -> {
                // Default helpful suggestion
                val availableCount = desks.count { it.status == "AVAILABLE" }
                explanation = "Currently there are $availableCount available desks in the office. Try prompts like: 'Move Sarah Jenkins to desk A-105', 'Assign Alex Rivera to a standing desk near window', or 'Swap seats between David Chen and Michael Chang'."
            }
        }

        return AiResponse(
            summaryText = explanation,
            proposedActions = actions,
            isGeminiPowered = false
        )
    }

    private fun findBestAvailableDesk(
        lowerPrompt: String,
        department: String,
        desks: List<DeskEntity>
    ): DeskEntity? {
        val available = desks.filter { it.status == "AVAILABLE" }
        if (available.isEmpty()) return null

        // Score available desks based on prompt preferences and department proximity
        val wantsStanding = lowerPrompt.contains("standing")
        val wantsMonitors = lowerPrompt.contains("monitor") || lowerPrompt.contains("dual")
        val wantsWindow = lowerPrompt.contains("window")

        return available.maxByOrNull { desk ->
            var score = 0
            if (wantsStanding && desk.hasStandingDesk) score += 10
            if (wantsMonitors && desk.hasDualMonitors) score += 10
            if (wantsWindow && desk.isNearWindow) score += 10
            if (desk.zone.contains(department, ignoreCase = true)) score += 15
            score
        } ?: available.firstOrNull()
    }
}
