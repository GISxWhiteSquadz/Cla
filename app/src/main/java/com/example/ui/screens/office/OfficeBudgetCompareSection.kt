package com.example.ui.screens.office

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PartComparisonEntity
import com.example.ui.RepairViewModel
import com.example.ui.theme.*
import com.example.ui.util.bounceClick
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeBudgetCompareSection(
    viewModel: RepairViewModel,
    modifier: Modifier = Modifier
) {
    val comparisons by viewModel.partComparisons.collectAsState()
    val documents by viewModel.officeDocuments.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Aggregate budget calculations from PC-Bau plans and quotes
    val pcPlans = remember(documents) {
        documents.filter { it.docType == "PC-Bau Plan" || it.budgetTargetEuro > 0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header & Quick Action Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Budget & Teile-Vergleich",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Komponenten gegenüberstellen & Kostengrenzen prüfen",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueVariant),
                modifier = Modifier
                    .bounceClick { showCreateDialog = true }
                    .testTag("new_compare_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("Vergleich +", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 90.dp)
        ) {
            // Active Budget Calculations Section
            if (pcPlans.isNotEmpty()) {
                item {
                    Text(
                        text = "Aktive Projekt-Budgets & Kostenziele",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(pcPlans, key = { "plan_${it.id}" }) { plan ->
                    ModernBudgetPlanProgressCard(
                        title = plan.title,
                        device = plan.deviceOrProject,
                        spentEuro = plan.totalEuro,
                        targetBudgetEuro = if (plan.budgetTargetEuro > 0) plan.budgetTargetEuro else plan.totalEuro * 1.1,
                        onOpenPlan = {
                            viewModel.setEditingDocument(plan)
                            viewModel.selectOfficeSubTab(2) // Jump to Editor
                        }
                    )
                }
            }

            // Part Comparisons Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gespeicherte Teile-Vergleiche",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = CircleShape,
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = "${comparisons.size}",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            if (comparisons.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Balance, contentDescription = null, modifier = Modifier.size(24.dp), tint = TextMuted)
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Noch keine Teile-Vergleiche angelegt",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Vergleiche zwei Grafikkarten, SSDs oder Ersatzteile mit Vor- und Nachteilen.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                                modifier = Modifier.bounceClick { showCreateDialog = true }
                            ) {
                                Text(
                                    text = "Beispiel-Vergleich erstellen",
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                items(comparisons, key = { it.id }) { comp ->
                    ModernPartComparisonCard(
                        comp = comp,
                        onDelete = { viewModel.deletePartComparison(comp.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        ModernCreatePartComparisonDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, category, budget, aName, aSpecs, aPrice, aPros, bName, bSpecs, bPrice, bPros, rec ->
                viewModel.createPartComparison(
                    title = title,
                    category = category,
                    targetBudget = budget,
                    partAName = aName,
                    partASpecs = aSpecs,
                    partAPrice = aPrice,
                    partAPros = aPros,
                    partBName = bName,
                    partBSpecs = bSpecs,
                    partBPrice = bPrice,
                    partBPros = bPros,
                    recommendation = rec
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ModernBudgetPlanProgressCard(
    title: String,
    device: String,
    spentEuro: Double,
    targetBudgetEuro: Double,
    onOpenPlan: () -> Unit
) {
    val progress = if (targetBudgetEuro > 0) (spentEuro / targetBudgetEuro).toFloat().coerceIn(0f, 1f) else 0f
    val isOverBudget = spentEuro > targetBudgetEuro && targetBudgetEuro > 0
    val diff = targetBudgetEuro - spentEuro

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (device.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = device,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.bounceClick { onOpenPlan() }
                ) {
                    Text(
                        text = "Editor öffnen",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Animated Linear Progress Bar
            val animatedProgress by animateFloatAsState(targetValue = progress, label = "budget_progress")
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
                        .background(if (isOverBudget) DangerRed else PrimaryBlue)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bisher: ${String.format(Locale.GERMANY, "%.2f €", spentEuro)}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Ziel: ${String.format(Locale.GERMANY, "%.2f €", targetBudgetEuro)}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = if (isOverBudget) "Über: +${String.format(Locale.GERMANY, "%.2f €", -diff)}"
                    else "Frei: ${String.format(Locale.GERMANY, "%.2f €", diff)}",
                    color = if (isOverBudget) DangerRed else SuccessGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ModernPartComparisonCard(
    comp: PartComparisonEntity,
    onDelete: () -> Unit
) {
    val priceDiff = comp.partAPriceEuro - comp.partBPriceEuro
    val diffText = if (priceDiff > 0) {
        "${comp.partBName} ist ${String.format(Locale.GERMANY, "%.2f €", priceDiff)} günstiger"
    } else if (priceDiff < 0) {
        "${comp.partAName} ist ${String.format(Locale.GERMANY, "%.2f €", -priceDiff)} günstiger"
    } else {
        "Beide Varianten preisgleich"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category Badge, Title, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = PrimaryBlueVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = comp.category,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }
                    Text(
                        text = comp.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Löschen", tint = DangerRed, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Two Column Comparison Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option A Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("OPTION A", fontSize = 9.sp, color = AccentCyan, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(comp.partAName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = String.format(Locale.GERMANY, "%.2f €", comp.partAPriceEuro),
                            color = AccentCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (comp.partASpecs.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(comp.partASpecs, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                        if (comp.partAPros.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("+ ${comp.partAPros}", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Option B Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("OPTION B", fontSize = 9.sp, color = PrimaryBlue, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(comp.partBName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = String.format(Locale.GERMANY, "%.2f €", comp.partBPriceEuro),
                            color = PrimaryBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (comp.partBSpecs.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(comp.partBSpecs, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                        if (comp.partBPros.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("+ ${comp.partBPros}", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Price difference pill
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.PriceCheck, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                    Text(diffText, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Recommendation Footer
            if (comp.recommendation.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                        Column {
                            Text(
                                "Fazit & Werkstatt-Empfehlung:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                comp.recommendation,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernCreatePartComparisonDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        category: String,
        budget: Double,
        aName: String,
        aSpecs: String,
        aPrice: Double,
        aPros: String,
        bName: String,
        bSpecs: String,
        bPrice: Double,
        bPros: String,
        recommendation: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Hardware & PC-Bau") }
    var budgetStr by remember { mutableStateOf("") }

    var aName by remember { mutableStateOf("") }
    var aSpecs by remember { mutableStateOf("") }
    var aPriceStr by remember { mutableStateOf("") }
    var aPros by remember { mutableStateOf("") }

    var bName by remember { mutableStateOf("") }
    var bSpecs by remember { mutableStateOf("") }
    var bPriceStr by remember { mutableStateOf("") }
    var bPros by remember { mutableStateOf("") }

    var rec by remember { mutableStateOf("") }

    // Quick presets to fill
    val presets = listOf(
        Triple("RTX 4070 Super vs RX 7800 XT", "GeForce RTX 4070 Super", "Radeon RX 7800 XT"),
        Triple("Samsung 990 Pro vs Crucial T500", "Samsung 990 Pro 2TB", "Crucial T500 2TB"),
        Triple("Ryzen 7 7800X3D vs i7-14700K", "AMD Ryzen 7 7800X3D", "Intel Core i7-14700K")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Balance, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                Text("Neuen Teile-Vergleich erstellen", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Schnellvorlagen:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presets) { (presetTitle, pA, pB) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.bounceClick {
                                    title = presetTitle
                                    aName = pA
                                    bName = pB
                                    if (pA.contains("RTX")) {
                                        aPriceStr = "629.00"
                                        aSpecs = "12GB GDDR6X, DLSS 3.5, 220W TDP"
                                        aPros = "Beste Effizienz & Raytracing"
                                        bPriceStr = "539.00"
                                        bSpecs = "16GB GDDR6, FSR 3, 263W TDP"
                                        bPros = "Mehr VRAM & günstigerer Preis"
                                        rec = "Für 1440p mit Raytracing RTX 4070 Super wählen, für reines Rasterizing & VRAM-Zukunftssicherheit die RX 7800 XT."
                                    }
                                }
                            ) {
                                Text(
                                    text = presetTitle,
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Titel des Vergleichs *", color = TextSecondary, fontSize = 12.sp) },
                        placeholder = { Text("z.B. RTX 4070 Super vs. RX 7800 XT", color = TextMuted, fontSize = 12.sp) },
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
                }

                // Option A Fields
                item {
                    Text("Option A (Variante 1):", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = aName,
                        onValueChange = { aName = it },
                        label = { Text("Name Komponente A *", color = TextSecondary, fontSize = 11.sp) },
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

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = aPriceStr,
                            onValueChange = { aPriceStr = it },
                            label = { Text("Preis (€)", color = TextSecondary, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = aPros,
                            onValueChange = { aPros = it },
                            label = { Text("Vorteile", color = TextSecondary, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Option B Fields
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Option B (Variante 2):", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text("Name Komponente B *", color = TextSecondary, fontSize = 11.sp) },
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

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bPriceStr,
                            onValueChange = { bPriceStr = it },
                            label = { Text("Preis (€)", color = TextSecondary, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bPros,
                            onValueChange = { bPros = it },
                            label = { Text("Vorteile", color = TextSecondary, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Optional Budget Limit
                item {
                    OutlinedTextField(
                        value = budgetStr,
                        onValueChange = { budgetStr = it },
                        label = { Text("Maximales Budget / Limit (€) (Optional)", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("z.B. 600", color = TextMuted, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                // Live latency-free Price & Budget Analysis Card
                item {
                    val pA = aPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val pB = bPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val bud = budgetStr.replace(",", ".").toDoubleOrNull() ?: 0.0

                    if (pA > 0 || pB > 0) {
                        val diff = pA - pB
                        val maxP = maxOf(pA, pB)
                        val percent = if (maxP > 0) (kotlin.math.abs(diff) / maxP) * 100.0 else 0.0

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (diff != 0.0) AccentCyan.copy(alpha = 0.5f) else DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                                        Text("Live-Preisdifferenz & Analyse", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (pA > 0 && pB > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (diff > 0) SuccessGreen.copy(alpha = 0.2f) else if (diff < 0) PrimaryBlue.copy(alpha = 0.2f) else DarkSurfaceElevated
                                        ) {
                                            Text(
                                                text = if (diff > 0) "-${String.format(Locale.GERMANY, "%.1f%%", percent)}"
                                                else if (diff < 0) "-${String.format(Locale.GERMANY, "%.1f%%", percent)}"
                                                else "0 %",
                                                color = if (diff != 0.0) AccentCyan else TextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                if (pA > 0 && pB > 0) {
                                    Text(
                                        text = if (diff > 0) {
                                            "Option B (${bName.ifBlank { "Variante 2" }}) ist ${String.format(Locale.GERMANY, "%.2f €", diff)} günstiger als Option A."
                                        } else if (diff < 0) {
                                            "Option A (${aName.ifBlank { "Variante 1" }}) ist ${String.format(Locale.GERMANY, "%.2f €", -diff)} günstiger als Option B."
                                        } else {
                                            "Beide Optionen sind exakt preisgleich (${String.format(Locale.GERMANY, "%.2f €", pA)})."
                                        },
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        text = "Gib für beide Optionen einen Preis ein, um die Live-Differenz zu sehen.",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                if (bud > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (pA > 0) {
                                            val diffA = bud - pA
                                            Text(
                                                text = "A: " + if (diffA >= 0) "Im Budget (+${String.format(Locale.GERMANY, "%.2f €", diffA)})" else "Über Budget (-${String.format(Locale.GERMANY, "%.2f €", -diffA)})",
                                                color = if (diffA >= 0) SuccessGreen else DangerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (pB > 0) {
                                            val diffB = bud - pB
                                            Text(
                                                text = "B: " + if (diffB >= 0) "Im Budget (+${String.format(Locale.GERMANY, "%.2f €", diffB)})" else "Über Budget (-${String.format(Locale.GERMANY, "%.2f €", -diffB)})",
                                                color = if (diffB >= 0) SuccessGreen else DangerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = rec,
                        onValueChange = { rec = it },
                        label = { Text("Empfehlung / Fazit", color = TextSecondary, fontSize = 11.sp) },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && aName.isNotBlank() && bName.isNotBlank()) {
                        val pA = aPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val pB = bPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val bud = budgetStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onConfirm(title, category, bud, aName, aSpecs, pA, aPros, bName, bSpecs, pB, bPros, rec)
                    }
                },
                enabled = title.isNotBlank() && aName.isNotBlank() && bName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Vergleich speichern", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = TextSecondary)
            }
        }
    )
}
