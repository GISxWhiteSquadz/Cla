package com.example.ui.screens.workbench

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Download
import com.example.ui.components.calculateScrollProgress
import com.example.ui.components.interactiveBounce
import com.example.ui.components.pulsingGlow
import com.example.ui.components.scrollReactiveBackground
import com.example.ui.theme.DarkBackground
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.DeviceComponent
import com.example.model.DeviceProfile
import com.example.model.ErrorCodeItem
import com.example.model.MessageSender
import com.example.ui.RepairViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkbenchProjectSection(viewModel: RepairViewModel) {
    val coroutineScope = rememberCoroutineScope()

    // Observe active workbench state
    val activeProfile by viewModel.workbenchActiveProfile.collectAsState()
    val selectedComponent by viewModel.workbenchSelectedComponent.collectAsState()
    val selectedSoftwareIssue by viewModel.workbenchSoftwareIssue.collectAsState()
    val isWorkbenchLoading by viewModel.isWorkbenchLoading.collectAsState()
    val chatMessages by viewModel.workbenchChatMessages.collectAsState()
    val allDbProfiles by viewModel.allDeviceProfiles.collectAsState()

    // Chat input local state
    var chatInputText by remember { mutableStateOf("") }
    var isChatHistoryExpanded by remember { mutableStateOf(false) }
    val stepChecks = remember { mutableStateMapOf<String, Boolean>() }
    var showOfficeSuccessMessage by remember { mutableStateOf(false) }

    fun exportComponentToOffice(profile: DeviceProfile, comp: DeviceComponent) {
        viewModel.createOfficeDocument(
            title = "Werkstattauftrag: ${comp.componentName}",
            docType = "Auftrag",
            categoryFolder = profile.category,
            customerName = "Werkstatt-Kunde",
            customerContact = "werkbank@intern",
            deviceOrProject = "${profile.manufacturer} ${profile.modelName}",
            content = """
GERÄT: ${profile.manufacturer} ${profile.modelName} (Typ: ${profile.modelNumber})
KATEGORIE: ${profile.category} > ${profile.subcategory}
BAUGRUPPE: ${comp.componentName} (${comp.componentCategory})
TEILENUMMER / SPEZIFIKATION: ${comp.partNumberOrSpec}
GESCHÄTZTE REPARATURZEIT: ${comp.estimatedTimeMinutes} Minuten
SCHWIERIGKEITSGRAD: ${comp.difficulty}

BENÖTIGTE WERKZEUGE:
${comp.tools.joinToString("\n") { "• $it" }}

SICHERHEITSHINWEISE:
${comp.warnings.joinToString("\n") { "• $it" }}

REPARATURSCHRITTE:
${comp.repairSteps.mapIndexed { idx, step -> "${idx + 1}. [ ] ${step.title}: ${step.description}" }.joinToString("\n")}

STATUS: Auf Werkbank diagnostiziert & bereit zur Instandsetzung
            """.trimIndent(),
            lineItemsJson = "[]",
            subtotal = 49.0,
            taxRate = 19.0,
            total = 58.31
        )
        showOfficeSuccessMessage = true
        coroutineScope.launch {
            delay(3500)
            showOfficeSuccessMessage = false
        }
    }

    val listState = rememberLazyListState()
    val scrollProgress = listState.calculateScrollProgress(maxItemIndexThreshold = 5)
    val installedSkillPacks by viewModel.installedSkillPacks.collectAsState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .scrollReactiveBackground(scrollProgress = scrollProgress, baseBackground = DarkBackground)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // =====================================================================
        // 1. CHATEINGABEFENSTER GANZ OBEN DIREKT UNTER DER REITERNAVIGATION
        // =====================================================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(PrimaryBlue.copy(alpha = 0.7f), Color(0xFF38BDF8).copy(alpha = 0.6f))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workbench_chat_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Chat Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "KI-Werkbank & Hardware-Meister",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (activeProfile != null) {
                                        "Projekt: ${activeProfile?.manufacturer} ${activeProfile?.modelName}"
                                    } else {
                                        "Bereit für Fragen oder neue Geräte-Anfragen"
                                    },
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Live-Recherche",
                                color = Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CHATEINGABEFELD DIREKT GANZ OBEN UNTER DER LEISTE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = {
                                Text(
                                    "Gerätename eingeben oder Reparaturfrage an KI stellen...",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                if (chatInputText.isNotEmpty()) {
                                    IconButton(
                                        onClick = { chatInputText = "" },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Eingabe löschen",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workbench_chat_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val text = chatInputText.trim()
                                if (text.isNotEmpty()) {
                                    viewModel.sendWorkbenchChatMessage(text)
                                    chatInputText = ""
                                }
                            },
                            enabled = chatInputText.isNotBlank() && !isWorkbenchLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .pulsingGlow(glowColor = PrimaryBlue)
                                .interactiveBounce {
                                    val text = chatInputText.trim()
                                    if (text.isNotEmpty() && !isWorkbenchLoading) {
                                        viewModel.sendWorkbenchChatMessage(text)
                                        chatInputText = ""
                                    }
                                }
                                .testTag("workbench_chat_send_button")
                        ) {
                            if (isWorkbenchLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Senden",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Schnellauswahl-Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (installedSkillPacks.isNotEmpty()) PrimaryBlue.copy(alpha = 0.2f) else DarkSurface)
                                    .border(0.8.dp, PrimaryBlue, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.openSkillPacksManager() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Offline-Skills (${installedSkillPacks.size})",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        val prompts = listOf(
                            "iPhone 14 Pro laden",
                            "PlayStation 5 laden",
                            "DeLonghi Kaffeevollautomat laden",
                            "Soll-Messwerte & Pinbelegung",
                            "Welche Schrauben gehören wohin?",
                            "Temperatur für Klebefalz",
                            "Kurzschluss lokalisieren"
                        )
                        items(prompts) { p ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurface)
                                    .border(0.8.dp, DarkBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.sendWorkbenchChatMessage(p)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(text = p, color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Ladezustand / Recherchefortschritt
                    if (isWorkbenchLoading) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = DarkSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "KI recherchiert Spezifikationen & speichert in lokaler Datenbank...",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Letzte KI-Antwort & Konversation
                    if (chatMessages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val lastMessage = chatMessages.lastOrNull()
                        if (lastMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, PrimaryBlue.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val senderColor = when (lastMessage.sender) {
                                            MessageSender.USER -> AccentCyan
                                            MessageSender.AGENT -> Color(0xFF38BDF8)
                                            MessageSender.SYSTEM -> WarningAmber
                                        }
                                        val senderLabel = when (lastMessage.sender) {
                                            MessageSender.USER -> "Du"
                                            MessageSender.AGENT -> lastMessage.modelTag ?: "Gemini Hardware-Meister"
                                            MessageSender.SYSTEM -> "System"
                                        }
                                        Text(
                                            text = senderLabel,
                                            color = senderColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (chatMessages.size > 1) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable { isChatHistoryExpanded = !isChatHistoryExpanded }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isChatHistoryExpanded) "Verlauf verbergen" else "Verlauf (${chatMessages.size})",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = if (isChatHistoryExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = lastMessage.text,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        // Vollständiger Verlauf (eingeklappt/ausgeklappt)
                        AnimatedVisibility(visible = isChatHistoryExpanded && chatMessages.size > 1) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkSurface)
                                        .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(chatMessages.dropLast(1)) { msg ->
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                val senderColor = when (msg.sender) {
                                                    MessageSender.USER -> AccentCyan
                                                    MessageSender.AGENT -> Color(0xFF38BDF8)
                                                    MessageSender.SYSTEM -> WarningAmber
                                                }
                                                val senderLabel = when (msg.sender) {
                                                    MessageSender.USER -> "Du"
                                                    MessageSender.AGENT -> msg.modelTag ?: "Gemini Hardware-Meister"
                                                    MessageSender.SYSTEM -> "System"
                                                }
                                                Text(
                                                    text = senderLabel,
                                                    color = senderColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(1.dp))
                                                Text(
                                                    text = msg.text,
                                                    color = TextSecondary,
                                                    fontSize = 10.sp,
                                                    lineHeight = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. DARUNTER: NUR DAS ZUM ZEITPUNKT ANGEFRAGTE GERÄT ODER LEERE WERKBANK
        // =====================================================================
        val profile = activeProfile
        if (profile != null) {
            // -----------------------------------------------------------------
            // A. AKTIVES GERÄT: HEADER & 1-KLICK SCHLIESSEN BUTTON
            // -----------------------------------------------------------------
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workbench_active_device_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.PhoneAndroid,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${profile.manufacturer} ${profile.modelName}",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${profile.category} > ${profile.subcategory} • Typ: ${profile.modelNumber} (${profile.releaseYear})",
                                    color = AccentCyan,
                                    fontSize = 11.sp
                                )
                            }

                            // 1-KLICK SCHLIESSEN / REPARATUR ABSCHLIESSEN BUTTON
                            Button(
                                onClick = { viewModel.clearWorkbenchDevice() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkSurface,
                                    contentColor = DangerRed
                                ),
                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("workbench_close_device_button")
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Schließen", modifier = Modifier.size(14.dp), tint = DangerRed)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Schließen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                            }
                        }

                        // Specifications Grid
                        if (profile.specifications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .padding(8.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    profile.specifications.take(4).forEach { spec ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${spec.category}: ${spec.label}",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = spec.value,
                                                color = TextPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // B. BAUTEILELISTE DIESES GERÄTS
            // -----------------------------------------------------------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bauteileliste (${profile.components.size} Baugruppen):",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tippen für Anleitung",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(profile.components) { comp ->
                        val isSel = selectedComponent?.componentName == comp.componentName && selectedSoftwareIssue == null
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) PrimaryBlue.copy(alpha = 0.25f) else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSel) PrimaryBlue else DarkBorder
                            ),
                            modifier = Modifier
                                .width(185.dp)
                                .clickable {
                                    viewModel.selectWorkbenchComponent(comp)
                                }
                                .testTag("workbench_component_${comp.componentName.replace(" ", "_")}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Build,
                                        contentDescription = null,
                                        tint = if (isSel) PrimaryBlue else AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = comp.componentCategory,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comp.componentName,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${comp.estimatedTimeMinutes} Min | ${comp.difficulty}",
                                    color = WarningAmber,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // C. REPARATURANLEITUNG FÜR DAS AUSGEWÄHLTE BAUTEIL
            // -----------------------------------------------------------------
            selectedComponent?.let { comp ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workbench_repair_guide_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = comp.componentName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${comp.partNumberOrSpec} • ${comp.componentCategory}",
                                        color = AccentCyan,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = { exportComponentToOffice(profile, comp) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("In Office", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (showOfficeSuccessMessage) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Auftrag erfolgreich im Office-Bereich angelegt!",
                                        color = SuccessGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Required Tools
                            if (comp.tools.isNotEmpty()) {
                                Text("Erforderliche Werkzeuge:", color = TextMuted, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(comp.tools.joinToString(", "), color = AccentCyan, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Safety Warnings
                            if (comp.warnings.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DangerRed.copy(alpha = 0.15f))
                                        .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sicherheitswarnungen:", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        comp.warnings.forEach { w ->
                                            Text("• $w", color = TextPrimary, fontSize = 10.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Step-by-Step Checklist
                            Text(
                                text = "Reparaturschritte zum Abhaken:",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            comp.repairSteps.forEachIndexed { idx, step ->
                                val checkKey = "${comp.componentName}_$idx"
                                val isChecked = stepChecks[checkKey] ?: step.isCompleted
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { stepChecks[checkKey] = !isChecked }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { stepChecks[checkKey] = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SuccessGreen,
                                            uncheckedColor = TextMuted
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${idx + 1}. ${step.title}",
                                            color = if (isChecked) TextMuted else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (isChecked) TextDecoration.LineThrough else null
                                        )
                                        Text(
                                            text = step.description,
                                            color = if (isChecked) TextMuted else TextSecondary,
                                            fontSize = 10.sp,
                                            textDecoration = if (isChecked) TextDecoration.LineThrough else null
                                        )
                                        if (!step.caution.isNullOrBlank()) {
                                            Text(
                                                text = "Achtung: ${step.caution}",
                                                color = WarningAmber,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // D. SOFTWARE- & FIRMWARE-LÖSUNGEN DIESES GERÄTS
            // -----------------------------------------------------------------
            if (profile.errorCodes.isNotEmpty() || profile.softwareProcedures.isNotEmpty()) {
                item {
                    Text(
                        text = "Software- & Firmware-Lösungen:",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Error codes row
                if (profile.errorCodes.isNotEmpty()) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(profile.errorCodes) { err ->
                                val isSel = selectedSoftwareIssue?.code == err.code
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSel) DangerRed.copy(alpha = 0.2f) else DarkSurfaceElevated
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSel) DangerRed else DarkBorder
                                    ),
                                    modifier = Modifier
                                        .width(185.dp)
                                        .clickable {
                                            viewModel.selectWorkbenchSoftwareIssue(err)
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                tint = DangerRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = err.code,
                                                color = DangerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = err.description,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Software Issue or Procedure Details
                selectedSoftwareIssue?.let { issue ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${issue.code} - ${issue.description}",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Ursache: ${issue.cause}", color = TextSecondary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lösung: ${issue.solution}", color = SuccessGreen, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Software Procedures
                if (profile.softwareProcedures.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Terminal, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Software- & Wartungsverfahren:", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                profile.softwareProcedures.forEach { proc ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkSurface)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = proc,
                                            color = AccentCyan,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
            // =================================================================
            // CASE B: WERKBANK IST FREI (EMPTY STATE)
            // =================================================================
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workbench_empty_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Handyman,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Die Werkbank ist aktuell frei",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Kein aktives Reparaturprojekt geöffnet. Gib oben im Chat einfach dein gewünschtes Gerät ein (z. B. 'iPhone 14 Pro Display tauschen' oder 'PS5 HDMI Port'), oder wähle ein gespeichertes Gerät aus der Datenbank:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick-load from Room database
                        if (allDbProfiles.isNotEmpty()) {
                            Text(
                                text = "Gespeicherte Geräte aus der Datenbank:",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allDbProfiles.take(8).forEach { dbItem ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkSurface)
                                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                viewModel.loadDeviceByFingerprintToWorkbench(dbItem.fingerprintKey)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.PhoneAndroid,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${dbItem.manufacturer} ${dbItem.modelName}",
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
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
