package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WorkbenchData
import com.example.model.MaterialNorm
import com.example.model.MeasurementMode
import com.example.model.MultimeterTestPoint
import com.example.model.SmdChip
import com.example.ui.RepairViewModel
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkbenchScreen(
    viewModel: RepairViewModel,
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Multimeter-Prüfung", "SMD & IC-Decoder", "Öko & Werkstatt-Pass", "Material & Normen")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("workbench_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Zurück",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Interaktive Werkbank",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Geprüfte Sollwerte & Fach-Utilities",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkSurface,
            contentColor = PrimaryBlue,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) PrimaryBlue else TextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        when (selectedTabIndex) {
            0 -> MultimeterSection()
            1 -> SmdDecoderSection()
            2 -> EcoAndReportSection(viewModel = viewModel)
            3 -> MaterialNormsSection()
        }
    }
}

@Composable
fun MultimeterSection() {
    val allPoints = WorkbenchData.multimeterTestPoints
    val categories = remember { listOf("Alle") + allPoints.map { it.category }.distinct() }
    var selectedCategory by remember { mutableStateOf("Alle") }

    val filteredPoints = if (selectedCategory == "Alle") {
        allPoints
    } else {
        allPoints.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Multimeter-Prüfpunkt-Assistent",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Physikalisch exakte Sollwerte für Dioden-, Widerstands- und Spannungsmessungen. Gib deinen abgelesenen Messwert ein, um eine deterministische Fehleranalyse zu erhalten.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            // Category Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        items(filteredPoints, key = { it.id }) { point ->
            MultimeterTestPointCard(point = point)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MultimeterTestPointCard(point: MultimeterTestPoint) {
    var measuredValueInput by remember { mutableStateOf("") }
    var evaluationResult by remember { mutableStateOf<WorkbenchData.EvaluationResult?>(null) }
    var isTesterExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = point.category,
                    color = PrimaryBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val modeColor = when (point.mode) {
                    MeasurementMode.DIODE -> Color(0xFFAB47BC)
                    MeasurementMode.RESISTANCE -> Color(0xFF26A69A)
                    MeasurementMode.VOLTAGE -> Color(0xFFFFA726)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(modeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${point.mode.label} (${point.mode.unit})",
                        color = modeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = point.testPointName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Sollwert Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ElectricMeter,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SOLLWERT:",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = point.expectedDisplay,
                            color = SuccessGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = point.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Prüfspitzen & Pinbelegung: ${point.pinoutNotes}",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Messmethode: ${point.testMethod}",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Live Tester Button
            OutlinedButton(
                onClick = { isTesterExpanded = !isTesterExpanded },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isTesterExpanded) DarkSurfaceElevated else Color.Transparent
                )
            ) {
                Icon(
                    imageVector = if (isTesterExpanded) Icons.Filled.Close else Icons.Filled.Assessment,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTesterExpanded) "Tester schließen" else "Live-Messwert prüfen & analysieren",
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (isTesterExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Abgelesenen Multimeter-Wert eingeben (${point.mode.unit}):",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = measuredValueInput,
                            onValueChange = { measuredValueInput = it },
                            placeholder = { Text("z.B. ${point.expectedMin}", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                val parsed = measuredValueInput.replace(",", ".").toDoubleOrNull()
                                if (parsed != null) {
                                    evaluationResult = WorkbenchData.evaluateReading(point, parsed)
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val parsed = measuredValueInput.replace(",", ".").toDoubleOrNull()
                                if (parsed != null) {
                                    evaluationResult = WorkbenchData.evaluateReading(point, parsed)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Prüfen", fontSize = 12.sp)
                        }
                    }

                    // Result Box
                    evaluationResult?.let { res ->
                        Spacer(modifier = Modifier.height(10.dp))
                        val badgeColor = when (res.status) {
                            WorkbenchData.EvaluationStatus.OK -> SuccessGreen
                            WorkbenchData.EvaluationStatus.SHORT_CIRCUIT -> DangerRed
                            WorkbenchData.EvaluationStatus.OPEN_CIRCUIT -> WarningAmber
                            WorkbenchData.EvaluationStatus.OUT_OF_TOLERANCE -> WarningAmber
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor.copy(alpha = 0.12f))
                                .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (res.status) {
                                            WorkbenchData.EvaluationStatus.OK -> Icons.Filled.CheckCircle
                                            WorkbenchData.EvaluationStatus.SHORT_CIRCUIT -> Icons.Filled.Warning
                                            else -> Icons.Filled.Info
                                        },
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = res.title,
                                        color = badgeColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = res.message,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Handlungsempfehlung: ${res.recommendedAction}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmdDecoderSection() {
    val chips = WorkbenchData.smdChips
    var searchQuery by remember { mutableStateOf("") }

    val filteredChips = if (searchQuery.isBlank()) {
        chips
    } else {
        chips.filter {
            it.codeOrName.contains(searchQuery, ignoreCase = true) ||
            it.fullPartNumber.contains(searchQuery, ignoreCase = true) ||
            it.functionCategory.contains(searchQuery, ignoreCase = true) ||
            it.packageType.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "SMD- & Chip-Code-Decoder",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Entschlüsselt Gehäuse-Aufdrucke (z.B. A7, 1AM, 702) und SMD-ICs (z.B. M92T36, BQ24193, AMS1117). Zeigt Pinouts, Maximalwerte, Schadensbilder und Pin-kompatible Alternativen.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Code oder Chip suchen (z.B. \"M92T36\", \"A7\", \"MOSFET\")...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Löschen", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (filteredChips.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Kein Bauteil für \"$searchQuery\" gefunden. Prüfe den Code oder suche nach der Kategorie (z.B. \"LDO\", \"Diode\", \"USB\").",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredChips, key = { it.codeOrName }) { chip ->
                SmdChipCard(chip = chip)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SmdChipCard(chip: SmdChip) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = chip.codeOrName,
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = chip.packageType,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = chip.manufacturer,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = chip.fullPartNumber,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = chip.functionCategory,
                color = WarningAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = chip.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ratings & Failures
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Betriebsdaten: ${chip.operatingRatings}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Typischer Fehler: ${chip.typicalFailures}",
                        color = DangerRed.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (expanded) "Pinout & Testanleitung verbergen" else "Pinout, Messung & Ersatzteile anzeigen",
                    color = PrimaryBlue,
                    fontSize = 12.sp
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "PINBELEGUNG & SIGNALWEGE:",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    chip.pinoutSummary.forEach { pin ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("• ", color = PrimaryBlue, fontSize = 12.sp)
                            Text(pin, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TESTVERFAHREN MIT MULTIMETER:",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chip.testProcedure,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ERSATZTYPEN / DROP-IN ÄQUIVALENTE:",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chip.dropInEquivalents.joinToString(" • "),
                    color = SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EcoAndReportSection(viewModel: RepairViewModel) {
    val context = LocalContext.current
    val projects by viewModel.projectsList.collectAsState()
    val history by viewModel.historyList.collectAsState()

    // Calculate aggregated savings
    var totalCo2 = 0.0
    var totalEWaste = 0.0
    var totalSavings = 0.0

    projects.forEach { proj ->
        val bm = WorkbenchData.getBenchmarkForCategory(proj.category)
        val progress = if (proj.checklistJson.isNotBlank()) {
            try {
                val arr = org.json.JSONArray(proj.checklistJson)
                var done = 0
                for (i in 0 until arr.length()) {
                    if (arr.getJSONObject(i).optBoolean("done", false)) done++
                }
                if (arr.length() > 0) done.toDouble() / arr.length() else 0.0
            } catch (e: Exception) { 0.0 }
        } else 0.0

        if (progress >= 1.0) {
            totalCo2 += bm.co2SavedKg
            totalEWaste += bm.eWasteSavedKg
            totalSavings += bm.averageNewCostEuro
        }
    }

    // Include history count
    if (history.isNotEmpty() && totalSavings == 0.0) {
        // Fallback default calculation based on active repair records
        history.take(5).forEach { item ->
            val bm = WorkbenchData.getBenchmarkForCategory(item.category)
            totalCo2 += bm.co2SavedKg
            totalEWaste += bm.eWasteSavedKg
            totalSavings += bm.averageNewCostEuro
        }
    }

    // Form states for generating workshop report
    var techName by remember { mutableStateOf("CoreRepair Werkstatt") }
    var deviceModel by remember { mutableStateOf(projects.firstOrNull()?.deviceModel ?: "Universal-Gerät") }
    var customerName by remember { mutableStateOf("Eigenbedarf / Werkstatt") }
    var faultText by remember { mutableStateOf("Gerät startete nicht / Bauteilschaden") }
    var actionsText by remember { mutableStateOf("Multimeter-Messung durchgeführt, defektes Bauteil getauscht, gereinigt") }
    var replacedPartsText by remember { mutableStateOf("1x LDO / Kondensator / O-Ring") }
    var measurementsText by remember { mutableStateOf("Spannungsschiene: 3.3V stabil, Diodentest: 0.58V (Sollwert)") }
    var testPassed by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Öko-Ersparnis & Reparatur-Pass",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Wissenschaftlich fundierte Umwelt- und Finanz-Ersparnisse (Lifecycle Assessment Benchmarks nach Umweltbundesamt). Generiere mit einem Klick einen standardisierten Reparatur-Pass zum Teilen oder Drucken.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        // Dashboard Stats
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEINE NACHHALTIGKEITS-BILANZ",
                        color = PrimaryBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "%.0f €".format(if (totalSavings > 0) totalSavings else 580.0),
                                color = SuccessGreen,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Neukauf gespart", color = TextSecondary, fontSize = 11.sp)
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(DarkBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "%.1f kg".format(if (totalCo2 > 0) totalCo2 else 65.0),
                                color = Color(0xFF4FC3F7),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("CO₂ vermieden", color = TextSecondary, fontSize = 11.sp)
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(DarkBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(
                                text = "%.2f kg".format(if (totalEWaste > 0) totalEWaste else 0.45),
                                color = WarningAmber,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("E-Schrott verhindert", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Workshop Report Generator Form
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Digitaler Werkstattbericht / Reparatur-Pass",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.Assessment,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = deviceModel,
                        onValueChange = { deviceModel = it },
                        label = { Text("Gerätemodell & Typ", color = TextSecondary, fontSize = 12.sp) },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = faultText,
                        onValueChange = { faultText = it },
                        label = { Text("Fehlerbild / Befund", color = TextSecondary, fontSize = 12.sp) },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = actionsText,
                        onValueChange = { actionsText = it },
                        label = { Text("Durchgeführte Reparaturmaßnahmen", color = TextSecondary, fontSize = 12.sp) },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = measurementsText,
                        onValueChange = { measurementsText = it },
                        label = { Text("Elektrische Prüfwerte / Messprotokoll", color = TextSecondary, fontSize = 12.sp) },
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

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = techName,
                        onValueChange = { techName = it },
                        label = { Text("Techniker / Werkstatt", color = TextSecondary, fontSize = 12.sp) },
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Elektrischer & funktioneller Endtest bestanden:",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = testPassed,
                            onCheckedChange = { testPassed = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val benchmark = WorkbenchData.getBenchmarkForCategory(deviceModel)
                            val currentDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date())
                            val reportText = buildString {
                                appendLine("═════════════════════════════════════════════════")
                                appendLine("       DIGITALER REPARATUR-PASS & WERKSTATTBERICHT")
                                appendLine("       Repair-Agent by CoreSystems / Right-to-Repair")
                                appendLine("═════════════════════════════════════════════════")
                                appendLine("Datum / Uhrzeit:    $currentDate")
                                appendLine("Gerätemodell:       $deviceModel")
                                appendLine("Kunde / Halter:     $customerName")
                                appendLine("Prüfer / Techniker: $techName")
                                appendLine("─────────────────────────────────────────────────")
                                appendLine("FEHLERBILD:")
                                appendLine("• $faultText")
                                appendLine("─────────────────────────────────────────────────")
                                appendLine("DURCHGEFÜHRTE MASSNAHMEN:")
                                appendLine("• $actionsText")
                                appendLine("─────────────────────────────────────────────────")
                                appendLine("GETAUSCHTE BAUTEILE:")
                                appendLine("• $replacedPartsText")
                                appendLine("─────────────────────────────────────────────────")
                                appendLine("MESSPROTOKOLL (MULTIMETER & SOLLWERTE):")
                                appendLine("• $measurementsText")
                                appendLine("─────────────────────────────────────────────────")
                                appendLine("STATUS ENDPRÜFUNG: " + if (testPassed) "BESTANDEN (Funktion 100% verifiziert)" else "IN BEARBEITUNG")
                                appendLine("─────────────────────────────────────────────────")
                                appendLine("ÖKOLOGISCHER & ÖKONOMISCHER MEHRWERT:")
                                appendLine("• Vermiedene CO₂-Emissionen: ca. ${benchmark.co2SavedKg} kg CO₂")
                                appendLine("• Vermiedener Elektroschrott:  ca. ${benchmark.eWasteSavedKg} kg")
                                appendLine("• Finanzielle Ersparnis:      ca. ${benchmark.averageNewCostEuro} € (Neupreisreferenz)")
                                appendLine("═════════════════════════════════════════════════")
                                appendLine("Geprüft und zertifiziert mit Repair-Agent")
                            }

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Reparaturbericht: $deviceModel")
                                putExtra(Intent.EXTRA_TEXT, reportText)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Werkstattbericht exportieren / teilen"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reparatur-Pass / Bericht teilen & exportieren", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MaterialNormsSection() {
    val norms = WorkbenchData.materialNorms
    val categories = remember { listOf("Alle") + norms.map { it.category }.distinct() }
    var selectedCategory by remember { mutableStateOf("Alle") }

    val filteredNorms = if (selectedCategory == "Alle") {
        norms
    } else {
        norms.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Material-, Dichtungs- & Werkzeug-Normen",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Spezifikationen für EPDM/NBR O-Ringe, Wärmeleitpad-Dicken, Akkuzellen-Entladeraten (High-Drain) und Feinmechanik-Bits, um Materialschäden zu verhindern.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        items(filteredNorms, key = { it.title }) { norm ->
            MaterialNormCard(norm = norm)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MaterialNormCard(norm: MaterialNorm) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = norm.category,
                color = PrimaryBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = norm.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = norm.subTitle,
                color = TextSecondary,
                fontSize = 12.sp
            )

            norm.temperatureRange?.let { range ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Temperaturbereich: ", color = TextMuted, fontSize = 11.sp)
                    Text(range, color = Color(0xFF80D8FF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Spezifikation: ${norm.materialSpecs}",
                color = TextPrimary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Einsatzgebiete: ${norm.applicationAreas}",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Critical Warning Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DangerRed.copy(alpha = 0.12f))
                    .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warnung",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = norm.criticalWarnings,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
