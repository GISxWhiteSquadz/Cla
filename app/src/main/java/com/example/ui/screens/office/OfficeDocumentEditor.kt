package com.example.ui.screens.office

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfficeDocumentEntity
import com.example.model.OfficeLineItem
import com.example.model.OfficeTemplates
import com.example.ui.RepairViewModel
import com.example.ui.theme.*
import com.example.ui.util.DocumentExportHelper
import com.example.ui.util.bounceClick
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeDocumentEditor(
    viewModel: RepairViewModel,
    modifier: Modifier = Modifier
) {
    val activeDoc by viewModel.editingDocument.collectAsState()
    val context = LocalContext.current

    // Local form state
    var title by remember(activeDoc) { mutableStateOf(activeDoc?.title ?: "Neues Dokument") }
    var docType by remember(activeDoc) { mutableStateOf(activeDoc?.docType ?: "Angebot") }
    var status by remember(activeDoc) { mutableStateOf(activeDoc?.status ?: "Entwurf") }
    var customerName by remember(activeDoc) { mutableStateOf(activeDoc?.customerName ?: "") }
    var customerContact by remember(activeDoc) { mutableStateOf(activeDoc?.customerContact ?: "") }
    var deviceOrProject by remember(activeDoc) { mutableStateOf(activeDoc?.deviceOrProject ?: "") }
    var content by remember(activeDoc) { mutableStateOf(activeDoc?.content ?: "") }
    var taxRate by remember(activeDoc) { mutableStateOf(activeDoc?.taxRatePercent ?: 19.0) }
    var budgetTargetStr by remember(activeDoc) {
        mutableStateOf(if ((activeDoc?.budgetTargetEuro ?: 0.0) > 0) activeDoc!!.budgetTargetEuro.toString() else "")
    }

    // Line items state
    var lineItems by remember(activeDoc) {
        mutableStateOf(DocumentExportHelper.parseLineItems(activeDoc?.lineItemsJson ?: "[]"))
    }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    // Calculated totals
    val subtotal = remember(lineItems) { lineItems.sumOf { it.total } }
    val total = remember(subtotal, taxRate) { subtotal * (1.0 + taxRate / 100.0) }
    val budgetTarget = budgetTargetStr.replace(",", ".").toDoubleOrNull() ?: 0.0

    // Quick presets for workshop tasks
    val quickServicePresets = listOf(
        OfficeLineItem("Diagnose & Fehlersuche", "SRV-DIAG", 1, 29.00, true),
        OfficeLineItem("Arbeitszeit Werkstatt (1 Std)", "SRV-LABOR", 1, 69.00, true),
        OfficeLineItem("Wärmeleitpaste & Cleaner", "MAT-PASTE", 1, 9.90, false),
        OfficeLineItem("VDE 0701 Sicherheitsprüfung", "SRV-VDE", 1, 24.50, true),
        OfficeLineItem("Displaymontage & Klebedichtung", "SRV-DISP", 1, 45.00, true),
        OfficeLineItem("Ultraschallbad Reinigung", "SRV-CLEAN", 1, 19.00, true)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Editor Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { viewModel.selectOfficeSubTab(1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = TextPrimary)
                        }

                        Column {
                            Text(
                                text = if (activeDoc != null) "${activeDoc!!.documentNumber}: $title" else "Dokument-Editor",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$docType • ${lineItems.size} Positionen • ${String.format(Locale.GERMANY, "%.2f €", total)}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                val itemsJson = DocumentExportHelper.serializeLineItems(lineItems)
                                val folder = when (docType) {
                                    "Angebot" -> "Angebote & Kostenvoranschläge"
                                    "Rechnung" -> "Rechnungen"
                                    "Auftrag" -> "Reparatur- & PC-Aufträge"
                                    "PC-Bau Plan" -> "PC-Konfigurationen & Budget"
                                    "Freies Dokument" -> "Freie Dokumente & Notizen"
                                    else -> "Protokolle & Berichte"
                                }
                                val docForPdf = (activeDoc ?: OfficeDocumentEntity(
                                    documentNumber = "DOC-2026-${(100..999).random()}",
                                    title = title.ifBlank { "Unbenanntes Dokument" },
                                    docType = docType,
                                    categoryFolder = folder,
                                    customerName = customerName,
                                    customerContact = customerContact,
                                    deviceOrProject = deviceOrProject,
                                    status = status,
                                    content = content,
                                    lineItemsJson = itemsJson,
                                    subtotalEuro = subtotal,
                                    taxRatePercent = taxRate,
                                    totalEuro = total,
                                    budgetTargetEuro = budgetTarget,
                                    fileSizeBytes = (content.length * 2 + itemsJson.length).toLong()
                                )).copy(
                                    title = title.ifBlank { "Unbenanntes Dokument" },
                                    docType = docType,
                                    categoryFolder = folder,
                                    customerName = customerName,
                                    customerContact = customerContact,
                                    deviceOrProject = deviceOrProject,
                                    status = status,
                                    content = content,
                                    lineItemsJson = itemsJson,
                                    subtotalEuro = subtotal,
                                    taxRatePercent = taxRate,
                                    totalEuro = total,
                                    budgetTargetEuro = budgetTarget
                                )
                                DocumentExportHelper.printDocumentAsPdf(context, docForPdf)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "PDF Drucken / Speichern", tint = AccentCyan)
                        }

                        IconButton(
                            onClick = {
                                activeDoc?.let { DocumentExportHelper.shareDocument(context, it) }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Teilen", tint = TextSecondary)
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryBlue,
                            modifier = Modifier
                                .bounceClick {
                                    val itemsJson = DocumentExportHelper.serializeLineItems(lineItems)
                                    val folder = when (docType) {
                                        "Angebot" -> "Angebote & Kostenvoranschläge"
                                        "Rechnung" -> "Rechnungen"
                                        "Auftrag" -> "Reparatur- & PC-Aufträge"
                                        "PC-Bau Plan" -> "PC-Konfigurationen & Budget"
                                        "Freies Dokument" -> "Freie Dokumente & Notizen"
                                        else -> "Protokolle & Berichte"
                                    }
                                    val docToSave = (activeDoc ?: OfficeDocumentEntity(
                                        documentNumber = "DOC-2026-${(100..999).random()}",
                                        title = title,
                                        docType = docType,
                                        categoryFolder = folder,
                                        customerName = customerName,
                                        customerContact = customerContact,
                                        deviceOrProject = deviceOrProject,
                                        status = status,
                                        content = content,
                                        lineItemsJson = itemsJson,
                                        subtotalEuro = subtotal,
                                        taxRatePercent = taxRate,
                                        totalEuro = total,
                                        budgetTargetEuro = budgetTarget,
                                        fileSizeBytes = (content.length * 2 + itemsJson.length).toLong()
                                    )).copy(
                                        title = title,
                                        docType = docType,
                                        categoryFolder = folder,
                                        customerName = customerName,
                                        customerContact = customerContact,
                                        deviceOrProject = deviceOrProject,
                                        status = status,
                                        content = content,
                                        lineItemsJson = itemsJson,
                                        subtotalEuro = subtotal,
                                        taxRatePercent = taxRate,
                                        totalEuro = total,
                                        budgetTargetEuro = budgetTarget,
                                        updatedTimestamp = System.currentTimeMillis()
                                    )
                                    viewModel.saveOfficeDocument(docToSave)
                                    showSavedSnackbar = true
                                }
                                .testTag("save_document_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Speichern", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Editor Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
            ) {
                // Document Type and Status row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var typeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = !typeExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = docType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Dokumenttyp", color = TextSecondary, fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false },
                                modifier = Modifier.background(DarkSurfaceElevated)
                            ) {
                                listOf("Angebot", "Rechnung", "Auftrag", "Reparaturbericht", "Prüfprotokoll", "PC-Bau Plan", "Freies Dokument").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            docType = type
                                            if (content.isBlank()) {
                                                content = OfficeTemplates.getTemplateFor(type, customerName, deviceOrProject)
                                            }
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var statusExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = !statusExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Status", color = TextSecondary, fontSize = 11.sp) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                            )
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false },
                                modifier = Modifier.background(DarkSurfaceElevated)
                            ) {
                                listOf("Entwurf", "Offen / Versendet", "In Bearbeitung", "Bezahlt", "Abgeschlossen").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s, color = TextPrimary, fontSize = 12.sp) },
                                        onClick = {
                                            status = s
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Title & Device
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Dokumententitel / Gegenstand", color = TextSecondary, fontSize = 12.sp) },
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

                // Customer and Project details
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Kunde / Firma", color = TextSecondary, fontSize = 11.sp) },
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
                        OutlinedTextField(
                            value = deviceOrProject,
                            onValueChange = { deviceOrProject = it },
                            label = { Text("Gerät / Projekt", color = TextSecondary, fontSize = 11.sp) },
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
                    }
                }

                // Contact & Target Budget
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerContact,
                            onValueChange = { customerContact = it },
                            label = { Text("Kontakt (Tel / E-Mail)", color = TextSecondary, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1.2f)
                        )
                        OutlinedTextField(
                            value = budgetTargetStr,
                            onValueChange = { budgetTargetStr = it },
                            label = { Text("Ziel-Budget (€)", color = TextSecondary, fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                }

                // Quick Service Insertion Presets Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Schnellbausteine für Werkstatt & Teile:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(quickServicePresets) { preset ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                    modifier = Modifier.bounceClick {
                                        lineItems = lineItems + preset
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (preset.isLabor) Icons.Default.Build else Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = if (preset.isLabor) WarningAmber else AccentCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "${preset.description} (${String.format(Locale.GERMANY, "%.2f €", preset.unitPrice)})",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Line Items Header & Add Button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Positionen (Teile & Arbeitslohn)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = CircleShape,
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                            ) {
                                Text(
                                    text = "${lineItems.size}",
                                    color = AccentCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlueVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                            modifier = Modifier.bounceClick { showAddItemDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("Position +", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Line Items List
                if (lineItems.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = DarkSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Text(
                                text = "Noch keine Positionen erfasst. Nutze die Schnellbausteine oben oder klicke auf 'Position +', um Ersatzteile oder Arbeitszeit hinzuzufügen.",
                                modifier = Modifier.padding(14.dp),
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    itemsIndexed(lineItems) { index, item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = DarkSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = if (item.isLabor) WarningAmber.copy(alpha = 0.15f) else AccentCyan.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (item.isLabor) WarningAmber.copy(alpha = 0.4f) else AccentCyan.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Text(
                                                text = if (item.isLabor) "ARBEIT" else "TEIL",
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isLabor) WarningAmber else AccentCyan
                                            )
                                        }
                                        Text(
                                            text = item.description,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (item.partNumber.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Art.-Nr: ${item.partNumber}",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.quantity}x @ ${String.format(Locale.GERMANY, "%.2f €", item.unitPrice)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = String.format(Locale.GERMANY, "%.2f €", item.total),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                    IconButton(
                                        onClick = {
                                            lineItems = lineItems.toMutableList().also { it.removeAt(index) }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Löschen", tint = DangerRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Calculation Summary Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Zwischensumme (Netto):", color = TextSecondary, fontSize = 12.sp)
                                Text(String.format(Locale.GERMANY, "%.2f €", subtotal), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Tax selection chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("MwSt.-Satz:", color = TextSecondary, fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(19.0 to "19%", 7.0 to "7%", 0.0 to "0%").forEach { (rate, label) ->
                                        val isSel = taxRate == rate
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSel) PrimaryBlue else DarkSurfaceElevated,
                                            modifier = Modifier.bounceClick { taxRate = rate }
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) Color.White else TextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Umsatzsteuer (${taxRate.toInt()}%):", color = TextSecondary, fontSize = 12.sp)
                                Text(String.format(Locale.GERMANY, "%.2f €", subtotal * (taxRate / 100.0)), color = TextPrimary, fontSize = 12.sp)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DarkBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("GESAMTBETRAG (Brutto):", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                Text(
                                    text = String.format(Locale.GERMANY, "%.2f €", total),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryBlue
                                )
                            }

                            // Budget Check Indicator
                            if (budgetTarget > 0) {
                                val diff = budgetTarget - total
                                val isWithinBudget = diff >= 0
                                val progress = (total / budgetTarget).toFloat().coerceIn(0f, 1f)

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ziel-Budget: ${String.format(Locale.GERMANY, "%.2f €", budgetTarget)}", color = TextMuted, fontSize = 11.sp)
                                    Text(
                                        text = if (isWithinBudget) "Im Budget (+${String.format(Locale.GERMANY, "%.2f €", diff)})"
                                        else "Überschritten (${String.format(Locale.GERMANY, "%.2f €", diff)})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWithinBudget) SuccessGreen else DangerRed
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(DarkSurfaceElevated)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress)
                                            .fillMaxHeight()
                                            .background(if (isWithinBudget) SuccessGreen else DangerRed)
                                    )
                                }
                            }
                        }
                    }
                }

                // Document Text / Markdown Editor
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dokumententext & Diagnose-Befund",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = {
                                    content = OfficeTemplates.getTemplateFor(docType, customerName, deviceOrProject)
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentCyan)
                                Spacer(Modifier.width(4.dp))
                                Text("Standard-Vorlage laden", color = AccentCyan, fontSize = 11.sp)
                            }
                        }

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 360.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            placeholder = { Text("Hier den Text des Berichts, Kostenvoranschlags oder der Rechnung eingeben...", color = TextMuted, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Save confirmation toast snackbar
        if (showSavedSnackbar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Text("Dokument erfolgreich im Archiv gesichert!", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        TextButton(onClick = { showSavedSnackbar = false }) {
                            Text("OK", color = AccentCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        ModernAddLineItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAdd = { newItem ->
                lineItems = lineItems + newItem
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun ModernAddLineItemDialog(
    onDismiss: () -> Unit,
    onAdd: (OfficeLineItem) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("1") }
    var unitPriceStr by remember { mutableStateOf("") }
    var isLabor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                Text("Position erfassen", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isLabor) PrimaryBlueVariant.copy(alpha = 0.35f) else DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (!isLabor) PrimaryBlue else DarkBorder),
                        modifier = Modifier.weight(1f).bounceClick { isLabor = false }
                    ) {
                        Text(
                            text = "Teil / Hardware",
                            color = if (!isLabor) AccentCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLabor) PrimaryBlueVariant.copy(alpha = 0.35f) else DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLabor) PrimaryBlue else DarkBorder),
                        modifier = Modifier.weight(1f).bounceClick { isLabor = true }
                    ) {
                        Text(
                            text = "Arbeitszeit / Service",
                            color = if (isLabor) AccentCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Bezeichnung *", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text(if (isLabor) "z.B. Displaytausch & Dichtungsprüfung" else "z.B. Kingston Fury 32GB DDR5-6000", color = TextMuted, fontSize = 12.sp) },
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
                    value = partNumber,
                    onValueChange = { partNumber = it },
                    label = { Text("Teile- / Artikelnummer (optional)", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("z.B. OEM-820-00165", color = TextMuted, fontSize = 12.sp) },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Menge", color = TextSecondary, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
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
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("Einzelpreis (€)", color = TextSecondary, fontSize = 12.sp) },
                        placeholder = { Text("49.00", color = TextMuted, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        val qty = quantityStr.toIntOrNull() ?: 1
                        val price = unitPriceStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                        onAdd(
                            OfficeLineItem(
                                description = description,
                                partNumber = partNumber,
                                quantity = qty,
                                unitPrice = price,
                                isLabor = isLabor
                            )
                        )
                    }
                },
                enabled = description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Hinzufügen", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = TextSecondary)
            }
        }
    )
}
