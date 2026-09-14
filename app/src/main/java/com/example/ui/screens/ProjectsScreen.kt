package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RepairProjectEntity
import com.example.ui.RepairViewModel
import com.example.ui.screens.office.OfficeArchiveSection
import com.example.ui.screens.office.OfficeBudgetCompareSection
import com.example.ui.screens.office.OfficeDocumentEditor
import com.example.ui.screens.office.OfficeOrdersSection
import com.example.ui.theme.*
import com.example.ui.util.bounceClick
import com.example.ui.util.rememberPulseAlpha
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun ProjectsScreen(viewModel: RepairViewModel) {
    val projects by viewModel.projectsList.collectAsState()
    val officeDocs by viewModel.officeDocuments.collectAsState()
    val selectedSubTab by viewModel.selectedOfficeSubTab.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Project filtering & search states
    var projectSearchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Alle") }

    val statusFilterOptions = listOf("Alle", "In Diagnose", "In Reparatur", "Ersatzteil bestellt", "Abgeschlossen")

    val filteredProjects = remember(projects, projectSearchQuery, selectedStatusFilter) {
        projects.filter { p ->
            val matchesQuery = projectSearchQuery.isBlank() ||
                    p.title.contains(projectSearchQuery, ignoreCase = true) ||
                    p.deviceModel.contains(projectSearchQuery, ignoreCase = true) ||
                    p.notes.contains(projectSearchQuery, ignoreCase = true)

            val matchesStatus = if (selectedStatusFilter == "Alle") true else p.status.equals(selectedStatusFilter, ignoreCase = true)

            matchesQuery && matchesStatus
        }
    }

    val activeCount = remember(projects) { projects.count { it.status != "Abgeschlossen" } }
    val waitingPartsCount = remember(projects) { projects.count { it.status == "Ersatzteil bestellt" } }
    val completedCount = remember(projects) { projects.count { it.status == "Abgeschlossen" } }

    val subTabs = listOf(
        SubTabItem(0, "Reparaturen", Icons.Filled.BuildCircle, count = projects.size),
        SubTabItem(1, "Aufträge & Kasse", Icons.AutoMirrored.Filled.ReceiptLong, count = officeDocs.size),
        SubTabItem(2, "Dokument-Editor", Icons.Filled.EditNote),
        SubTabItem(3, "Budget & Vergleich", Icons.Filled.Balance),
        SubTabItem(4, "Dateiarchiv", Icons.Filled.Inventory2, count = officeDocs.size)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(12.dp))

            // Unified Clean Header: "Office"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Office",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Compact Gemini AI Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF132A46),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .bounceClick { viewModel.openGeminiChat() }
                        .testTag("projects_gemini_ai_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Gemini KI",
                            tint = AccentCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Gemini KI",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // High-Tech Capsule Sub-Tab Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                subTabs.forEach { tab ->
                    val isSelected = selectedSubTab == tab.index
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.94f else if (isSelected) 1.02f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "subtab_scale_${tab.index}"
                    )

                    Surface(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                viewModel.selectOfficeSubTab(tab.index)
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PrimaryBlue else DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryBlueVariant else DarkBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )

                            if (tab.count != null && tab.count > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else DarkSurfaceElevated
                                ) {
                                    Text(
                                        text = "${tab.count}",
                                        color = if (isSelected) Color.White else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-Tab Content with smooth transitions
            androidx.compose.animation.AnimatedContent(
                targetState = selectedSubTab,
                transitionSpec = {
                    androidx.compose.animation.fadeIn(animationSpec = tween(300)) togetherWith
                    androidx.compose.animation.fadeOut(animationSpec = tween(300))
                },
                modifier = Modifier.weight(1f),
                label = "SubTabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // TAB 0: REPARATUR-PROJEKTE & CHECKLISTEN
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Project KPI Pills Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KpiCounterPill(
                                label = "In Arbeit",
                                count = activeCount,
                                accentColor = PrimaryBlue,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCounterPill(
                                label = "Wartet auf Teile",
                                count = waitingPartsCount,
                                accentColor = WarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                            KpiCounterPill(
                                label = "Fertig",
                                count = completedCount,
                                accentColor = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Search and Filter Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = projectSearchQuery,
                                onValueChange = { projectSearchQuery = it },
                                placeholder = { Text("Projekt, Gerät oder Notiz suchen...", color = TextMuted, fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                },
                                trailingIcon = {
                                    if (projectSearchQuery.isNotBlank()) {
                                        IconButton(onClick = { projectSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Filled.Close, contentDescription = "Löschen", tint = TextMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                        }

                        // Status Filter Chips
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(statusFilterOptions) { filter ->
                                val isSelected = selectedStatusFilter == filter
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) PrimaryBlueVariant.copy(alpha = 0.3f) else DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) PrimaryBlue else DarkBorder
                                    ),
                                    modifier = Modifier.bounceClick { selectedStatusFilter = filter }
                                ) {
                                    Text(
                                        text = filter,
                                        color = if (isSelected) AccentCyan else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Projects List
                        if (filteredProjects.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FolderOpen,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = if (projectSearchQuery.isNotBlank() || selectedStatusFilter != "Alle")
                                            "Keine Reparaturprojekte gefunden"
                                        else
                                            "Noch keine Reparaturprojekte angelegt",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Lege jetzt ein Projekt an – mit Checkliste, Gerätedaten & Notizen.",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                items(filteredProjects, key = { it.id }) { project ->
                                    ModernProjectCard(
                                        project = project,
                                        onChecklistToggle = { index, done ->
                                            viewModel.updateProjectChecklist(project, index, done)
                                        },
                                        onAddChecklistItem = { text ->
                                            viewModel.addProjectChecklistItem(project, text)
                                        },
                                        onStatusChange = { newStatus ->
                                            viewModel.updateProjectStatus(project, newStatus)
                                        },
                                        onDelete = {
                                            viewModel.deleteProject(project.id)
                                        },
                                        onDiagnoseDevice = {
                                            viewModel.startDiagnosis(project.deviceModel, project.category)
                                        },
                                        onOpenWorkbench = {
                                            viewModel.openWorkbench()
                                        },
                                        onCreateOfficeOrder = {
                                            viewModel.createOfficeDocument(
                                                title = "Reparaturauftrag: ${project.title}",
                                                docType = "Auftrag",
                                                categoryFolder = "Reparatur- & PC-Aufträge",
                                                customerName = "",
                                                customerContact = "",
                                                deviceOrProject = "${project.deviceModel} (${project.category})",
                                                content = "Auftrag für Reparatur ${project.title} (${project.deviceModel})\nNotizen: ${project.notes}",
                                                lineItemsJson = "[]",
                                                subtotal = 0.0,
                                                taxRate = 19.0,
                                                total = 0.0,
                                                budgetTarget = 0.0
                                            )
                                            viewModel.selectOfficeSubTab(2) // Navigate to editor
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    OfficeOrdersSection(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
                2 -> {
                    OfficeDocumentEditor(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
                3 -> {
                    OfficeBudgetCompareSection(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
                4 -> {
                    OfficeArchiveSection(viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
            }
        }
        }

        // Floating Action Button to Add Order (Only on Tab 0)
        if (selectedSubTab == 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp, start = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue,
                    shadowElevation = 6.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .bounceClick { showCreateDialog = true }
                        .testTag("add_project_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Neuer Auftrag",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Neuer Auftrag",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ModernCreateProjectDialog(
            categories = viewModel.categories.map { it.title.replace("\n", " ") },
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, device, category, notes ->
                viewModel.createNewProject(title, device, category, notes)
                showCreateDialog = false
            }
        )
    }
}

data class SubTabItem(
    val index: Int,
    val title: String,
    val icon: ImageVector,
    val count: Int? = null
)

@Composable
fun KpiCounterPill(
    label: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)) {
            Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = "$count",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun ModernProjectCard(
    project: RepairProjectEntity,
    onChecklistToggle: (Int, Boolean) -> Unit,
    onAddChecklistItem: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDiagnoseDevice: () -> Unit,
    onOpenWorkbench: () -> Unit,
    onCreateOfficeOrder: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var isChecklistExpanded by remember { mutableStateOf(true) }
    var newStepText by remember { mutableStateOf("") }
    var showAddStepInput by remember { mutableStateOf(false) }

    // Parse checklist items
    val items = remember(project.checklistJson) {
        val list = mutableListOf<Pair<String, Boolean>>()
        try {
            val arr = JSONArray(project.checklistJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(obj.getString("text") to obj.optBoolean("done", false))
            }
        } catch (e: Exception) {
            // fallback
        }
        list
    }

    val totalCount = items.size
    val completedCount = items.count { it.second }
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val statusColor = when (project.status) {
        "Abgeschlossen" -> SuccessGreen
        "In Reparatur" -> PrimaryBlue
        "Ersatzteil bestellt" -> WarningAmber
        else -> AccentCyan
    }

    val categoryIcon = when {
        project.category.contains("Computer", ignoreCase = true) || project.category.contains("PC", ignoreCase = true) -> Icons.Filled.Computer
        project.category.contains("Smartphone", ignoreCase = true) || project.category.contains("Handy", ignoreCase = true) -> Icons.Filled.Smartphone
        project.category.contains("Haushalt", ignoreCase = true) || project.category.contains("Kaffee", ignoreCase = true) -> Icons.Filled.Coffee
        project.category.contains("Fahrrad", ignoreCase = true) || project.category.contains("Bike", ignoreCase = true) -> Icons.AutoMirrored.Filled.DirectionsBike
        project.category.contains("Fahrzeug", ignoreCase = true) || project.category.contains("KFZ", ignoreCase = true) -> Icons.Filled.DirectionsCar
        project.category.contains("Robotik", ignoreCase = true) || project.category.contains("Maker", ignoreCase = true) -> Icons.Filled.SmartToy
        project.category.contains("Audio", ignoreCase = true) || project.category.contains("HiFi", ignoreCase = true) -> Icons.Filled.Headphones
        else -> Icons.Filled.Build
    }

    val pulseAlpha = rememberPulseAlpha()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Icon Avatar, Title, Device, Status Menu & Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Avatar with subtle border & background
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = project.category,
                            tint = AccentCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${project.deviceModel} • ${project.category}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Interactive Status Badge Pill with Pulsing Live Dot
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier.bounceClick { showStatusMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor.copy(alpha = if (project.status != "Abgeschlossen") pulseAlpha else 1f))
                            )
                            Text(
                                text = project.status,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        listOf("In Diagnose", "In Reparatur", "Ersatzteil bestellt", "Abgeschlossen").forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val c = when (status) {
                                            "Abgeschlossen" -> SuccessGreen
                                            "In Reparatur" -> PrimaryBlue
                                            "Ersatzteil bestellt" -> WarningAmber
                                            else -> AccentCyan
                                        }
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(c))
                                        Text(status, color = TextPrimary, fontSize = 13.sp)
                                    }
                                },
                                onClick = {
                                    onStatusChange(status)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Projekt löschen",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar with Gradient & Step Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Arbeitsschritte",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "($completedCount/$totalCount)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = if (progress >= 1f) SuccessGreen else PrimaryBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Animated Linear Progress
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 400),
                label = "progress_${project.id}"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DarkSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (progress >= 1f)
                                Brush.horizontalGradient(listOf(SuccessGreen, Color(0xFF34D399)))
                            else
                                Brush.horizontalGradient(listOf(PrimaryBlue, AccentCyan))
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Checklist Section Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isChecklistExpanded = !isChecklistExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isChecklistExpanded) "Checkliste ausblenden" else "Checkliste anzeigen (${items.size} Schritte)",
                    color = AccentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isChecklistExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(
                visible = isChecklistExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items.forEachIndexed { index, (text, done) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onChecklistToggle(index, !done) }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Checkbox(
                                checked = done,
                                onCheckedChange = { onChecklistToggle(index, it) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = PrimaryBlue,
                                    uncheckedColor = TextMuted,
                                    checkmarkColor = Color.White
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = text,
                                color = if (done) TextMuted else TextPrimary,
                                fontSize = 12.sp,
                                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }

                    // Add Custom Step Inline
                    if (showAddStepInput) {
                        val focusManager = LocalFocusManager.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = newStepText,
                                onValueChange = { newStepText = it },
                                placeholder = { Text("Neuer Schritt...", color = TextMuted, fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceElevated,
                                    unfocusedContainerColor = DarkSurfaceElevated,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (newStepText.isNotBlank()) {
                                        onAddChecklistItem(newStepText)
                                        newStepText = ""
                                        showAddStepInput = false
                                        focusManager.clearFocus()
                                    }
                                }),
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                            IconButton(
                                onClick = {
                                    if (newStepText.isNotBlank()) {
                                        onAddChecklistItem(newStepText)
                                        newStepText = ""
                                        showAddStepInput = false
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Hinzufügen", tint = PrimaryBlue)
                            }
                            IconButton(onClick = { showAddStepInput = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Abbrechen", tint = TextMuted)
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { showAddStepInput = true },
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Schritt hinzufügen", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Notes banner if present
            if (project.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Text(
                            text = project.notes,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row: Diagnose, Werkbank, Auftrag in Kasse
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick { onDiagnoseDevice() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.AutoFixHigh, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("KI-Guide", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick { onOpenWorkbench() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.ElectricMeter, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Werkbank", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryBlueVariant.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1.3f)
                        .bounceClick { onCreateOfficeOrder() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("In Kasse / Auftrag", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernCreateProjectDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, device: String, category: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var device by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Computer & PC") }
    var notes by remember { mutableStateOf("") }

    // Quick presets for real-world scenarios
    val presets = listOf(
        ProjectPreset(
            name = "Gaming-PC",
            title = "Gaming PC Zusammenbau & Burn-In Stresstest",
            device = "Custom Gaming PC (AMD AM5 / RTX 4070)",
            category = "Computer & PC",
            notes = "Komponenten prüfen, Kabelmanagement, BIOS Flash & 30min AIDA64 Stresstest"
        ),
        ProjectPreset(
            name = "Smartphone",
            title = "iPhone / Galaxy Display- & Akkutausch",
            device = "Apple iPhone 14 Pro",
            category = "Smartphones & Tablets",
            notes = "Original Klebedichtung erneuern, Akkukapazität und TrueTone kalibrieren"
        ),
        ProjectPreset(
            name = "Kaffeevollautomat",
            title = "DeLonghi Brüheinheit Revision & Entkalkung",
            device = "DeLonghi Magnifica S",
            category = "Haushalt & Kleingeräte",
            notes = "O-Ringe erneuern, Mahlwerk reinigen, Druckprüfung 15 bar"
        ),
        ProjectPreset(
            name = "E-Bike Akku",
            title = "E-Bike Akku BMS & Kapazitätstest",
            device = "Bosch PowerPack 500",
            category = "Fahrrad & E-Bike",
            notes = "Zellenspannungen messen, BMS Ruhestrom prüfen, Zyklentest"
        ),
        ProjectPreset(
            name = "Konsole",
            title = "PS5 HDMI-Port Tausch & Flüssigmetall",
            device = "Sony PlayStation 5",
            category = "Konsolen & Gaming",
            notes = "HDMI Buchse mikrolöten, APU Flüssigmetall neu verteilen, Lüfter entstauben"
        )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.BuildCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                Text("Neues Reparaturprojekt anlegen", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick preset pills
                Text("Schnellvorlagen:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.bounceClick {
                                title = preset.title
                                device = preset.device
                                category = preset.category
                                notes = preset.notes
                            }
                        ) {
                            Text(
                                text = preset.name,
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Projekttitel *", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("z.B. iPhone 14 Pro Displaytausch", color = TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = device,
                    onValueChange = { device = it },
                    label = { Text("Gerätemodell / Hardware *", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("z.B. iPhone 14 Pro A2890", color = TextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Fehlerbeschreibung / Ersatzteil-Notizen", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("Symptome, Messwerte, Seriennummern...", color = TextMuted, fontSize = 12.sp) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && device.isNotBlank()) {
                        onConfirm(title, device, category, notes)
                    }
                },
                enabled = title.isNotBlank() && device.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Auftrag starten", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Abbrechen", color = TextSecondary, fontSize = 12.sp)
            }
        }
    )
}

data class ProjectPreset(
    val name: String,
    val title: String,
    val device: String,
    val category: String,
    val notes: String
)
