package com.example.ui.screens.office

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfficeDocumentEntity
import com.example.model.OfficeTemplates
import com.example.ui.RepairViewModel
import com.example.ui.theme.*
import com.example.ui.util.DocumentExportHelper
import com.example.ui.util.bounceClick
import com.example.ui.util.rememberPulseAlpha
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeOrdersSection(
    viewModel: RepairViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.officeDocuments.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("Alle") }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var createType by remember { mutableStateOf("Angebot") }

    val filterOptions = listOf("Alle", "Angebot", "Rechnung", "Auftrag", "PC-Bau Plan")

    val filteredDocs = remember(documents, selectedFilter, searchQuery) {
        documents.filter { doc ->
            val matchesFilter = if (selectedFilter == "Alle") true else doc.docType.equals(selectedFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    doc.title.contains(searchQuery, ignoreCase = true) ||
                    doc.documentNumber.contains(searchQuery, ignoreCase = true) ||
                    doc.customerName.contains(searchQuery, ignoreCase = true) ||
                    doc.deviceOrProject.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    val totalRevenue = remember(documents) {
        documents.filter { it.docType == "Rechnung" && it.status == "Bezahlt" }.sumOf { it.totalEuro }
    }
    val openInvoices = remember(documents) {
        documents.filter { it.docType == "Rechnung" && it.status != "Bezahlt" }.sumOf { it.totalEuro }
    }
    val openOffers = remember(documents) {
        documents.filter { it.docType == "Angebot" }.sumOf { it.totalEuro }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // High-Tech Financial Overview KPI Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SuccessGreen))
                        Text(
                            text = "BEZAHLT",
                            fontSize = 10.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.GERMANY, "%.2f €", totalRevenue),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(DarkBorder)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(DangerRed))
                        Text(
                            text = "OFFEN",
                            fontSize = 10.sp,
                            color = DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.GERMANY, "%.2f €", openInvoices),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DangerRed
                    )
                }

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(DarkBorder)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentCyan))
                        Text(
                            text = "ANGEBOTE",
                            fontSize = 10.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.GERMANY, "%.2f €", openOffers),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Action Buttons Row: Angebot, Rechnung, Auftrag
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue,
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueVariant),
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        createType = "Angebot"
                        showCreateDialog = true
                    }
                    .testTag("new_offer_btn")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Angebot", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        createType = "Rechnung"
                        showCreateDialog = true
                    }
                    .testTag("new_invoice_btn")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Rechnung", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        createType = "Auftrag"
                        showCreateDialog = true
                    }
                    .testTag("new_order_btn")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Auftrag", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Nummer, Kunde, Gerät oder Titel durchsuchen...", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )

        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filterOptions) { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) PrimaryBlueVariant.copy(alpha = 0.35f) else DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBlue else DarkBorder),
                    modifier = Modifier.bounceClick { selectedFilter = filter }
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) AccentCyan else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Documents List
        if (filteredDocs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = TextMuted
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Keine Dokumente für '$selectedFilter' gefunden",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Erstelle jetzt ein Angebot, eine Rechnung oder einen Auftrag.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryBlue,
                        modifier = Modifier.bounceClick {
                            createType = if (selectedFilter == "Alle") "Angebot" else selectedFilter
                            showCreateDialog = true
                        }
                    ) {
                        Text(
                            text = "Jetzt $createType erstellen",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredDocs, key = { it.id }) { doc ->
                    ModernDocumentOrderCard(
                        doc = doc,
                        onEdit = {
                            viewModel.setEditingDocument(doc)
                            viewModel.selectOfficeSubTab(2) // Jump to Editor
                        },
                        onShare = {
                            DocumentExportHelper.shareDocument(context, doc)
                        },
                        onConvertToInvoice = {
                            viewModel.convertOfferToInvoice(doc)
                        },
                        onToggleStatus = { newStatus ->
                            viewModel.saveOfficeDocument(doc.copy(status = newStatus))
                        },
                        onDelete = {
                            viewModel.deleteOfficeDocument(doc.id)
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDocumentQuickDialog(
            defaultType = createType,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, docType, customer, contact, device ->
                val template = OfficeTemplates.getTemplateFor(docType, customer, device)
                val folder = when (docType) {
                    "Angebot" -> "Angebote & Kostenvoranschläge"
                    "Rechnung" -> "Rechnungen"
                    "Auftrag" -> "Reparatur- & PC-Aufträge"
                    "PC-Bau Plan" -> "PC-Konfigurationen & Budget"
                    else -> "Protokolle & Berichte"
                }
                viewModel.createOfficeDocument(
                    title = title,
                    docType = docType,
                    categoryFolder = folder,
                    customerName = customer,
                    customerContact = contact,
                    deviceOrProject = device,
                    content = template,
                    lineItemsJson = "[]",
                    subtotal = 0.0,
                    taxRate = 19.0,
                    total = 0.0,
                    budgetTarget = 0.0
                )
                showCreateDialog = false
                viewModel.selectOfficeSubTab(2) // Navigate directly to editor
            }
        )
    }
}

@Composable
fun ModernDocumentOrderCard(
    doc: OfficeDocumentEntity,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onConvertToInvoice: () -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(doc.createdTimestamp) {
        SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(doc.createdTimestamp))
    }

    val typeColor = when (doc.docType) {
        "Angebot" -> PrimaryBlue
        "Rechnung" -> SuccessGreen
        "Auftrag" -> WarningAmber
        "PC-Bau Plan" -> AccentCyan
        else -> TextSecondary
    }

    val pulseAlpha = rememberPulseAlpha()

    val initials = remember(doc.customerName) {
        if (doc.customerName.isBlank()) "KD"
        else doc.customerName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onEdit() },
        shape = RoundedCornerShape(14.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type Badge, Number, Date, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = typeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = doc.docType.uppercase(Locale.GERMANY),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                    Text(
                        text = doc.documentNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                // Status Pill with optional pulsating dot
                Surface(
                    color = when (doc.status) {
                        "Bezahlt", "Abgeschlossen" -> SuccessGreen.copy(alpha = 0.15f)
                        "In Bearbeitung" -> WarningAmber.copy(alpha = 0.15f)
                        "Offen / Versendet" -> DangerRed.copy(alpha = 0.15f)
                        else -> DarkSurfaceElevated
                    },
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (doc.status) {
                            "Bezahlt", "Abgeschlossen" -> SuccessGreen.copy(alpha = 0.4f)
                            "In Bearbeitung" -> WarningAmber.copy(alpha = 0.4f)
                            "Offen / Versendet" -> DangerRed.copy(alpha = 0.4f)
                            else -> DarkBorder
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (doc.status == "In Bearbeitung" || doc.status == "Offen / Versendet") {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (doc.status == "In Bearbeitung") WarningAmber.copy(alpha = pulseAlpha)
                                        else DangerRed.copy(alpha = pulseAlpha)
                                    )
                            )
                        }
                        Text(
                            text = doc.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (doc.status) {
                                "Bezahlt", "Abgeschlossen" -> SuccessGreen
                                "In Bearbeitung" -> WarningAmber
                                "Offen / Versendet" -> DangerRed
                                else -> TextSecondary
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Main Title & Customer monogram avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (doc.customerName.isNotBlank()) "${doc.customerName} • $dateStr" else dateStr,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (doc.deviceOrProject.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = TextMuted
                    )
                    Text(
                        text = doc.deviceOrProject,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = DarkBorder
            )

            // Bottom row: Amount & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (doc.docType == "PC-Bau Plan") "Gesamtkosten" else "Gesamtbetrag",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(Locale.GERMANY, "%.2f €", doc.totalEuro),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (doc.status == "Bezahlt") SuccessGreen else PrimaryBlue
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (doc.docType == "Angebot") {
                        IconButton(
                            onClick = onConvertToInvoice,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "In Rechnung wandeln",
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (doc.docType == "Rechnung" && doc.status != "Bezahlt") {
                        IconButton(
                            onClick = { onToggleStatus("Bezahlt") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Als Bezahlt buchen",
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Exportieren", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Löschen", tint = DangerRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateDocumentQuickDialog(
    defaultType: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, docType: String, customer: String, contact: String, device: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(defaultType) }
    var customer by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var device by remember { mutableStateOf("") }

    val docTypes = listOf("Angebot", "Rechnung", "Auftrag", "Reparaturbericht", "PC-Bau Plan")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(22.dp))
                Text("Neues Dokument erstellen", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Dokumenttyp:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(docTypes) { type ->
                        val isSelected = selectedType == type
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryBlueVariant.copy(alpha = 0.35f) else DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBlue else DarkBorder),
                            modifier = Modifier.bounceClick { selectedType = type }
                        ) {
                            Text(
                                text = type,
                                color = if (isSelected) AccentCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bezeichnung / Betreff *", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("z.B. Reparatur & Wartung iPhone 14", color = TextMuted, fontSize = 12.sp) },
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
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Kunde / Firma", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("z.B. Max Mustermann GmbH", color = TextMuted, fontSize = 12.sp) },
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
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Kontakt (Telefon / E-Mail)", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("z.B. 0171-1234567 oder kunde@mail.de", color = TextMuted, fontSize = 12.sp) },
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
                    label = { Text("Gerät / Hardware", color = TextSecondary, fontSize = 12.sp) },
                    placeholder = { Text("z.B. Gaming PC RTX 4070 oder Jura E8", color = TextMuted, fontSize = 12.sp) },
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "$selectedType - ${device.ifBlank { "Neues Dokument" }}" }
                    onConfirm(finalTitle, selectedType, customer, contact, device)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Erstellen & Bearbeiten", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = TextSecondary)
            }
        }
    )
}
