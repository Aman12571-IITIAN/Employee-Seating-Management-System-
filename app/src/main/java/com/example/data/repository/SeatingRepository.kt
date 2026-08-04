package com.example.data.repository

import com.example.data.dao.SeatingDao
import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.SeatingLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SeatingRepository(private val dao: SeatingDao) {

    val allDesks: Flow<List<DeskEntity>> = dao.getAllDesks()
    val allEmployees: Flow<List<EmployeeEntity>> = dao.getAllEmployees()
    val allLogs: Flow<List<SeatingLogEntity>> = dao.getAllLogs()

    suspend fun checkAndSeedDatabase() {
        val existingDesks = dao.getAllDesks().first()
        if (existingDesks.isEmpty()) {
            resetToInitialBlueprint()
        }
    }

    suspend fun resetToInitialBlueprint() {
        dao.clearDesks()
        dao.clearEmployees()
        dao.clearLogs()

        val seedEmployees = listOf(
            EmployeeEntity("EMP-101", "Sarah Jenkins", "sarah.j@company.com", "Engineering", "Lead Mobile Engineer", "#3B82F6", "A-101"),
            EmployeeEntity("EMP-102", "Alex Rivera", "alex.r@company.com", "Engineering", "Senior Backend Developer", "#10B981", "A-102"),
            EmployeeEntity("EMP-103", "David Chen", "david.c@company.com", "Engineering", "DevOps Engineer", "#8B5CF6", "A-103"),
            EmployeeEntity("EMP-104", "Priya Sharma", "priya.s@company.com", "Engineering", "Frontend Specialist", "#F59E0B", "A-104"),
            EmployeeEntity("EMP-105", "Marcus Vance", "marcus.v@company.com", "Product", "Principal Product Manager", "#EC4899", "B-101"),
            EmployeeEntity("EMP-106", "Elena Rostova", "elena.r@company.com", "Design", "Lead UX Researcher", "#06B6D4", "B-102"),
            EmployeeEntity("EMP-107", "Liam O'Connor", "liam.o@company.com", "Design", "UI & Brand Designer", "#8B5CF6", "B-103"),
            EmployeeEntity("EMP-108", "Jessica Alba", "jessica.a@company.com", "Product", "Technical PM", "#F43F5E", "B-104"),
            EmployeeEntity("EMP-109", "Michael Chang", "michael.c@company.com", "Sales", "Enterprise Sales Director", "#10B981", "C-101"),
            EmployeeEntity("EMP-110", "Chloe Bennett", "chloe.b@company.com", "Sales", "Account Executive", "#3B82F6", "C-102"),
            EmployeeEntity("EMP-111", "Daniel Kim", "daniel.k@company.com", "Marketing", "Growth Marketing Lead", "#F59E0B", "C-103"),
            EmployeeEntity("EMP-112", "Sophia Martinez", "sophia.m@company.com", "Marketing", "Content Strategist", "#EC4899", "C-104"),
            EmployeeEntity("EMP-113", "Rachel Adams", "rachel.a@company.com", "HR", "Head of People & Culture", "#6366F1", "D-101"),
            EmployeeEntity("EMP-114", "Brandon Scott", "brandon.s@company.com", "Finance", "Financial Analyst", "#14B8A6", "D-102"),
            // Unassigned new hires
            EmployeeEntity("EMP-115", "Jordan Taylor", "jordan.t@company.com", "Engineering", "Junior Fullstack Dev", "#3B82F6", null),
            EmployeeEntity("EMP-116", "Samantha Wright", "samantha.w@company.com", "Sales", "SDR Specialist", "#10B981", null),
            EmployeeEntity("EMP-117", "Lucas Silva", "lucas.s@company.com", "Product", "Associate Product Owner", "#F43F5E", null)
        )

        val seedDesks = mutableListOf<DeskEntity>()

        // Floor 1 (Zones A & B)
        // Zone A: Engineering (6 desks)
        seedDesks.add(DeskEntity("A-101", 1, "Zone A - Engineering", 1, 1, "OCCUPIED", "EMP-101", hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-102", 1, "Zone A - Engineering", 1, 2, "OCCUPIED", "EMP-102", hasStandingDesk = false, hasDualMonitors = true, isNearWindow = false, isErgonomic = true))
        seedDesks.add(DeskEntity("A-103", 1, "Zone A - Engineering", 2, 1, "OCCUPIED", "EMP-103", hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-104", 1, "Zone A - Engineering", 2, 2, "OCCUPIED", "EMP-104", hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = false))
        seedDesks.add(DeskEntity("A-105", 1, "Zone A - Engineering", 3, 1, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-106", 1, "Zone A - Engineering", 3, 2, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = true, isNearWindow = false, isErgonomic = true))

        // Zone B: Product & Design (6 desks)
        seedDesks.add(DeskEntity("B-101", 1, "Zone B - Product & Design", 1, 1, "OCCUPIED", "EMP-105", hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("B-102", 1, "Zone B - Product & Design", 1, 2, "OCCUPIED", "EMP-106", hasStandingDesk = false, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("B-103", 1, "Zone B - Product & Design", 2, 1, "OCCUPIED", "EMP-107", hasStandingDesk = true, hasDualMonitors = false, isNearWindow = false, isErgonomic = true))
        seedDesks.add(DeskEntity("B-104", 1, "Zone B - Product & Design", 2, 2, "OCCUPIED", "EMP-108", hasStandingDesk = false, hasDualMonitors = true, isNearWindow = false, isErgonomic = false))
        seedDesks.add(DeskEntity("B-105", 1, "Zone B - Product & Design", 3, 1, "RESERVED", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("B-106", 1, "Zone B - Product & Design", 3, 2, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = true))

        // Zone C: Sales & Marketing (6 desks)
        seedDesks.add(DeskEntity("C-101", 1, "Zone C - Sales & Marketing", 1, 1, "OCCUPIED", "EMP-109", hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("C-102", 1, "Zone C - Sales & Marketing", 1, 2, "OCCUPIED", "EMP-110", hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = true))
        seedDesks.add(DeskEntity("C-103", 1, "Zone C - Sales & Marketing", 2, 1, "OCCUPIED", "EMP-111", hasStandingDesk = false, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("C-104", 1, "Zone C - Sales & Marketing", 2, 2, "OCCUPIED", "EMP-112", hasStandingDesk = true, hasDualMonitors = false, isNearWindow = false, isErgonomic = true))
        seedDesks.add(DeskEntity("C-105", 1, "Zone C - Sales & Marketing", 3, 1, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("C-106", 1, "Zone C - Sales & Marketing", 3, 2, "MAINTENANCE", null, hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = false))

        // Zone D: Operations & HR (4 desks)
        seedDesks.add(DeskEntity("D-101", 1, "Zone D - Operations & HR", 1, 1, "OCCUPIED", "EMP-113", hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("D-102", 1, "Zone D - Operations & HR", 1, 2, "OCCUPIED", "EMP-114", hasStandingDesk = false, hasDualMonitors = true, isNearWindow = false, isErgonomic = true))
        seedDesks.add(DeskEntity("D-103", 1, "Zone D - Operations & HR", 2, 1, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = false, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("D-104", 1, "Zone D - Operations & HR", 2, 2, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = false))

        // Floor 2 (Exec & Innovation) - 8 desks
        seedDesks.add(DeskEntity("A-201", 2, "Zone A - Executive Suite", 1, 1, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-202", 2, "Zone A - Executive Suite", 1, 2, "RESERVED", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-203", 2, "Zone A - Executive Suite", 2, 1, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-204", 2, "Zone A - Executive Suite", 2, 2, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = false, isErgonomic = true))

        seedDesks.add(DeskEntity("B-201", 2, "Zone B - AI Lab & Innovation", 1, 1, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("B-202", 2, "Zone B - AI Lab & Innovation", 1, 2, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("B-203", 2, "Zone B - AI Lab & Innovation", 2, 1, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = true, isNearWindow = false, isErgonomic = true))
        seedDesks.add(DeskEntity("B-204", 2, "Zone B - AI Lab & Innovation", 2, 2, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = false))

        // Floor 3 (Expansion & Hotdesks) - 6 desks
        seedDesks.add(DeskEntity("A-301", 3, "Zone A - Hotdesks", 1, 1, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-302", 3, "Zone A - Hotdesks", 1, 2, "AVAILABLE", null, hasStandingDesk = true, hasDualMonitors = false, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-303", 3, "Zone A - Hotdesks", 2, 1, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = true, isNearWindow = false, isErgonomic = true))
        seedDesks.add(DeskEntity("A-304", 3, "Zone A - Hotdesks", 2, 2, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = false))
        seedDesks.add(DeskEntity("A-305", 3, "Zone A - Hotdesks", 3, 1, "RESERVED", null, hasStandingDesk = true, hasDualMonitors = true, isNearWindow = true, isErgonomic = true))
        seedDesks.add(DeskEntity("A-306", 3, "Zone A - Hotdesks", 3, 2, "AVAILABLE", null, hasStandingDesk = false, hasDualMonitors = false, isNearWindow = false, isErgonomic = false))

        val seedLogs = listOf(
            SeatingLogEntity(timestamp = System.currentTimeMillis() - 86400000 * 3, employeeName = "Sarah Jenkins", oldDeskId = null, newDeskId = "A-101", actionType = "ASSIGN", performedBy = "Admin Initial Setup", note = "Assigned to Lead Mobile Desk"),
            SeatingLogEntity(timestamp = System.currentTimeMillis() - 86400000 * 2, employeeName = "Alex Rivera", oldDeskId = null, newDeskId = "A-102", actionType = "ASSIGN", performedBy = "Admin Initial Setup", note = "Assigned to Engineering Pod"),
            SeatingLogEntity(timestamp = System.currentTimeMillis() - 86400000 * 1, employeeName = "Marcus Vance", oldDeskId = "A-105", newDeskId = "B-101", actionType = "RELOCATE", performedBy = "Admin via AI Assistant", note = "Relocated to Product Lead Desk near window")
        )

        dao.insertEmployees(seedEmployees)
        dao.insertDesks(seedDesks)
        seedLogs.forEach { dao.insertLog(it) }
    }

    suspend fun assignOrRelocateEmployee(employeeId: String, newDeskId: String, performedBy: String = "Admin Manual", note: String? = null) {
        val employees = dao.getAllEmployees().first()
        val desks = dao.getAllDesks().first()

        val emp = employees.find { it.id == employeeId } ?: return
        val targetDesk = desks.find { it.id == newDeskId } ?: return

        val oldDeskId = emp.assignedDeskId

        // 1. Unassign employee from old desk if any
        if (oldDeskId != null) {
            val oldDesk = desks.find { it.id == oldDeskId }
            if (oldDesk != null) {
                dao.updateDesk(oldDesk.copy(status = "AVAILABLE", assignedEmployeeId = null))
            }
        }

        // 2. If target desk already had an employee, unassign them
        if (targetDesk.assignedEmployeeId != null && targetDesk.assignedEmployeeId != employeeId) {
            val existingEmp = employees.find { it.id == targetDesk.assignedEmployeeId }
            if (existingEmp != null) {
                dao.updateEmployee(existingEmp.copy(assignedDeskId = null))
            }
        }

        // 3. Update employee with new desk
        dao.updateEmployee(emp.copy(assignedDeskId = newDeskId))

        // 4. Update target desk status
        dao.updateDesk(targetDesk.copy(status = "OCCUPIED", assignedEmployeeId = employeeId))

        // 5. Log change
        val actionType = if (oldDeskId == null) "ASSIGN" else "RELOCATE"
        dao.insertLog(
            SeatingLogEntity(
                employeeName = emp.name,
                oldDeskId = oldDeskId,
                newDeskId = newDeskId,
                actionType = actionType,
                performedBy = performedBy,
                note = note ?: "$actionType ${emp.name} to $newDeskId"
            )
        )
    }

    suspend fun unassignEmployee(employeeId: String, performedBy: String = "Admin Manual", note: String? = null) {
        val employees = dao.getAllEmployees().first()
        val desks = dao.getAllDesks().first()

        val emp = employees.find { it.id == employeeId } ?: return
        val oldDeskId = emp.assignedDeskId ?: return

        val oldDesk = desks.find { it.id == oldDeskId }
        if (oldDesk != null) {
            dao.updateDesk(oldDesk.copy(status = "AVAILABLE", assignedEmployeeId = null))
        }

        dao.updateEmployee(emp.copy(assignedDeskId = null))

        dao.insertLog(
            SeatingLogEntity(
                employeeName = emp.name,
                oldDeskId = oldDeskId,
                newDeskId = null,
                actionType = "UNASSIGN",
                performedBy = performedBy,
                note = note ?: "Unassigned ${emp.name} from $oldDeskId"
            )
        )
    }

    suspend fun swapSeats(empId1: String, empId2: String, performedBy: String = "Admin via AI Assistant", note: String? = null) {
        val employees = dao.getAllEmployees().first()
        val desks = dao.getAllDesks().first()

        val emp1 = employees.find { it.id == empId1 } ?: return
        val emp2 = employees.find { it.id == empId2 } ?: return

        val deskId1 = emp1.assignedDeskId
        val deskId2 = emp2.assignedDeskId

        // Swap desk assignments
        dao.updateEmployee(emp1.copy(assignedDeskId = deskId2))
        dao.updateEmployee(emp2.copy(assignedDeskId = deskId1))

        if (deskId1 != null) {
            val d1 = desks.find { it.id == deskId1 }
            if (d1 != null) {
                dao.updateDesk(d1.copy(assignedEmployeeId = emp2.id, status = "OCCUPIED"))
            }
        }

        if (deskId2 != null) {
            val d2 = desks.find { it.id == deskId2 }
            if (d2 != null) {
                dao.updateDesk(d2.copy(assignedEmployeeId = emp1.id, status = "OCCUPIED"))
            }
        }

        dao.insertLog(
            SeatingLogEntity(
                employeeName = "${emp1.name} & ${emp2.name}",
                oldDeskId = "$deskId1 <-> $deskId2",
                newDeskId = "$deskId2 <-> $deskId1",
                actionType = "SWAP",
                performedBy = performedBy,
                note = note ?: "Swapped seating between ${emp1.name} ($deskId1) and ${emp2.name} ($deskId2)"
            )
        )
    }

    suspend fun setDeskStatus(deskId: String, newStatus: String, performedBy: String = "Admin Manual", note: String? = null) {
        val desks = dao.getAllDesks().first()
        val desk = desks.find { it.id == deskId } ?: return

        // If setting to available/maintenance/reserved, unassign any assigned employee
        if (newStatus != "OCCUPIED" && desk.assignedEmployeeId != null) {
            val employees = dao.getAllEmployees().first()
            val emp = employees.find { it.id == desk.assignedEmployeeId }
            if (emp != null) {
                dao.updateEmployee(emp.copy(assignedDeskId = null))
            }
        }

        dao.updateDesk(desk.copy(status = newStatus, assignedEmployeeId = if (newStatus == "OCCUPIED") desk.assignedEmployeeId else null))

        dao.insertLog(
            SeatingLogEntity(
                employeeName = desk.assignedEmployeeId ?: "Desk $deskId",
                oldDeskId = deskId,
                newDeskId = deskId,
                actionType = if (newStatus == "RESERVED") "RESERVE" else "STATUS_CHANGE",
                performedBy = performedBy,
                note = note ?: "Changed status of $deskId to $newStatus"
            )
        )
    }
}
