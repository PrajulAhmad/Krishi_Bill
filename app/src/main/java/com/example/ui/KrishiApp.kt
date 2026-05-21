package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.db.*
import com.example.ui.theme.*
import java.util.*

val tinyRounded = 2.dp
val standardRounded = 4.dp
val lgRounded = 8.dp
val xlRounded = 12.dp
val fullRounded = 999.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KrishiApp(viewModel: KrishiViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("Dashboard") }

    // Collect DB states
    val allFarmers by viewModel.farmers.collectAsStateWithLifecycle()
    val recentFarmersList by viewModel.recentFarmers.collectAsStateWithLifecycle()
    val allWorkLogs by viewModel.workLogs.collectAsStateWithLifecycle()
    val recentWorkLogsList by viewModel.recentWorkLogs.collectAsStateWithLifecycle()
    val allExpensesList by viewModel.expenses.collectAsStateWithLifecycle()
    val allEquipmentsList by viewModel.equipments.collectAsStateWithLifecycle()

    // Interactive states
    val isOffline by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val syncQueueCount by viewModel.syncQueueCount.collectAsStateWithLifecycle()
    val activeKeypadField by viewModel.activeKeypadField.collectAsStateWithLifecycle()

    // Dialog trigger states
    var showAddFarmerDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var showEditRatesDialog by remember { mutableStateOf(false) }

    // Dynamic computations
    val totalUnpaidDues = allFarmers.sumOf { it.pendingDues }
    val totalExpensesSum = allExpensesList.sumOf { it.amount }
    val todayRevenue = allWorkLogs.filter { it.status == "PAID" }.sumOf { it.totalBill }
    val todayWorkAcres = allWorkLogs.sumOf { it.areaAcres }

    Scaffold(
        topBar = {
            Column {
                // Main Top Screen Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = "Farm Icon",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Krishi-Bill",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PrimaryGreen
                        )
                    }

                    // Sync Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(fullRounded))
                            .background(BorderMuted.copy(alpha = 0.5f))
                            .clickable {
                                Toast
                                    .makeText(context, "Cloud Sync Completed Successfully!", Toast.LENGTH_SHORT)
                                    .show()
                                viewModel.syncData()
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (syncQueueCount == 0) Icons.Default.CloudDone else Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = if (syncQueueCount == 0) SyncSuccess else SyncPending,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (syncQueueCount == 0) "Synced" else "$syncQueueCount Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
                Divider(color = BorderMuted)
            }
        },
        bottomBar = {
            Column {
                Divider(color = BorderMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavI(
                        icon = Icons.Default.Dashboard,
                        label = "Dashboard",
                        selected = currentTab == "Dashboard",
                        onClick = { currentTab = "Dashboard" },
                        testTag = "nav_dashboard"
                    )
                    BottomNavI(
                        icon = Icons.Default.Groups,
                        label = "Farmers",
                        selected = currentTab == "Farmers",
                        onClick = { currentTab = "Farmers" },
                        testTag = "nav_farmers"
                    )
                    BottomNavI(
                        icon = Icons.Default.FormatListBulleted,
                        label = "Work Logs",
                        selected = currentTab == "WorkLogs",
                        onClick = { currentTab = "WorkLogs" },
                        testTag = "nav_worklogs"
                    )
                    BottomNavI(
                        icon = Icons.Default.ReceiptLong,
                        label = "Expenses",
                        selected = currentTab == "Expenses",
                        onClick = { currentTab = "Expenses" },
                        testTag = "nav_expenses"
                    )
                    BottomNavI(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        selected = currentTab == "Profile",
                        onClick = { currentTab = "Profile" },
                        testTag = "nav_profile"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Switch tabs with crossfade
            Crossfade(targetState = currentTab, label = "tab") { tab ->
                when (tab) {
                    "Dashboard" -> DashboardScreen(
                        viewModel = viewModel,
                        recentFarmers = recentFarmersList,
                        recentWorkLogs = recentWorkLogsList,
                        isOffline = isOffline,
                        syncQueue = syncQueueCount,
                        todayRevenue = todayRevenue,
                        unpaidTotals = totalUnpaidDues,
                        todayWorkAcres = todayWorkAcres,
                        totalExpensesSum = totalExpensesSum,
                        onNavigateToFarmers = { currentTab = "Farmers" },
                        onNavigateToWorkLogs = { currentTab = "WorkLogs" }
                    )
                    "Farmers" -> FarmersScreen(
                        viewModel = viewModel,
                        allFarmers = allFarmers,
                        totalUnpaidDues = totalUnpaidDues,
                        onAddFarmerClick = { showAddFarmerDialog = true },
                        onSelectFarmerToLog = { farmer ->
                            viewModel.selectedFarmerForNewLog.value = farmer
                            currentTab = "WorkLogs"
                        }
                    )
                    "WorkLogs" -> WorkLogsScreen(
                        viewModel = viewModel,
                        allWorkLogs = allWorkLogs,
                        allFarmers = allFarmers,
                        allEquipments = allEquipmentsList
                    )
                    "Expenses" -> ExpensesScreen(
                        viewModel = viewModel,
                        allExpenses = allExpensesList,
                        totalSum = totalExpensesSum,
                        onAddExpenseClick = { showAddExpenseDialog = true }
                    )
                    "Profile" -> ProfileScreen(
                        viewModel = viewModel,
                        allEquipments = allEquipmentsList,
                        onAddEquipmentClick = { showAddEquipmentDialog = true },
                        onEditRatesClick = { showEditRatesDialog = true }
                    )
                }
            }

            // Keypad overlay pop-up
            AnimatedVisibility(
                visible = activeKeypadField != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                activeKeypadField?.let { fieldTitle ->
                    val typedValue by viewModel.keypadBuffer.collectAsStateWithLifecycle()
                    NumericKeypadOverlay(
                        title = "Enter $fieldTitle",
                        bufferText = typedValue,
                        onKeyPress = { viewModel.onKeypadPress(it) },
                        onBackspace = { viewModel.onKeypadBackspace() },
                        onCancel = { viewModel.closeKeypad(confirm = false) },
                        onConfirm = { viewModel.closeKeypad(confirm = true) }
                    )
                }
            }
        }
    }

    // dialog boxes

    // Add Farmer Dialog Box
    if (showAddFarmerDialog) {
        AddFarmerDialog(
            onDismiss = { showAddFarmerDialog = false },
            onSave = { name, phone, dues ->
                viewModel.addFarmer(name, phone, dues)
                showAddFarmerDialog = false
                Toast.makeText(context, "Farmer added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add Expense Dialog Box
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, amount, category ->
                viewModel.addExpense(title, amount, category)
                showAddExpenseDialog = false
                Toast.makeText(context, "Expense logged!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Add Equipment Dialog Box
    if (showAddEquipmentDialog) {
        AddEquipmentDialog(
            onDismiss = { showAddEquipmentDialog = false },
            onSave = { name, rate, unit ->
                viewModel.addEquipment(name, rate, unit)
                showAddEquipmentDialog = false
                Toast.makeText(context, "Equipment Added!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Rates Dialog Box
    if (showEditRatesDialog) {
        EditRatesHelpDialog(onDismiss = { showEditRatesDialog = false })
    }
}

@Composable
fun BottomNavI(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(xlRounded))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.width(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(xlRounded))
                    .background(if (selected) HarvestYellow else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) OnSecondaryContainer else OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) PrimaryGreen else OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: KrishiViewModel,
    recentFarmers: List<Farmer>,
    recentWorkLogs: List<WorkLog>,
    isOffline: Boolean,
    syncQueue: Int,
    todayRevenue: Double,
    unpaidTotals: Double,
    todayWorkAcres: Double,
    totalExpensesSum: Double,
    onNavigateToFarmers: () -> Unit,
    onNavigateToWorkLogs: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Safe Offline Mode Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderMuted, RoundedCornerShape(lgRounded))
                    .clip(RoundedCornerShape(lgRounded))
                    .background(if (isOffline) Color(0xFFE8F5E9) else Color(0xFFF3E5F5))
                    .clickable { viewModel.toggleOfflineMode() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isOffline) Icons.Default.CloudSync else Icons.Default.CloudDone,
                            contentDescription = "Sync Queue",
                            tint = if (isOffline) SyncPending else SyncSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isOffline) "Sync Queue: $syncQueue items" else "All Records Synced",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tinyRounded))
                            .background(if (isOffline) ErrorRed else SyncSuccess)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Text(
                                text = if (isOffline) "OFFLINE MODE" else "CONNECTED",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "OPERATIONS SUMMARY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = OnSurfaceVariant
            )
        }

        // Summary Stats Grid 2x2
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        title = "TODAY'S REVENUE",
                        value = "₹${String.format(Locale.US, "%,.0f", todayRevenue)}",
                        valueColor = PrimaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "UNPAID TOTALS",
                        value = "₹${String.format(Locale.US, "%,.0f", unpaidTotals)}",
                        valueColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        title = "TODAY'S WORK",
                        value = "${String.format(Locale.US, "%.1f", todayWorkAcres)} Acres",
                        valueColor = OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "EXPENSES",
                        value = "₹${String.format(Locale.US, "%,.0f", totalExpensesSum)}",
                        valueColor = OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // RECENT FARMERS Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT FARMERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "ALL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clickable { onNavigateToFarmers() }
                        .padding(4.dp)
                )
            }
        }

        // Horizontally Scrolling Recent Farmers List
        item {
            if (recentFarmers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(1.dp, BorderMuted, RoundedCornerShape(lgRounded))
                        .background(AppSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No farmers found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recentFarmers) { farmer ->
                        RecentFarmerCard(
                            farmer = farmer,
                            onClick = {
                                viewModel.selectedFarmerForNewLog.value = farmer
                                onNavigateToWorkLogs()
                            }
                        )
                    }
                }
            }
        }

        // BOTTOM RECENT ACTIVITY list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clickable { onNavigateToWorkLogs() }
                        .padding(4.dp)
                )
            }
        }

        items(recentWorkLogs) { log ->
            ActivityLogCard(log)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Floating Button for adding new entries instantly
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { onNavigateToWorkLogs() },
            containerColor = HarvestYellow,
            elevation = FloatingActionButtonDefaults.elevation(0.dp),
            modifier = Modifier
                .testTag("add_work_entry_fab")
                .size(56.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Work Entry",
                tint = OnSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, BorderMuted, RoundedCornerShape(standardRounded))
            .background(AppSurface)
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                ),
                color = valueColor
            )
        }
    }
}

@Composable
fun RecentFarmerCard(
    farmer: Farmer,
    onClick: () -> Unit
) {
    val initials = if (farmer.name.contains(" ")) {
        "${farmer.name[0]}${farmer.name.substringAfter(" ")[0]}"
    } else {
        farmer.name.take(2).uppercase()
    }

    val colorsList = listOf(Color(0xFF81C784), Color(0xFFFFD54F), Color(0xFFE57373), Color(0xFF64B5F6))
    val avatarBg = colorsList[farmer.name.length % colorsList.size]

    Box(
        modifier = Modifier
            .width(110.dp)
            .border(1.dp, BorderMuted, RoundedCornerShape(standardRounded))
            .clip(RoundedCornerShape(standardRounded))
            .background(AppSurface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(xlRounded))
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = farmer.name,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (farmer.pendingDues > 0) "DUE" else "SETTLED",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun ActivityLogCard(log: WorkLog) {
    val categoryIcon = when {
        log.equipmentName.contains("Plowing") -> Icons.Default.Agriculture
        log.equipmentName.contains("Spraying") -> Icons.Default.Opacity
        log.equipmentName.contains("Harvester") -> Icons.Default.Build
        else -> Icons.Default.Build
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(standardRounded),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, BorderMuted)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(standardRounded))
                        .background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = "Activity type",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = log.equipmentName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (log.areaAcres > 0) "${log.areaAcres} Acres • ${log.date}" else "Repair • ${log.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tinyRounded))
                        .background(
                            when (log.status) {
                                "PAID" -> Color(0xFFE8F5E9)
                                "PENDING" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE3F2FD)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.status,
                        color = when (log.status) {
                            "PAID" -> SyncSuccess
                            "PENDING" -> SyncPending
                            else -> Color(0xFF1565C0)
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${String.format(Locale.US, "%,.0f", log.totalBill)}",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    color = PrimaryGreen
                )
            }
        }
    }
}

// ==========================================
// 2. FARMERS SCREEN
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FarmersScreen(
    viewModel: KrishiViewModel,
    allFarmers: List<Farmer>,
    totalUnpaidDues: Double,
    onAddFarmerClick: () -> Unit,
    onSelectFarmerToLog: (Farmer) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedFarmerId by remember { mutableStateOf<Int?>(null) }
    val filteredFarmers = allFarmers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("farmer_search_input"),
                        placeholder = { Text("Search farmers...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderMuted
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(lgRounded)
                    )

                    Button(
                        onClick = { /* Toggle search filters */ },
                        shape = RoundedCornerShape(lgRounded),
                        border = BorderStroke(1.dp, BorderMuted),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppSurface,
                            contentColor = OnSurfaceVariant
                        ),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.FilterList, "Filter")
                            Text("Filters", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Summary Info Display Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatCard(
                        title = "TOTAL FARMERS",
                        value = "${allFarmers.size}",
                        valueColor = PrimaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "PENDING DUES",
                        value = "₹${String.format(Locale.US, "%,.0f", totalUnpaidDues)}",
                        valueColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "RECENT FARMERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            // Quick Avatars Scroll Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val colorsList = listOf(Color(0xFFE3F2FD), Color(0xFFFFEBEE), Color(0xFFE8F5E9), Color(0xFFFFF3E0))
                    items(allFarmers.take(6)) { f ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.width(80.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colorsList[f.name.length % colorsList.size])
                                    .clickable { searchQuery = f.name },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Farmer Avatar",
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = f.name.substringBefore(" "),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ALL FARMERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            // List of all farmers
            items(filteredFarmers) { farmer ->
                FarmerListRowCard(
                    farmer = farmer,
                    isExpanded = expandedFarmerId == farmer.id,
                    onClick = {
                        expandedFarmerId = if (expandedFarmerId == farmer.id) null else farmer.id
                    },
                    onAddWorkEntry = {
                        onSelectFarmerToLog(farmer)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Farmer button at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onAddFarmerClick,
                colors = ButtonDefaults.buttonColors(containerColor = HarvestYellow),
                shape = RoundedCornerShape(xlRounded),
                modifier = Modifier
                    .testTag("add_farmer_bottom_button")
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, "Add Farmer", tint = OnSecondaryContainer)
                    Text(
                        text = "ADD FARMER",
                        color = OnSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FarmerListRowCard(
    farmer: Farmer,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onAddWorkEntry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(standardRounded),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, BorderMuted)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(standardRounded))
                            .background(SurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = farmer.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = farmer.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format(Locale.US, "%,.0f", farmer.pendingDues)}",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = if (farmer.pendingDues > 0) ErrorRed else PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tinyRounded))
                            .background(if (farmer.pendingDues > 0) Color(0xFFFFDAD6) else Color(0xFFE8F5E9))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = farmer.status,
                            color = if (farmer.pendingDues > 0) ErrorRed else SyncSuccess,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = BorderMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAddWorkEntry,
                            shape = RoundedCornerShape(standardRounded),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, "Work Entry", tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Work Entry", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        OutlinedButton(
                            onClick = { /* add expense */ },
                            shape = RoundedCornerShape(standardRounded),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, PrimaryGreen)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TrendingDown, "Add Expense", tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                                Text("Add Expense", color = PrimaryGreen, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. WORK LOGS SCREEN & FORM LOGGERS
// ==========================================
@Composable
fun WorkLogsScreen(
    viewModel: KrishiViewModel,
    allWorkLogs: List<WorkLog>,
    allFarmers: List<Farmer>,
    allEquipments: List<Equipment>
) {
    var viewStateMode by remember { mutableStateOf("Form") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Tab Headers to switch view modes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppSurface),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "New Entry Form",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (viewStateMode == "Form") PrimaryGreen else OnSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewStateMode = "Form" }
                    .padding(vertical = 14.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Past Logs (${allWorkLogs.size})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (viewStateMode == "History") PrimaryGreen else OnSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewStateMode = "History" }
                    .padding(vertical = 14.dp),
                textAlign = TextAlign.Center
            )
        }
        Divider(color = BorderMuted)

        if (viewStateMode == "Form") {
            WorkLogEntryFormView(
                viewModel = viewModel,
                allFarmers = allFarmers,
                allEquipments = allEquipments,
                onSaveSuccess = { viewStateMode = "History" }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (allWorkLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No work logs recorded yet.", color = OnSurfaceVariant)
                        }
                    }
                } else {
                    items(allWorkLogs) { log ->
                        ActivityLogCard(log = log)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkLogEntryFormView(
    viewModel: KrishiViewModel,
    allFarmers: List<Farmer>,
    allEquipments: List<Equipment>,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current

    // Observe live form state parameters
    val activeFarmer by viewModel.selectedFarmerForNewLog.collectAsStateWithLifecycle()
    val activeEquip by viewModel.selectedEquipmentForNewLog.collectAsStateWithLifecycle()
    val acres by viewModel.typedAcres.collectAsStateWithLifecycle()
    val rate by viewModel.typedRate.collectAsStateWithLifecycle()
    val advancePaid by viewModel.typedAdvance.collectAsStateWithLifecycle()
    val saveAsDefault by viewModel.saveAsDefaultRate.collectAsStateWithLifecycle()

    val doubleAcres = acres.toDoubleOrNull() ?: 0.0
    val doubleRate = rate.toDoubleOrNull() ?: 0.0
    val doubleAdvance = advancePaid.toDoubleOrNull() ?: 0.0
    val calculatedTotal = doubleAcres * doubleRate
    val calculatedRemaining = calculatedTotal - doubleAdvance

    var farmerSearchInput by remember { mutableStateOf("") }
    var dropdownEquipmentsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(allEquipments) {
        if (activeEquip == null && allEquipments.isNotEmpty()) {
            viewModel.selectedEquipmentForNewLog.value = allEquipments.first()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "New Work Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = "May 20, 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tinyRounded))
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("LOGGING", color = SyncPending, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "FARMER NAME",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            OutlinedTextField(
                value = farmerSearchInput,
                onValueChange = { farmerSearchInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_farmer_search"),
                leadingIcon = { Icon(Icons.Default.Person, "Farmer") },
                placeholder = { Text("Search farmer or tap badges below...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = BorderMuted
                ),
                shape = RoundedCornerShape(standardRounded),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    allFarmers.take(3).forEach { farmer ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(fullRounded))
                                .background(BorderMuted.copy(alpha = 0.5f))
                                .clickable {
                                    viewModel.selectedFarmerForNewLog.value = farmer
                                    farmerSearchInput = farmer.name
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = farmer.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurface
                            )
                        }
                    }
                }
            }

            activeFarmer?.let { selected ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(standardRounded))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, SyncSuccess, RoundedCornerShape(standardRounded))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Selected: ${selected.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = SyncSuccess
                            )
                            Text(
                                text = "Outstanding: ₹${String.format(Locale.US, "%,.0f", selected.pendingDues)} | ${selected.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = SyncSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = "EQUIPMENT USED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = activeEquip?.let { "${it.name} (${it.unitType})" } ?: "Select equipment...",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dropdownEquipmentsExpanded = true },
                    trailingIcon = {
                        IconButton(onClick = { dropdownEquipmentsExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, "Dropdown")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderMuted
                    ),
                    shape = RoundedCornerShape(standardRounded)
                )

                DropdownMenu(
                    expanded = dropdownEquipmentsExpanded,
                    onDismissRequest = { dropdownEquipmentsExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    allEquipments.forEach { eq ->
                        DropdownMenuItem(
                            text = { Text("${eq.name} - ₹${eq.rate} / ${eq.unitType}") },
                            onClick = {
                                viewModel.selectedEquipmentForNewLog.value = eq
                                viewModel.typedRate.value = String.format(Locale.US, "%.2f", eq.rate)
                                dropdownEquipmentsExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AREA (ACRES)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, BorderMuted, RoundedCornerShape(standardRounded))
                            .clip(RoundedCornerShape(standardRounded))
                            .background(AppSurface)
                            .clickable { viewModel.openKeypad("Acres") }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = acres,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RATE (₹/ACRE)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, BorderMuted, RoundedCornerShape(standardRounded))
                            .clip(RoundedCornerShape(standardRounded))
                            .background(AppSurface)
                            .clickable { viewModel.openKeypad("Rate") }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = rate,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                }
            }

            Text(
                text = "AMOUNT PAID UPFRONT (₹)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, BorderMuted, RoundedCornerShape(standardRounded))
                    .clip(RoundedCornerShape(standardRounded))
                    .background(AppSurface)
                    .clickable { viewModel.openKeypad("Advance") }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Payments, "Money", tint = PrimaryGreen)
                    Text(
                        text = advancePaid,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.saveAsDefaultRate.value = !saveAsDefault }
            ) {
                Checkbox(
                    checked = saveAsDefault,
                    onCheckedChange = { viewModel.saveAsDefaultRate.value = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                )
                Column {
                    Text(
                        text = "Save as Default Rate",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Updates price for future entries of this equipment",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (activeFarmer == null) {
                        Toast.makeText(context, "ERROR: Please select a farmer first!", Toast.LENGTH_SHORT).show()
                    } else if (doubleAcres <= 0.0) {
                        Toast.makeText(context, "ERROR: Area cannot be zero!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addWorkLog()
                        onSaveSuccess()
                        Toast.makeText(context, "Work entry saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_work_log_button")
                    .height(48.dp),
                shape = RoundedCornerShape(standardRounded),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Save, "Save", tint = Color.White)
                    Text("SAVE WORK LOG", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = {
                    viewModel.selectedFarmerForNewLog.value = null
                    viewModel.typedAcres.value = "0.00"
                    viewModel.typedAdvance.value = "0.00"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(standardRounded),
                border = BorderStroke(1.dp, PrimaryGreen)
            ) {
                Text("CANCEL & DISCARD", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(standardRounded))
                    .background(PrimaryGreen)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL BILL", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(
                            "₹${String.format(Locale.US, "%,.2f", calculatedTotal)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ADVANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(
                            "₹${String.format(Locale.US, "%,.2f", doubleAdvance)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("REMAINING", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Text(
                            "₹${String.format(Locale.US, "%,.2f", calculatedRemaining)}",
                            color = if (calculatedRemaining > 0) HarvestYellow else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Custom Keypad
@Composable
fun NumericKeypadOverlay(
    title: String,
    bufferText: String,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onCancel)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = xlRounded, topEnd = xlRounded),
            colors = CardDefaults.cardColors(containerColor = AppSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = bufferText.ifEmpty { "0.00" },
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        textAlign = TextAlign.End
                    )
                }

                Divider(color = BorderMuted)
                Spacer(modifier = Modifier.height(16.dp))

                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(".", "0", "BACKSPACE")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rows.forEach { rowList ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowList.forEach { key ->
                                Button(
                                    onClick = {
                                        if (key == "BACKSPACE") onBackspace() else onKeyPress(key)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp),
                                    shape = RoundedCornerShape(standardRounded),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (key == "BACKSPACE") Color(0xFFFFDAD6) else SurfaceContainerLow,
                                        contentColor = if (key == "BACKSPACE") ErrorRed else OnSurface
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    if (key == "BACKSPACE") {
                                        Icon(Icons.Default.Backspace, "Backspace")
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(standardRounded),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(
                        text = "Confirm",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. EXPENSES SCREEN
// ==========================================
@Composable
fun ExpensesScreen(
    viewModel: KrishiViewModel,
    allExpenses: List<Expense>,
    totalSum: Double,
    onAddExpenseClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = allExpenses.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderMuted, RoundedCornerShape(lgRounded))
                        .clip(RoundedCornerShape(lgRounded))
                        .background(AppSurface)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL EXPENSES (OCT)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(Locale.US, "%,.0f", totalSum),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tinyRounded))
                                    .background(HarvestYellow)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.TrendingUp, "Up", modifier = Modifier.size(10.dp))
                                    Text("+12% vs last month", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = "Invoice Icon",
                            tint = OnSurfaceVariant.copy(alpha = 0.1f),
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }
            }

            // Search row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("expense_search_input"),
                        placeholder = { Text("Search bills...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderMuted
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(lgRounded)
                    )

                    Button(
                        onClick = { /* empty */ },
                        shape = RoundedCornerShape(lgRounded),
                        border = BorderStroke(1.dp, BorderMuted),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppSurface,
                            contentColor = OnSurfaceVariant
                        ),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.FilterList, "Filter")
                            Text("Filters", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "RECENT EXPENSES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No bills matching search filters found.", color = OnSurfaceVariant)
                    }
                }
            } else {
                items(filtered) { exp ->
                    ExpenseRowCard(exp)
                }
            }

            item {
                OutlinedButton(
                    onClick = onAddExpenseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_expense_bottom_button")
                        .height(48.dp),
                    shape = RoundedCornerShape(standardRounded),
                    border = BorderStroke(1.5.dp, PrimaryGreen)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add", tint = PrimaryGreen)
                        Text(
                            text = "Add Expense",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ExpenseRowCard(exp: Expense) {
    val icon = when (exp.category) {
        "Repair" -> Icons.Default.Build
        "Fuel" -> Icons.Default.LocalGasStation
        "Parts" -> Icons.Default.Settings
        else -> Icons.Default.Receipt
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(standardRounded),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, BorderMuted)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(standardRounded))
                        .background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = exp.category,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = exp.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${exp.date} • ${exp.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "₹${String.format(Locale.US, "%,.0f", exp.amount)}",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = PrimaryGreen
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Edit",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ==========================================
// 5. PROFILE SCREEN
// ==========================================
@Composable
fun ProfileScreen(
    viewModel: KrishiViewModel,
    allEquipments: List<Equipment>,
    onAddEquipmentClick: () -> Unit,
    onEditRatesClick: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User hero details
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderMuted, RoundedCornerShape(lgRounded))
                    .clip(RoundedCornerShape(lgRounded))
                    .background(AppSurface)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=200",
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(xlRounded))
                            .border(1.5.dp, PrimaryGreen, RoundedCornerShape(xlRounded))
                    )

                    Column {
                        Text(
                            text = "Rajesh Kumar",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+91 98765 43210",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tinyRounded))
                                .background(HarvestYellow)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Lead Manager",
                                color = OnSecondaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EQUIPMENT & RATES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Edit Rates",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    modifier = Modifier
                        .clickable(onClick = onEditRatesClick)
                        .padding(4.dp)
                )
            }
        }

        items(allEquipments) { eq ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(standardRounded),
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                border = BorderStroke(1.dp, BorderMuted)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(standardRounded))
                                .background(SurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            val logo = if (eq.iconName == "agriculture") Icons.Default.Agriculture else Icons.Default.Build
                            Icon(logo, "Equip Icon", tint = PrimaryGreen)
                        }
                        Column {
                            Text(text = eq.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(text = "Default: ${eq.unitType}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                    Text(
                        text = "₹${String.format(Locale.US, "%,.0f", eq.rate)}",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = PrimaryGreen
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onAddEquipmentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(standardRounded),
                border = BorderStroke(2.dp, PrimaryGreen)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, "Add Equipment", tint = PrimaryGreen)
                    Text("Add New Equipment", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = "BACKUP & SECURITY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderMuted, RoundedCornerShape(standardRounded))
                    .clip(RoundedCornerShape(standardRounded))
                    .background(AppSurface)
            ) {
                SettingsItem(
                    icon = Icons.Outlined.GridOn,
                    title = "Backup to Google Sheets",
                    subtitle = null,
                    onClick = {
                        Toast.makeText(context, "Exporting to Sheets completed!", Toast.LENGTH_SHORT).show()
                    }
                )
                Divider(color = BorderMuted)
                SettingsItem(
                    icon = Icons.Outlined.CloudSync,
                    title = "Cloud Sync Status",
                    subtitle = "Last synced: 2 mins ago",
                    onClick = {
                        viewModel.syncData()
                        Toast.makeText(context, "Synced successfully with server!", Toast.LENGTH_SHORT).show()
                    }
                )
                Divider(color = BorderMuted)
                SettingsItem(
                    icon = Icons.Outlined.FileDownload,
                    title = "Export Data (CSV/PDF)",
                    subtitle = null,
                    onClick = {
                        Toast.makeText(context, "Krishi_Bill_Data.csv download initiated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item {
            Text(
                text = "GENERAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderMuted, RoundedCornerShape(standardRounded))
                    .clip(RoundedCornerShape(standardRounded))
                    .background(AppSurface)
            ) {
                SettingsItem(
                    icon = Icons.Outlined.HelpOutline,
                    title = "Help & Support",
                    subtitle = null,
                    onClick = {
                        Toast.makeText(context, "Support desk: support@krishibill.com", Toast.LENGTH_LONG).show()
                    }
                )
                Divider(color = BorderMuted)
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "About App",
                    subtitle = null,
                    onClick = {
                        Toast.makeText(context, "Agri-Utility Pro stable binary v2.4.1", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        item {
            Button(
                onClick = {
                    Toast.makeText(context, "Logging out user session...", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .testTag("logout_button")
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(standardRounded),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, "Logout", tint = Color.White)
                    Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = "Version 2.4.1 (Stable Build)",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, fontSize = 11.sp)
                }
            }
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Arrow", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

// Dialog boxes

// Add Farmer Dialog Box
@Composable
fun AddFarmerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var duesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Farmer Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Farmer Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_farmer_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (+91...)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )
                OutlinedTextField(
                    value = duesText,
                    onValueChange = { duesText = it },
                    label = { Text("Initial Pending Dues (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty() && phone.isNotEmpty()) {
                        val dues = duesText.toDoubleOrNull() ?: 0.0
                        onSave(name, phone, dues)
                    }
                }
            ) {
                Text("SAVE", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = ErrorRed)
            }
        },
        shape = RoundedCornerShape(standardRounded),
        containerColor = AppSurface
    )
}

// Add Expense Dialog Box
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Repair") }
    val categories = listOf("Repair", "Fuel", "Parts", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Bill / Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title (e.g. Purchase Cylinder)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_expense_title"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )

                Text("Category Type:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestYellow,
                                selectedLabelColor = OnSecondaryContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotEmpty()) {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        onSave(title, amount, category)
                    }
                }
            ) {
                Text("LOG EXPENSE", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = ErrorRed)
            }
        },
        shape = RoundedCornerShape(standardRounded),
        containerColor = AppSurface
    )
}

// Add Equipment Dialog Box
@Composable
fun AddEquipmentDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }
    var unitType by remember { mutableStateOf("Per Acre") }
    val units = listOf("Per Acre", "Per Hour")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Farm Equipment Asset", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Equipment Name (e.g. Seed Drill)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_equipment_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Default Rental Rate (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryGreen)
                )

                Text("Billing Basis Unit:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    units.forEach { unit ->
                        FilterChip(
                            selected = unitType == unit,
                            onClick = { unitType = unit },
                            label = { Text(unit) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestYellow,
                                selectedLabelColor = OnSecondaryContainer
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty()) {
                        val rate = rateText.toDoubleOrNull() ?: 0.0
                        onSave(name, rate, unitType)
                    }
                }
            ) {
                Text("ADD EQUIPMENT", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = ErrorRed)
            }
        },
        shape = RoundedCornerShape(standardRounded),
        containerColor = AppSurface
    )
}

// Edit Rates Info popup
@Composable
fun EditRatesHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editing Equipment Rental Rates", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Default rates of registered machinery and tractors can be modified or updated dynamically directly inside the 'New Work Entry' screen by selecting an equipment, altering its rate field with the digital numpad, and checking the 'Save as Default Rate' configuration button."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("UNDERSTOOD", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(standardRounded),
        containerColor = AppSurface
    )
}
