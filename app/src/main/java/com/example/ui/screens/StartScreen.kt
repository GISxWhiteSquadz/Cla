package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Download
import com.example.ui.components.calculateScrollProgress
import com.example.ui.components.interactiveBounce
import com.example.ui.components.pulsingGlow
import com.example.ui.components.scrollReactiveBackground
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.SuccessGreen
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.RepairHistoryEntity
import com.example.model.RepairCategory
import com.example.ui.RepairViewModel
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueVariant
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StartScreen(
    viewModel: RepairViewModel,
    onNavigateToCategories: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCameraDialog by remember { mutableStateOf(false) }
    var photoInputDescription by remember { mutableStateOf("") }
    val isDiagnosing by viewModel.isDiagnosing.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val language by viewModel.language.collectAsState()
    val allSkillPacks by viewModel.allSkillPacks.collectAsState()
    val installedSkillPacks by viewModel.installedSkillPacks.collectAsState()
    val isEn = language.uppercase() == "EN"

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
        // App Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "CoreRepair Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CoreSystems",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "CoreRepair AI",
                            color = TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Language Switcher Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.setLanguage(if (isEn) "DE" else "EN")
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("language_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = "Language",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEn) "EN" else "DE",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Subtitle
        item {
            Text(
                text = if (isEn) "What do you want to repair today?" else "Was möchtest du heute reparieren?",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Central All-in-One Gemini AI Command & Diagnosis Center
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFF0F1D33), Color(0xFF101726))
                        )
                    )
                    .border(
                        1.2.dp,
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(PrimaryBlue, Color(0xFF38BDF8), PrimaryBlueVariant)
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Bar inside Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(PrimaryBlue, Color(0xFF00D4FF))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Column {
                                Text(
                                    text = "Gemini KI-Hardware-Agent",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Zentrale All-in-One Eingabe & Analyse",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Google Search Live",
                                color = Color(0xFF38BDF8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grand All-in-One Input Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isEn)
                                    "Describe issue, device, error code or ask Gemini..."
                                else
                                    "Beschreibe dein Anliegen, Fehlercode, Gerät oder Frage an Gemini...",
                                color = TextMuted,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
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
                            .testTag("device_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Controls inside Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Camera / Visual Scan Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showCameraDialog = true }
                                    .testTag("camera_diagnosis_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Foto-Scan",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Foto-Scan",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Interactive Gemini Chat Dialog Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.openGeminiChat() }
                                    .testTag("gemini_chatbot_launcher_card")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = "KI-Chat",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "KI-Chat",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Send / Start Button
                        Button(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.startDiagnosis(searchQuery)
                                } else {
                                    viewModel.openGeminiChat()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .pulsingGlow(glowColor = PrimaryBlue)
                                .interactiveBounce {
                                    if (searchQuery.isNotBlank()) {
                                        viewModel.startDiagnosis(searchQuery)
                                    } else {
                                        viewModel.openGeminiChat()
                                    }
                                }
                                .testTag("search_submit_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Analysieren",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "Analysieren" else "Starten",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick-Prompt Suggestion Chips
                    Text(
                        text = "Schnell-Themen:",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickPrompts = listOf(
                            "Display & Glasbruch",
                            "Akku & BMS Fehler",
                            "Kein Start / PMIC Kurzschluss",
                            "Bootloop / Fastboot Fix",
                            "Wasserschaden Sofortmaßnahme"
                        )
                        items(quickPrompts) { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(0.8.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        searchQuery = prompt
                                        viewModel.startDiagnosis(prompt)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Offline 'Repair Skill' Packs Status & Launcher Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(
                        width = 1.dp,
                        color = if (installedSkillPacks.isNotEmpty()) SuccessGreen.copy(alpha = 0.4f) else DarkBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (installedSkillPacks.isNotEmpty()) SuccessGreen.copy(alpha = 0.15f) else DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = if (installedSkillPacks.isNotEmpty()) SuccessGreen else PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Offline 'Repair Skill' Packs",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${installedSkillPacks.size}/${allSkillPacks.size} Geladen",
                                        color = SuccessGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Vorlagen, Sicherheitsregeln & Tests für netzunabhängigen Agenten",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Packs verwalten",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Interactive Workbench & Utilities Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { viewModel.openWorkbench() }
                    .padding(16.dp)
                    .testTag("open_workbench_card")
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
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = "Werkbank",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Localization.t("start_workbench_card_title", language),
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Localization.t("start_workbench_card_sub", language),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Öffnen",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Beginner Guide & Technical Glossary Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, WarningAmber.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { viewModel.openGlossary() }
                    .padding(16.dp)
                    .testTag("open_glossary_card")
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
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "Glossar",
                                tint = WarningAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = Localization.t("start_glossary_card_title", language),
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Localization.t("start_glossary_card_sub", language),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Öffnen",
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Categories Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gerätekategorien",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Alle anzeigen",
                    color = PrimaryBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onNavigateToCategories() }
                        .padding(4.dp)
                        .testTag("show_all_categories_button")
                )
            }
        }

        // 2x4 Categories Grid
        item {
            val displayCategories = viewModel.categories.take(8)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 4) {
                        val cat = displayCategories[i]
                        CategoryTile(
                            category = cat,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = {
                                val catName = when (cat.id) {
                                    "smartphone" -> "Smartphones"
                                    "laptop" -> "Laptops & Notebooks"
                                    "tablet" -> "Tablets"
                                    "haushalt" -> "Haushaltsgeräte"
                                    "gaming" -> "Gaming & Konsolen"
                                    "audio" -> "Audio & Hi-Fi"
                                    "fahrrad" -> "Fahrrad & E-Bike"
                                    "fahrzeug" -> "Fahrzeuge & KFZ"
                                    "pc_build" -> "PC-Bau & Hardware"
                                    "maker_robotics" -> "Robotik & Maker"
                                    else -> cat.title.replace("\n", " ").trim()
                                }
                                viewModel.openCategory(catName)
                                onNavigateToCategories()
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 4 until 8) {
                        val cat = displayCategories[i]
                        CategoryTile(
                            category = cat,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = {
                                val catName = when (cat.id) {
                                    "smartphone" -> "Smartphones"
                                    "laptop" -> "Laptops & Notebooks"
                                    "tablet" -> "Tablets"
                                    "haushalt" -> "Haushaltsgeräte"
                                    "gaming" -> "Gaming & Konsolen"
                                    "audio" -> "Audio & Hi-Fi"
                                    "fahrrad" -> "Fahrrad & E-Bike"
                                    "fahrzeug" -> "Fahrzeuge & KFZ"
                                    "pc_build" -> "PC-Bau & Hardware"
                                    "maker_robotics" -> "Robotik & Maker"
                                    else -> cat.title.replace("\n", " ").trim()
                                }
                                viewModel.openCategory(catName)
                                onNavigateToCategories()
                            }
                        )
                    }
                }
            }
        }

        // Recent Repairs Section
        item {
            Text(
                text = "Letzte Reparaturen",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (historyList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Noch keine Reparaturen durchgeführt",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(historyList.take(3)) { history ->
                RecentRepairItem(
                    history = history,
                    onClick = { viewModel.loadDiagnosisFromHistory(history) }
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { viewModel.openDisclaimerModal() },
                    modifier = Modifier.testTag("open_disclaimer_button")
                ) {
                    Text(
                        text = if (isEn) "View Safety Notice & Disclaimer" else "Sicherheitshinweise & Haftungsausschluss anzeigen",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Camera / Typeplate input dialog
    if (showCameraDialog) {
        AlertDialog(
            onDismissRequest = { showCameraDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Gerät oder Typenschild erfassen",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Fotografiere das Gerät, Typenschild oder beschreibe die Modellnummer. Das lokale Vision-Modell (Qwen2.5-VL-3B) gleicht die Daten mit der RAG-Datenbank ab.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = photoInputDescription,
                        onValueChange = { photoInputDescription = it },
                        label = { Text("Typenschild / Modell / Fehlerbeschreibung", color = TextSecondary) },
                        placeholder = { Text("z.B. DeLonghi ECAM 22.110.B Fehler 8", color = TextMuted) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("camera_dialog_text_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoInputDescription = "Bosch WAN28200 Serie 6 - Fehler E18"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bosch E18", fontSize = 11.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                photoInputDescription = "DeLonghi Magnifica S ECAM - Fehler 8"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DeLonghi", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val query = photoInputDescription.ifBlank { "DeLonghi Magnifica S Brüheinheit klemmt" }
                        showCameraDialog = false
                        viewModel.startDiagnosis(
                            deviceQuery = query,
                            imageUri = "content://camera/typeplate_sample.jpg"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.testTag("confirm_camera_analysis_button")
                ) {
                    Text("Jetzt analysieren", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraDialog = false }) {
                    Text("Abbrechen", color = TextSecondary)
                }
            }
        )
    }

    if (isDiagnosing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Lokale RAG-Diagnose läuft...",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GGUF-Modell analysiert Schaltpläne & Wissensbasis",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CategoryTile(
    category: RepairCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val icon = when (category.iconName) {
        "smartphone" -> Icons.Filled.PhoneAndroid
        "laptop" -> Icons.Filled.Computer
        "tablet" -> Icons.Filled.Tablet
        "haushalt" -> Icons.Filled.Kitchen
        "coffee" -> Icons.Filled.Coffee
        "gaming", "gamepad" -> Icons.Filled.SportsEsports
        "audio" -> Icons.AutoMirrored.Filled.VolumeUp
        "bike" -> Icons.AutoMirrored.Filled.DirectionsBike
        "car" -> Icons.Filled.DirectionsCar
        "pc_hardware" -> Icons.Filled.Hardware
        "robotics" -> Icons.Filled.Build
        else -> Icons.Filled.Build
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.title,
            tint = PrimaryBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecentRepairItem(
    history: RepairHistoryEntity,
    onClick: () -> Unit
) {
    val dateStr = remember(history.timestamp) {
        val sdf = SimpleDateFormat("dd. MMM, HH:mm", Locale.GERMAN)
        sdf.format(Date(history.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("recent_repair_item_${history.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.deviceName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = history.problemDescription,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$dateStr • ${history.modelUsed}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Öffnen",
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
