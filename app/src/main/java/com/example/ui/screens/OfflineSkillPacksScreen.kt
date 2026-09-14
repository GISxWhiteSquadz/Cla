package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RepairSkillPacksCatalog
import com.example.model.DiagnosticProcedureItem
import com.example.model.RepairSkillPack
import com.example.model.RepairTemplateItem
import com.example.model.SafetyInstructionItem
import com.example.ui.RepairViewModel
import com.example.ui.components.calculateScrollProgress
import com.example.ui.components.interactiveBounce
import com.example.ui.components.pulsingGlow
import com.example.ui.components.scrollReactiveBackground
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineSkillPacksScreen(
    viewModel: RepairViewModel,
    onBack: () -> Unit
) {
    val allPacksEntities by viewModel.allSkillPacks.collectAsState()
    val downloadingPackId by viewModel.downloadingPackId.collectAsState()
    val selectedSkillPack by viewModel.selectedSkillPack.collectAsState()
    val categoryFilter by viewModel.skillPackCategoryFilter.collectAsState()
    val searchQuery by viewModel.skillPackSearchQuery.collectAsState()

    val allDomainPacks = remember(allPacksEntities) {
        allPacksEntities.map { RepairSkillPacksCatalog.toDomain(it) }
    }

    val installedPacks = allDomainPacks.filter { it.isInstalled }
    val totalInstalledStorageMb = installedPacks.sumOf { it.sizeMb }

    // Filter categories
    val categories = listOf("Alle", "Smartphones & Tablets", "Batterietechnik & Power", "Elektronik & SMD", "PC & Workstations", "Mobilität & Akkus", "Gaming & Entertainment", "Haushalt & Mechanik", "Rettung & Reinigung")

    val filteredPacks = remember(allDomainPacks, categoryFilter, searchQuery) {
        allDomainPacks.filter { pack ->
            val matchCategory = categoryFilter == "Alle" || pack.category.equals(categoryFilter, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                    pack.title.contains(searchQuery, ignoreCase = true) ||
                    pack.description.contains(searchQuery, ignoreCase = true) ||
                    pack.supportedKeywords.any { it.contains(searchQuery, ignoreCase = true) }
            matchCategory && matchSearch
        }
    }

    val listState = rememberLazyListState()
    val scrollProgress = listState.calculateScrollProgress(maxItemIndexThreshold = 4)

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .scrollReactiveBackground(scrollProgress = scrollProgress, baseBackground = DarkBackground),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .border(width = 1.dp, color = DarkBorder)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .interactiveBounce { onBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Offline 'Repair Skill' Packs",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .border(0.5.dp, SuccessGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    color = SuccessGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Vorlagen, Sicherheitsregeln & Tests für den lokalen Agenten",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSkillPackSearchQuery(it) },
                    placeholder = { Text("Packs & Reparaturen durchsuchen...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSkillPackSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Scroll Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = categoryFilter == cat
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setSkillPackCategoryFilter(cat) },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = DarkSurfaceElevated,
                                labelColor = TextSecondary,
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = DarkBorder,
                                selectedBorderColor = PrimaryBlue
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 14.dp,
                end = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Status & Storage Banner
            item {
                SkillPacksSummaryCard(
                    installedCount = installedPacks.size,
                    totalCount = allDomainPacks.size,
                    storageUsedMb = totalInstalledStorageMb
                )
            }

            if (filteredPacks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = TextMuted, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Keine Skill-Packs gefunden", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Passe deine Filterkriterien oder den Suchbegriff an.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredPacks, key = { it.packId }) { pack ->
                    val isDownloading = downloadingPackId == pack.packId
                    SkillPackCard(
                        pack = pack,
                        isDownloading = isDownloading,
                        onInspect = { viewModel.selectSkillPack(pack) },
                        onDownload = { viewModel.downloadSkillPack(pack.packId) },
                        onUninstall = { viewModel.uninstallSkillPack(pack.packId) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Detail Modal / Sheet
    if (selectedSkillPack != null) {
        SkillPackDetailDialog(
            pack = selectedSkillPack!!,
            isDownloading = downloadingPackId == selectedSkillPack!!.packId,
            onDismiss = { viewModel.selectSkillPack(null) },
            onDownload = { viewModel.downloadSkillPack(selectedSkillPack!!.packId) },
            onUninstall = { viewModel.uninstallSkillPack(selectedSkillPack!!.packId) },
            onApplyTemplate = { template ->
                viewModel.applySkillTemplateToWorkbench(template, selectedSkillPack!!.title)
            }
        )
    }
}

@Composable
fun SkillPacksSummaryCard(
    installedCount: Int,
    totalCount: Int,
    storageUsedMb: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lokaler KI-Wissensspeicher",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$installedCount von $totalCount Packs installiert (${String.format("%.1f", storageUsedMb)} MB Offline-Speicher)",
                    color = AccentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Der KI-Agent greift automatisch ohne Internetverbindung auf alle installierten Prozeduren & Sicherheitsregeln zu.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun SkillPackCard(
    pack: RepairSkillPack,
    isDownloading: Boolean,
    onInspect: () -> Unit,
    onDownload: () -> Unit,
    onUninstall: () -> Unit
) {
    val packIcon = getSkillPackIcon(pack.iconName)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(
                width = 1.dp,
                color = if (pack.isInstalled) SuccessGreen.copy(alpha = 0.4f) else DarkBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onInspect() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Icon, Title, Status Badge
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (pack.isInstalled) PrimaryBlue.copy(alpha = 0.2f) else DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = packIcon,
                        contentDescription = null,
                        tint = if (pack.isInstalled) PrimaryBlue else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pack.title,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (pack.isInstalled) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .border(0.5.dp, SuccessGreen, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Bereit", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "${pack.sizeMb} MB",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${pack.category} • v${pack.version}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Description
            Text(
                text = pack.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Content summary pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SummaryPill(label = "${pack.templates.size} Vorlagen", color = PrimaryBlue)
                SummaryPill(label = "${pack.safetyInstructions.size} Sicherheit", color = DangerRed)
                SummaryPill(label = "${pack.diagnosticProcedures.size} Prüfungen", color = WarningAmber)
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onInspect,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.interactiveBounce { onInspect() }
                ) {
                    Text("Details & Inhalt ansehen", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (pack.isInstalled) {
                        IconButton(
                            onClick = onUninstall,
                            modifier = Modifier
                                .size(32.dp)
                                .interactiveBounce { onUninstall() }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Entfernen", tint = DangerRed.copy(alpha = 0.75f), modifier = Modifier.size(18.dp))
                        }

                        Button(
                            onClick = onInspect,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.interactiveBounce { onInspect() }
                        ) {
                            Text("Öffnen", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            enabled = !isDownloading,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .pulsingGlow(glowColor = PrimaryBlue, isPulsing = !isDownloading)
                                .interactiveBounce(enabled = !isDownloading) { onDownload() }
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lädt...", color = Color.White, fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Laden (${pack.sizeMb} MB)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SkillPackDetailDialog(
    pack: RepairSkillPack,
    isDownloading: Boolean,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onUninstall: () -> Unit,
    onApplyTemplate: (RepairTemplateItem) -> Unit
) {
    var selectedDetailTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Vorlagen (${pack.templates.size})", "Sicherheit (${pack.safetyInstructions.size})", "Diagnose (${pack.diagnosticProcedures.size})")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(getSkillPackIcon(pack.iconName), contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = pack.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pack.category} • v${pack.version}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Schließen", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // Tab Selection
                TabRow(
                    selectedTabIndex = selectedDetailTab,
                    containerColor = DarkSurfaceElevated,
                    contentColor = PrimaryBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedDetailTab]),
                            color = PrimaryBlue,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .height(36.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedDetailTab == index,
                            onClick = { selectedDetailTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedDetailTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedDetailTab == index) PrimaryBlue else TextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedDetailTab) {
                        0 -> {
                            // Templates
                            items(pack.templates, key = { it.id }) { template ->
                                TemplateDetailCard(template = template, onApply = { onApplyTemplate(template) })
                            }
                        }
                        1 -> {
                            // Safety
                            items(pack.safetyInstructions, key = { it.id }) { safety ->
                                SafetyDetailCard(safety = safety)
                            }
                        }
                        2 -> {
                            // Diagnostics
                            items(pack.diagnosticProcedures, key = { it.id }) { diag ->
                                DiagnosticDetailCard(diag = diag)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pack.isInstalled) {
                    TextButton(onClick = onUninstall) {
                        Text("Deinstallieren", color = DangerRed, fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (!pack.isInstalled) {
                    Button(
                        onClick = onDownload,
                        enabled = !isDownloading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .pulsingGlow(glowColor = PrimaryBlue, isPulsing = !isDownloading)
                            .interactiveBounce(enabled = !isDownloading) { onDownload() }
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Herunterladen...", color = Color.White, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Herunterladen (${pack.sizeMb} MB)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.interactiveBounce { onDismiss() }
                    ) {
                        Text("Fertig", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun TemplateDetailCard(
    template: RepairTemplateItem,
    onApply: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = template.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "${template.estimatedMinutes} Min. • ${template.difficulty}", color = AccentCyan, fontSize = 10.sp)
                }
            }

            Text(text = template.description, color = TextSecondary, fontSize = 11.sp)

            if (template.toolsNeeded.isNotEmpty()) {
                Text(text = "Werkzeuge: ${template.toolsNeeded.joinToString(", ")}", color = WarningAmber, fontSize = 11.sp)
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                template.steps.forEachIndexed { i, step ->
                    Text(text = "${i + 1}. $step", color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .interactiveBounce { onApply() }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("In Werkbank anwenden", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SafetyDetailCard(safety: SafetyInstructionItem) {
    val isCritical = safety.severity.equals("KRITISCH", ignoreCase = true)
    val accentColor = if (isCritical) DangerRed else WarningAmber

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Security,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "[${safety.severity}] ${safety.title}", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Text(text = safety.description, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)

            if (safety.protectiveGear.isNotEmpty()) {
                Text(text = "Schutzkleidung: ${safety.protectiveGear.joinToString(", ")}", color = AccentCyan, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun DiagnosticDetailCard(diag: DiagnosticProcedureItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, SecondaryTeal.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = diag.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = "Symptom: ${diag.symptomTarget}", color = WarningAmber, fontSize = 11.sp)
            Text(text = "Messmethode: ${diag.testMethod}", color = TextSecondary, fontSize = 11.sp)
            Text(text = "Erwartetes Soll: ${diag.expectedResult}", color = SuccessGreen, fontSize = 11.sp)
            Text(text = "Fehlerbedeutung: ${diag.failureMeaning}", color = DangerRed, fontSize = 11.sp)
        }
    }
}

fun getSkillPackIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "smartphone" -> Icons.Default.Smartphone
        "batterychargingfull" -> Icons.Default.BatteryChargingFull
        "memory" -> Icons.Default.Memory
        "waterdrop" -> Icons.Default.WaterDrop
        "computer" -> Icons.Default.Computer
        "directionsbike" -> Icons.Default.DirectionsBike
        "sportsesports" -> Icons.Default.SportsEsports
        "coffee" -> Icons.Default.Coffee
        else -> Icons.Default.Build
    }
}
