package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.KrishiRepository
import com.example.data.db.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class KrishiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KrishiRepository

    init {
        // Scope for the DB creation/callback
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = KrishiRepository(db.krishiDao())
    }

    // --- State Streams ---
    val farmers: StateFlow<List<Farmer>> = repository.allFarmers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentFarmers: StateFlow<List<Farmer>> = repository.getRecentFarmers(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workLogs: StateFlow<List<WorkLog>> = repository.allWorkLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentWorkLogs: StateFlow<List<WorkLog>> = repository.getRecentWorkLogs(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equipments: StateFlow<List<Equipment>> = repository.allEquipments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive State ---
    private val _isOfflineMode = MutableStateFlow(true)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _syncQueueCount = MutableStateFlow(3)
    val syncQueueCount: StateFlow<Int> = _syncQueueCount.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow("Last synced: 2 mins ago")
    val lastSyncedTime: StateFlow<String> = _lastSyncedTime.asStateFlow()

    // Keypad Active Field: "Acres", "Rate", "Advance"
    private val _activeKeypadField = MutableStateFlow<String?>(null)
    val activeKeypadField: StateFlow<String?> = _activeKeypadField.asStateFlow()

    // Keypad typed value (raw string, e.g. "2.5")
    private val _keypadBuffer = MutableStateFlow("")
    val keypadBuffer: StateFlow<String> = _keypadBuffer.asStateFlow()

    // Work-Log Form entry state
    var selectedFarmerForNewLog = MutableStateFlow<Farmer?>(null)
    var selectedEquipmentForNewLog = MutableStateFlow<Equipment?>(null)
    var typedAcres = MutableStateFlow("0.00")
    var typedRate = MutableStateFlow("1200.00")
    var typedAdvance = MutableStateFlow("0.00")
    var saveAsDefaultRate = MutableStateFlow(false)

    // --- Actions ---
    fun toggleOfflineMode() {
        _isOfflineMode.value = !_isOfflineMode.value
        if (!_isOfflineMode.value) {
            // Synced up!
            syncData()
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _syncQueueCount.value = 0
            _lastSyncedTime.value = "Synced Now"
            // Mark all items as synced in db (for demo visual indicators)
            // Just local update
        }
    }

    fun addFarmer(name: String, phone: String, initialDues: Double, role: String = "Farmer") {
        viewModelScope.launch {
            val status = if (initialDues > 0) "DUE" else "SETTLED"
            val newFarmer = Farmer(
                name = name,
                phone = phone,
                pendingDues = initialDues,
                status = status,
                role = role
            )
            repository.insertFarmer(newFarmer)
            incrementSyncQueueIfOffline()
        }
    }

    fun addWorkLog() {
        val farmer = selectedFarmerForNewLog.value ?: return
        val equip = selectedEquipmentForNewLog.value ?: return
        val acres = typedAcres.value.toDoubleOrNull() ?: 0.0
        val rate = typedRate.value.toDoubleOrNull() ?: 0.0
        val advance = typedAdvance.value.toDoubleOrNull() ?: 0.0
        val totalBill = if (equip.unitType == "Per Hour") acres * rate else acres * rate
        val remaining = totalBill - advance

        viewModelScope.launch {
            val sdfDate = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.US)
            val currentDateStr = sdfDate.format(Date())
            val currentTimeStr = sdfTime.format(Date())

            val status = when {
                remaining <= 0.0 -> "PAID"
                advance > 0.0 -> "PENDING"
                else -> "PENDING"
            }

            val newLog = WorkLog(
                farmerId = farmer.id,
                farmerName = farmer.name,
                equipmentName = "${equip.name} - ${farmer.name}",
                areaAcres = acres,
                ratePerUnit = rate,
                amountPaidUpfront = advance,
                totalBill = totalBill,
                remainingAmount = remaining,
                date = "May 20, 2026", // Anchor date from UI screenshot
                time = currentTimeStr,
                status = status,
                saveAsDefaultRate = saveAsDefaultRate.value
            )
            repository.insertWorkLog(newLog)

            // Update farmer dues too
            if (remaining > 0.0) {
                val updatedFarmer = farmer.copy(
                    pendingDues = farmer.pendingDues + remaining,
                    status = "DUE"
                )
                repository.insertFarmer(updatedFarmer)
            }

            // Update default equipment rate if checked
            if (saveAsDefaultRate.value) {
                val updatedEquip = equip.copy(rate = rate)
                repository.updateEquipment(updatedEquip)
            }

            // Reset form
            selectedFarmerForNewLog.value = null
            typedAcres.value = "0.00"
            typedAdvance.value = "0.00"
            incrementSyncQueueIfOffline()
        }
    }

    fun addExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            val newExpense = Expense(
                title = title,
                amount = amount,
                category = category,
                date = "20 May"
            )
            repository.insertExpense(newExpense)
            incrementSyncQueueIfOffline()
        }
    }

    fun addEquipment(name: String, rate: Double, unitType: String) {
        viewModelScope.launch {
            val newReq = Equipment(
                name = name,
                rate = rate,
                unitType = unitType,
                iconName = if (categoryFromUnit(unitType) == "Hour") "agriculture" else "build"
            )
            repository.insertEquipment(newReq)
            incrementSyncQueueIfOffline()
        }
    }

    private fun categoryFromUnit(unit: String): String {
        return if (unit.contains("Hour")) "Hour" else "Acre"
    }

    private fun incrementSyncQueueIfOffline() {
        if (_isOfflineMode.value) {
            _syncQueueCount.value += 1
        }
    }

    // --- Keypad Input Handling ---
    fun openKeypad(fieldName: String) {
        _activeKeypadField.value = fieldName
        // Initialize keypad buffer with the current valuation of the field without trailing zeroes/dots
        val currentStr = when (fieldName) {
            "Acres" -> typedAcres.value
            "Rate" -> typedRate.value
            "Advance" -> typedAdvance.value
            else -> ""
        }
        _keypadBuffer.value = if (currentStr == "0.00" || currentStr == "0") "" else currentStr
    }

    fun closeKeypad(confirm: Boolean) {
        val field = _activeKeypadField.value
        val typed = _keypadBuffer.value
        if (confirm && field != null) {
            val doubleVal = typed.toDoubleOrNull() ?: 0.0
            val formatted = String.format(Locale.US, "%.2f", doubleVal)
            when (field) {
                "Acres" -> typedAcres.value = formatted
                "Rate" -> typedRate.value = formatted
                "Advance" -> typedAdvance.value = formatted
            }
        }
        _activeKeypadField.value = null
        _keypadBuffer.value = ""
    }

    fun onKeypadPress(char: String) {
        val current = _keypadBuffer.value
        if (char == ".") {
            if (!current.contains(".")) {
                _keypadBuffer.value = if (current.isEmpty()) "0." else current + "."
            }
        } else {
            // Cap digits for sanity
            if (current.replace(".", "").length < 8) {
                _keypadBuffer.value = current + char
            }
        }
    }

    fun onKeypadBackspace() {
        val current = _keypadBuffer.value
        if (current.isNotEmpty()) {
            _keypadBuffer.value = current.substring(0, current.length - 1)
        }
    }
}
