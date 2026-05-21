package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Farmer::class, WorkLog::class, Expense::class, Equipment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun krishiDao(): KrishiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "krishi_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.krishiDao())
                }
            }
        }

        suspend fun populateDatabase(dao: KrishiDao) {
            // Pre-populate Equipments
            val tractorId = dao.insertEquipment(
                Equipment(name = "2BT Tractor", rate = 850.0, unitType = "Per Hour", iconName = "agriculture")
            )
            val reaperId = dao.insertEquipment(
                Equipment(name = "2BRU Reaper", rate = 1200.0, unitType = "Per Acre", iconName = "build")
            )
            dao.insertEquipment(
                Equipment(name = "Sprayer System", rate = 600.0, unitType = "Per Acre", iconName = "local_gas_station")
            )

            // Pre-populate Farmers
            val rajeshId = dao.insertFarmer(
                Farmer(
                    name = "Rajesh Kumar",
                    phone = "+91 98765 43210",
                    pendingDues = 4200.0,
                    status = "DUE",
                    role = "Lead Manager",
                    avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150"
                )
            )
            val amitId = dao.insertFarmer(
                Farmer(
                    name = "Amit Singh",
                    phone = "+91 92345 67890",
                    pendingDues = 0.0,
                    status = "SETTLED",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"
                )
            )
            val mPatelId = dao.insertFarmer(
                Farmer(
                    name = "M. Patel",
                    phone = "+91 88877 66655",
                    pendingDues = 8250.0,
                    status = "DUE",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150"
                )
            )
            val rameshId = dao.insertFarmer(
                Farmer(
                    name = "Ramesh Chaudhary",
                    phone = "+91 98765 43210",
                    pendingDues = 4200.0,
                    status = "DUE",
                    avatarUrl = ""
                )
            )
            val sunitaId = dao.insertFarmer(
                Farmer(
                    name = "Sunita Devi",
                    phone = "+91 91234 56789",
                    pendingDues = 0.0,
                    status = "SETTLED",
                    avatarUrl = ""
                )
            )
            val harpreetId = dao.insertFarmer(
                Farmer(
                    name = "Harpreet Singh",
                    phone = "+91 99887 76655",
                    pendingDues = 12850.0,
                    status = "DUE",
                    avatarUrl = ""
                )
            )
            val omId = dao.insertFarmer(
                Farmer(
                    name = "Om Prakash",
                    phone = "+91 88776 65544",
                    pendingDues = 1150.0,
                    status = "DUE",
                    avatarUrl = ""
                )
            )
            val laxmiId = dao.insertFarmer(
                Farmer(
                    name = "Laxmi Narayan",
                    phone = "+91 77665 54433",
                    pendingDues = 0.0,
                    status = "SETTLED",
                    avatarUrl = ""
                )
            )

            // Pre-populate Work Logs
            dao.insertWorkLog(
                WorkLog(
                    id = 0,
                    farmerId = rajeshId.toInt(),
                    farmerName = "Rajesh Kumar",
                    equipmentName = "Plowing - Farm Alpha",
                    areaAcres = 2.5,
                    ratePerUnit = 600.0,
                    amountPaidUpfront = 1500.0,
                    totalBill = 1500.0,
                    remainingAmount = 0.0,
                    date = "May 20, 2026",
                    time = "10:30 AM",
                    status = "PAID"
                )
            )
            dao.insertWorkLog(
                WorkLog(
                    id = 0,
                    farmerId = amitId.toInt(),
                    farmerName = "Amit Singh",
                    equipmentName = "Spraying - Farm Beta",
                    areaAcres = 5.0,
                    ratePerUnit = 600.0,
                    amountPaidUpfront = 0.0,
                    totalBill = 3000.0,
                    remainingAmount = 3000.0,
                    date = "Yesterday",
                    time = "4:15 PM",
                    status = "PENDING"
                )
            )
            dao.insertWorkLog(
                WorkLog(
                    id = 0,
                    farmerId = mPatelId.toInt(),
                    farmerName = "M. Patel",
                    equipmentName = "Harvester Repair",
                    areaAcres = 0.0,
                    ratePerUnit = 0.0,
                    amountPaidUpfront = 0.0,
                    totalBill = 4500.0,
                    remainingAmount = 4500.0,
                    date = "Equipment Maintenance",
                    time = "2 days ago",
                    status = "SERVICE"
                )
            )

            // Pre-populate Expenses
            dao.insertExpense(
                Expense(
                    title = "Tractor Engine Repair",
                    amount = 12400.0,
                    category = "Repair",
                    date = "24 Oct",
                    timestamp = System.currentTimeMillis() - 86400000L * 2
                )
            )
            dao.insertExpense(
                Expense(
                    title = "Diesel - 150 Liters",
                    amount = 14250.0,
                    category = "Fuel",
                    date = "22 Oct",
                    timestamp = System.currentTimeMillis() - 86400000L * 4
                )
            )
            dao.insertExpense(
                Expense(
                    title = "Cultivator Blades",
                    amount = 4500.0,
                    category = "Parts",
                    date = "20 Oct",
                    timestamp = System.currentTimeMillis() - 86400000L * 6
                )
            )
            dao.insertExpense(
                Expense(
                    title = "Hydraulic Oil Change",
                    amount = 2200.0,
                    category = "Repair",
                    date = "18 Oct",
                    timestamp = System.currentTimeMillis() - 86400000L * 8
                )
            )
        }
    }
}
