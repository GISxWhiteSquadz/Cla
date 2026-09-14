package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.model.GGUFModelInfo
import com.example.ui.RepairViewModel
import com.example.ui.components.calculateScrollProgress
import com.example.ui.components.interactiveBounce
import com.example.ui.components.pulsingGlow
import com.example.ui.components.scrollReactiveBackground
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileScreen(viewModel: RepairViewModel) {
    val selectedModelId by viewModel.selectedModelId.collectAsState()
    val selectedProjector by viewModel.selectedProjector.collectAsState()
    val webResearchAllowed by viewModel.webResearchAllowed.collectAsState()
    val diagnosticReport by viewModel.diagnosticReport.collectAsState()
    val isLoadingDiagnostics by viewModel.isLoadingDiagnostics.collectAsState()
    val savedManuals by viewModel.savedManuals.collectAsState()
    val allSkillPacks by viewModel.allSkillPacks.collectAsState()
    val installedSkillPacks by viewModel.installedSkillPacks.collectAsState()

    var showModelPickerDialog by remember { mutableStateOf(false) }
    var showProjectorPickerDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showManualsDialog by remember { mutableStateOf(false) }

    val currentModel = viewModel.availableModels.find { it.id == selectedModelId }
    val listState = rememberLazyListState()
    val scrollProgress = listState.calculateScrollProgress(maxItemIndexThreshold = 5)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .scrollReactiveBackground(scrollProgress = scrollProgress, baseBackground = DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Einstellungen",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Avatar Header Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "CoreSystems Logo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "CoreRepair AI",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lokale KI-Modelle & Systemeinstellungen",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lokal-First • Offline-Bereit",
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Section: Offline 'Repair Skill' Packs
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Offline 'Repair Skill' Packs",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .border(0.5.dp, SuccessGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${installedSkillPacks.size}/${allSkillPacks.size} Aktiv",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Vordefinierte Reparatur-Vorlagen, Sicherheitsregeln & Diagnoseprüfungen für den autonomen Offline-Betrieb.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, if (installedSkillPacks.isNotEmpty()) PrimaryBlue.copy(alpha = 0.4f) else DarkBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.openSkillPacksManager() }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Skill-Packs verwalten",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (installedSkillPacks.isEmpty()) "Keine Packs geladen • Tippen zum Download" else "${installedSkillPacks.size} Packs geladen (${String.format("%.1f", installedSkillPacks.sumOf { it.sizeMb })} MB offline)",
                                    color = if (installedSkillPacks.isEmpty()) TextMuted else AccentCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openSkillPacksManager() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                            modifier = Modifier
                                .pulsingGlow(glowColor = PrimaryBlue)
                                .interactiveBounce { viewModel.openSkillPacksManager() }
                        ) {
                            Text(
                                text = "Öffnen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section: KI-Modell
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "KI-Modell",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Wähle ein lokal installiertes GGUF-Modell (llama.cpp). Es bleibt ausgewählt, bis du es hier änderst.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "Empfehlung für Mobilgeräte: Qwen2.5-VL-3B-Instruct Q4_K_M plus der dazugehörige mmproj-Projektor. Auf älteren Geräten Context Size 2048 verwenden; kleinere Modelle laufen schneller, liefern aber oft weniger genaue Reparaturdetails.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Model selection card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentModel?.name ?: "Kein Modell ausgewählt",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentModel?.let { "${it.sizeLabel} • ${it.recommendedRam}" } ?: "Bitte GGUF-Datei wählen",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showModelPickerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                            modifier = Modifier.testTag("select_model_button")
                        ) {
                            Text(
                                text = if (currentModel != null) "Ändern" else "Wählen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section: Bildanalyse (optional)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Bildanalyse (optional)",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Für Bilder benötigst du den zum Vision-GGUF passenden mmproj-Projektor. Ohne ihn bleibt die normale Textrecherche verfügbar.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Videocam,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (selectedProjector.isNotBlank()) selectedProjector else "Kein Bildprojektor ausgewählt",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (selectedProjector.isNotBlank()) "Vision-Projektor aktiv" else "Nur Text-Modus aktiv",
                                    color = if (selectedProjector.isNotBlank()) SuccessGreen else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showProjectorPickerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                            modifier = Modifier.testTag("select_projector_button")
                        ) {
                            Text(
                                text = if (selectedProjector.isNotBlank()) "Ändern" else "Wählen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section: Webrecherche erlauben Switch Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Webrecherche erlauben",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "DuckDuckGo / Gemini-Suche bei Bedarf; Suchbegriffe verlassen dein Gerät.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = webResearchAllowed,
                        onCheckedChange = { viewModel.setWebResearchAllowed(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryBlue,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkSurfaceElevated
                        ),
                        modifier = Modifier.testTag("web_research_switch")
                    )
                }
            }
        }

        // Additional Settings & Tools List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            ) {
                ProfileSettingRow(
                    icon = Icons.Filled.Book,
                    title = "Gespeicherte Anleitungen",
                    subtitle = "${savedManuals.size} Offline-Handbücher verfügbar",
                    onClick = { showManualsDialog = true }
                )
                ProfileDivider()
                ProfileSettingRow(
                    icon = Icons.Filled.Speed,
                    title = "System-Diagnose & Hardware-Test",
                    subtitle = "GGUF-Engine, RAM, Storage & RAG-Benchmark",
                    onClick = { viewModel.runDiagnostics() }
                )
                ProfileDivider()
                ProfileSettingRow(
                    icon = Icons.Filled.Delete,
                    title = "Verlauf löschen",
                    subtitle = "Alle vergangenen Recherchen entfernen",
                    onClick = { showClearHistoryDialog = true }
                )
                ProfileDivider()
                ProfileSettingRow(
                    icon = Icons.Filled.Info,
                    title = "Über CoreRepair AI",
                    subtitle = "Version 1.0.0 • by CoreSystems",
                    onClick = { showAboutDialog = true }
                )
                ProfileDivider()
                ProfileSettingRow(
                    icon = Icons.Filled.Security,
                    title = "Datenschutz & Hinweise",
                    subtitle = "100% On-Device, keine Nutzerüberwachung",
                    onClick = { showPrivacyDialog = true }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Model Picker Dialog
    if (showModelPickerDialog) {
        AlertDialog(
            onDismissRequest = { showModelPickerDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("GGUF-Modell auswählen", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.availableModels.forEach { model ->
                        val isSelected = model.id == selectedModelId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DarkSurfaceElevated else Color.Transparent)
                                .clickable {
                                    viewModel.setGGUFModel(model.id)
                                    showModelPickerDialog = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setGGUFModel(model.id)
                                    showModelPickerDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PrimaryBlue,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = model.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${model.filename} (${model.sizeLabel})",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelPickerDialog = false }) {
                    Text("Schließen", color = PrimaryBlue)
                }
            }
        )
    }

    // Projector Picker Dialog
    if (showProjectorPickerDialog) {
        AlertDialog(
            onDismissRequest = { showProjectorPickerDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Bildprojektor auswählen", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Option: Keiner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.setProjector("")
                                showProjectorPickerDialog = false
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedProjector.isBlank(),
                            onClick = {
                                viewModel.setProjector("")
                                showProjectorPickerDialog = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kein Bildprojektor (nur Text)", color = TextPrimary, fontSize = 13.sp)
                    }

                    viewModel.availableProjectors.forEach { proj ->
                        val isSelected = proj == selectedProjector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.setProjector(proj)
                                    showProjectorPickerDialog = false
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setProjector(proj)
                                    showProjectorPickerDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(proj, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProjectorPickerDialog = false }) {
                    Text("Fertig", color = PrimaryBlue)
                }
            }
        )
    }

    // Diagnostics Report Dialog
    if (diagnosticReport != null) {
        val report = diagnosticReport!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissDiagnostics() },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("System-Diagnose & Hardware", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DiagnosticItem("GGUF Engine", report.ggufEngineStatus)
                    DiagnosticItem("Aktives Modell", report.selectedModel)
                    DiagnosticItem("CPU Threads", "${report.activeThreads} Threads")
                    DiagnosticItem("Freier RAM", "${report.ramFreeMb} MB / ${report.ramTotalMb} MB")
                    DiagnosticItem("Freier Speicherplatz", "${report.storageFreeMb} MB intern")
                    DiagnosticItem("RAG Chunks in SQLite", "${report.databaseChunksCount} Wissens-Chunks")
                    DiagnosticItem("Aktive Projekte", "${report.databaseProjectsCount} Reparatur-Projekte")
                    DiagnosticItem("Embedding Benchmark", "${report.embeddingLatencyMs} ms (Vektor-Kosinus-Index)")
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissDiagnostics() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("OK", color = Color.White, fontSize = 12.sp)
                }
            }
        )
    }

    // Saved Manuals Dialog
    if (showManualsDialog) {
        AlertDialog(
            onDismissRequest = { showManualsDialog = false },
            containerColor = DarkSurface,
            title = {
                Text("Gespeicherte Anleitungen", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                if (savedManuals.isEmpty()) {
                    Text("Noch keine Anleitungen gespeichert.", color = TextSecondary, fontSize = 13.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        savedManuals.forEach { manual ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(manual.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(manual.summary, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showManualsDialog = false },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Schließen", color = PrimaryBlue, fontSize = 12.sp)
                }
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = DarkSurface,
            title = { Text("Verlauf löschen?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Möchtest du den gesamten Verlauf wirklich leeren?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Löschen", color = Color.White, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearHistoryDialog = false },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Abbrechen", color = TextSecondary, fontSize = 12.sp)
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "CoreSystems Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("CoreRepair AI", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("by CoreSystems", color = TextMuted, fontSize = 11.sp)
                    }
                }
            },
            text = {
                Text(
                    "CoreRepair AI – Lokales GGUF + RAG Reparatur-System.\n\n" +
                            "Entwickelt von CoreSystems für professionelle Werkstätten, Elektroniker und Do-It-Yourself Reparaturen.\n" +
                            "• Lokale Inferenz mit GGUF (llama.cpp)\n" +
                            "• On-Device Vektor-RAG mit SQLite\n" +
                            "• Keine Serverpflicht, keine Cloud-Abos\n" +
                            "• Version 1.0.0",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog = false },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Schließen", color = PrimaryBlue, fontSize = 12.sp)
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = DarkSurface,
            title = { Text("Datenschutz & Hinweise", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Datenschutz-Prinzipien:\n\n" +
                            "1. Lokale Daten: Fotos, Recherchen, Notizen und Projekt-Checklisten werden ausschließlich lokal auf deinem Gerät gespeichert.\n" +
                            "2. Optionale Webrecherche: Nur wenn du den Schalter 'Webrecherche erlauben' aktivierst, werden Anfragen an Suchmaschinen gestellt.\n" +
                            "3. Sicherheitshinweis: Reparaturen an netzspannungsführenden Geräten oder Lithium-Akkus erfordern Sachkunde. Beachte immer alle Sicherheitswarnungen.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showPrivacyDialog = false },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Verstanden", color = PrimaryBlue, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
fun ProfileSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ProfileDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DarkBorder)
    )
}

@Composable
fun DiagnosticItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
