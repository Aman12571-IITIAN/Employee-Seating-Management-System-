package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DeskEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.SeatingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeatingDao {
    @Query("SELECT * FROM desks ORDER BY floor ASC, id ASC")
    fun getAllDesks(): Flow<List<DeskEntity>>

    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM seating_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllLogs(): Flow<List<SeatingLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesks(desks: List<DeskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Update
    suspend fun updateDesk(desk: DeskEntity)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SeatingLogEntity)

    @Query("DELETE FROM desks")
    suspend fun clearDesks()

    @Query("DELETE FROM employees")
    suspend fun clearEmployees()

    @Query("DELETE FROM seating_logs")
    suspend fun clearLogs()
}
