package com.example.data

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow

class KrishiRepository(private val dao: KrishiDao) {
    // Farmers
    val allFarmers: Flow<List<Farmer>> = dao.getAllFarmers()
    fun getRecentFarmers(limit: Int): Flow<List<Farmer>> = dao.getRecentFarmers(limit)
    suspend fun insertFarmer(farmer: Farmer): Long = dao.insertFarmer(farmer)
    suspend fun updateFarmer(farmer: Farmer) = dao.updateFarmer(farmer)
    suspend fun deleteFarmer(id: Int) = dao.deleteFarmer(id)

    // Work Logs
    val allWorkLogs: Flow<List<WorkLog>> = dao.getAllWorkLogs()
    fun getRecentWorkLogs(limit: Int): Flow<List<WorkLog>> = dao.getRecentWorkLogs(limit)
    suspend fun insertWorkLog(workLog: WorkLog): Long = dao.insertWorkLog(workLog)
    suspend fun updateWorkLog(workLog: WorkLog) = dao.updateWorkLog(workLog)
    suspend fun deleteWorkLog(id: Int) = dao.deleteWorkLog(id)

    // Expenses
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    suspend fun insertExpense(expense: Expense): Long = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(id: Int) = dao.deleteExpense(id)

    // Equipments
    val allEquipments: Flow<List<Equipment>> = dao.getAllEquipments()
    suspend fun insertEquipment(equipment: Equipment): Long = dao.insertEquipment(equipment)
    suspend fun updateEquipment(equipment: Equipment) = dao.updateEquipment(equipment)
    suspend fun deleteEquipment(id: Int) = dao.deleteEquipment(id)
}
