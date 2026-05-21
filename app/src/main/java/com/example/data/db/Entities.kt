package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmers")
data class Farmer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val pendingDues: Double,
    val status: String, // "DUE" or "SETTLED"
    val role: String = "Farmer", // e.g. "Lead Manager"
    val avatarUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "work_logs")
data class WorkLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val farmerId: Int,
    val farmerName: String,
    val equipmentName: String,
    val areaAcres: Double,
    val ratePerUnit: Double,
    val amountPaidUpfront: Double,
    val totalBill: Double,
    val remainingAmount: Double,
    val date: String,
    val time: String,
    val status: String, // "PAID", "PENDING", "SERVICE"
    val timestamp: Long = System.currentTimeMillis(),
    val saveAsDefaultRate: Boolean = false,
    val synced: Boolean = false
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String, // "Repair", "Fuel", "Parts", "Other"
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "equipments")
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rate: Double,
    val unitType: String, // "Per Hour", "Per Acre"
    val iconName: String = "build", // e.g. "agriculture", "build", "local_gas_station"
    val timestamp: Long = System.currentTimeMillis()
)
