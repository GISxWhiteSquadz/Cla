package com.example.ui.screens.office

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfficeDocumentEntity
import com.example.ui.RepairViewModel
import com.example.ui.theme.*
import com.example.ui.util.DocumentExportHelper
import com.example.ui.util.bounceClick
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeArchiveSection(
    viewModel: RepairViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.officeDocuments.collectAsState()
    val selectedFolder by viewModel.selectedArchiveFolder.collectAsState()
    val searchQuery by viewModel.archiveSearchQuery.collectAsState()
    val context = LocalContext.current

    val folders = listOf(
        "Alle",
        "Angebote & Kostenvoranschläge",
        "Rechnungen",
        "Reparatur- & PC-Aufträge",
        "Protokolle & Berichte",
        "PC-Konfigurationen & Budget"
    )

    val filteredDocs = remember(documents, selectedFolder, searchQuery) {
        documents.filter { doc ->
            val matchesFolder = (selectedFolder == "Alle") || doc.categoryFolder.equals(selectedFolder, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    doc.title.contains(searchQuery, ignoreCase = true) ||
                    doc.documentNumber.contains(searchQuery, ignoreCase = true) ||
                    doc.customerName.contains(searchQuery, ignoreCase = true) ||
                    doc.deviceOrProject.contains(searchQuery, ignoreCase = true)
            matchesFolder && matchesQuery
        }
    }

    val totalStorageBytes = remember(documents) {
        documents.sumOf { it.fileSizeBytes }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Storage Header Stats Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = AccentCyan
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Geordnetes Dateiarchiv",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${documents.size} Dokumente • ${(totalStorageBytes / 1024.0).toInt()} KB lokal gesichert",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryBlue,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlueVariant),
                    modifier = Modifier.bounceClick {
                        viewModel.setEditingDocument(null)
                        viewModel.selectOfficeSubTab(2) // Jump to Editor
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("Neu +", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setArchiveSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            placeholder = { Text("Archiv durchsuchen (Titel, Nr., Kunde, Gerät)...", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Suchen", tint = TextMuted, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setArchiveSearchQuery("") }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = TextMuted, modifier = Modifier.size(14.dp))
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
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
        )

        // Folder Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(folders) { folder ->
                val isSelected = selectedFolder == folder
                val count = remember(documents, folder) {
                    if (folder == "Alle") documents.size
                    else documents.count { it.categoryFolder.equals(folder, ignoreCase = true) }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) PrimaryBlueVariant.copy(alpha = 0.35f) else DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBlue else DarkBorder),
                    modifier = Modifier.bounceClick { viewModel.setArchiveFolder(folder) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            if (folder == "Alle") Icons.Default.FolderSpecial else Icons.Default.Folder,
                            contentDescription = null,
                            tint = if (isSelected) AccentCyan else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = folder,
                            color = if (isSelected) AccentCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) PrimaryBlue else DarkSurfaceElevated
                        ) {
                            Text(
                                text = "$count",
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Archive Files List
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
                            Icons.Default.FolderZip,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = TextMuted
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Keine archivierten Dokumente gefunden",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "In diesem Ordner oder mit diesem Suchbegriff liegt keine Datei.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredDocs, key = { it.id }) { doc ->
                    ModernArchiveFileCard(
                        doc = doc,
                        onOpen = {
                            viewModel.setEditingDocument(doc)
                            viewModel.selectOfficeSubTab(2) // Jump to Editor
                        },
                        onShare = {
                            DocumentExportHelper.shareDocument(context, doc)
                        },
                        onDelete = {
                            viewModel.deleteOfficeDocument(doc.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernArchiveFileCard(
    doc: OfficeDocumentEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(doc.updatedTimestamp) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date(doc.updatedTimestamp))
    }

    val fileExt = when (doc.docType) {
        "Angebot", "Rechnung", "Prüfprotokoll" -> "PDF"
        "PC-Bau Plan" -> "XLS"
        else -> "TXT"
    }

    val extColor = when (fileExt) {
        "PDF" -> DangerRed
        "XLS" -> SuccessGreen
        else -> AccentCyan
    }

    val sizeKb = (doc.fileSizeBytes / 1024.0).coerceAtLeast(0.5)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onOpen() },
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // File Extension Badge
            Surface(
                color = extColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, extColor.copy(alpha = 0.4f)),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = fileExt,
                        color = extColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // File Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = doc.documentNumber,
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("•", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = doc.categoryFolder,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = dateStr,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text("•", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = String.format(Locale.GERMANY, "%.1f KB", sizeKb),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    if (doc.totalEuro > 0) {
                        Text("•", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = String.format(Locale.GERMANY, "%.2f €", doc.totalEuro),
                            color = PrimaryBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Teilen / Exportieren",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Löschen",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
