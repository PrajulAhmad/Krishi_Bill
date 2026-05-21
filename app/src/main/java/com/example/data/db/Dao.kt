package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KrishiDao {
    // --- Farmers ---
    @Query("SELECT * FROM farmers ORDER BY name ASC")
    fun getAllFarmers(): Flow<List<Farmer>>

    @Query("SELECT * FROM farmers ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentFarmers(limit: Int): Flow<List<Farmer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFarmer(farmer: Farmer): Long

    @Update
    suspend fun updateFarmer(farmer: Farmer)

    @Query("DELETE FROM farmers WHERE id = :id")
    suspend fun deleteFarmer(id: Int)

    // --- Work Logs ---
    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC")
    fun getAllWorkLogs(): Flow<List<WorkLog>>

    @Query("SELECT * FROM work_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentWorkLogs(limit: Int): Flow<List<WorkLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkLog(workLog: WorkLog): Long

    @Update
    suspend fun updateWorkLog(workLog: WorkLog)

    @Query("DELETE FROM work_logs WHERE id = :id")
    suspend fun deleteWorkLog(id: Int)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Int)

    // --- Equipments ---
    @Query("SELECT * FROM equipments ORDER BY timestamp ASC")
    fun getAllEquipments(): Flow<List<Equipment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: Equipment): Long

    @Update
    suspend fun updateEquipment(equipment: Equipment)

    @Query("DELETE FROM equipments WHERE id = :id")
    suspend fun deleteEquipment(id: Int)
}
