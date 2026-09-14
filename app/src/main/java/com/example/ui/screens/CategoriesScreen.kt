package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.DeviceProfileEntity
import com.example.model.DeviceComponent
import com.example.model.DeviceProfile
import com.example.model.RepairCategory
import com.example.ui.RepairViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun CategoriesScreen(
    viewModel: RepairViewModel,
    onBack: (() -> Unit)? = null
) {
    val selectedComponentDetail by viewModel.selectedComponentDetail.collectAsState()
    val selectedDeviceProfile by viewModel.selectedDeviceProfile.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsState()
    val selectedManufacturer by viewModel.selectedManufacturer.collectAsState()
    val allProfiles by viewModel.allDeviceProfiles.collectAsState()
    val catalogSearchQuery by viewModel.catalogSearchQuery.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when {
            // Level 3: Detailed Component Replacement Guide
            selectedComponentDetail != null -> {
                ComponentDetailView(
                    component = selectedComponentDetail!!,
                    deviceProfile = selectedDeviceProfile,
                    onBack = { viewModel.closeComponentDetail() },
                    onStartRepair = { comp ->
                        selectedDeviceProfile?.let { prof ->
                            viewModel.startDiagnosisFromComponent(prof, comp)
                        }
                    }
                )
            }

            // Level 2: Comprehensive Device Profile & Dossier
            selectedDeviceProfile != null -> {
                DeviceProfileView(
                    profile = selectedDeviceProfile!!,
                    onBack = { viewModel.navigateBackInCatalog() },
                    onSelectComponent = { comp -> viewModel.openComponentDetail(comp) },
                    onStartDiagnosis = { prof ->
                        viewModel.startDiagnosis(
                            deviceQuery = "${prof.manufacturer} ${prof.modelName} General-Diagnose",
                            categoryHint = prof.category
                        )
                    }
                )
            }

            // Level 1: Category Detail with Manufacturers, Subcategories and Devices
            selectedCategory != null -> {
                CategoryDetailView(
                    categoryName = selectedCategory!!,
                    allProfiles = allProfiles,
                    selectedSubcategory = selectedSubcategory,
                    selectedManufacturer = selectedManufacturer,
                    onSelectSubcategory = { sub -> viewModel.selectSubcategory(sub) },
                    onSelectManufacturer = { mfr -> viewModel.openManufacturer(mfr) },
                    onSelectDevice = { fingerprintKey -> viewModel.openDeviceProfile(fingerprintKey) },
                    onBack = {
                        if (!viewModel.navigateBackInCatalog()) {
                            onBack?.invoke()
                        }
                    }
                )
            }

            // Level 0: All Categories & Global Search
            else -> {
                CategoriesRootView(
                    categories = viewModel.categories,
                    allProfiles = allProfiles,
                    searchQuery = catalogSearchQuery,
                    onSearchQueryChange = { viewModel.setCatalogSearchQuery(it) },
                    onSelectCategory = { catTitle, initialSub -> viewModel.openCategory(catTitle, initialSub) },
                    onSelectDevice = { fingerprintKey -> viewModel.openDeviceProfile(fingerprintKey) },
                    onQuickIssueClick = { issue, catTitle ->
                        viewModel.startDiagnosis(
                            deviceQuery = "$issue ($catTitle)",
                            categoryHint = catTitle
                        )
                    },
                    onBack = onBack
                )
            }
        }
    }
}

// =============================================================================
// LEVEL 0: ROOT CATEGORIES & GLOBAL SEARCH
// =============================================================================

@Composable
fun CategoriesRootView(
    categories: List<RepairCategory>,
    allProfiles: List<DeviceProfileEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String, String?) -> Unit,
    onSelectDevice: (String) -> Unit,
    onQuickIssueClick: (String, String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val filteredProfiles = if (searchQuery.isNotBlank()) {
        allProfiles.filter {
            it.modelName.contains(searchQuery, ignoreCase = true) ||
                    it.manufacturer.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.modelNumber.contains(searchQuery, ignoreCase = true)
        }
    } else emptyList()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            if (onBack != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück zur Startseite",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zurück zur Startseite",
                        color = AccentCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = "Geräte-Katalog & Bauteile",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Wähle eine Kategorie für Hersteller, Modelle & Bauteil-Profile oder nutze die Suche.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Gerät, Hersteller oder Modell suchen...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suchen",
                        tint = PrimaryBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Löschen", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("catalog_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Show search results if user is actively searching
        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "Gefundene Geräte (${filteredProfiles.size}):",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (filteredProfiles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kein Gerät zu \"$searchQuery\" im Offline-Katalog.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Starte eine Diagnose auf dem Startbildschirm – der Agent recherchiert online und speichert das Dossier automatisch hier ab!",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(filteredProfiles) { prof ->
                    DeviceSummaryCard(
                        profile = prof,
                        onClick = { onSelectDevice(prof.fingerprintKey) }
                    )
                }
            }
        } else {
            // Render all Categories
            items(categories) { cat ->
                val normalizedCategoryName = getNormalizedCategoryName(cat)
                val categoryProfiles = allProfiles.filter {
                    isProfileInCategory(it.category, normalizedCategoryName) || isProfileInCategory(it.category, cat.id)
                }
                val deviceCount = categoryProfiles.size
                val subcategories = categoryProfiles
                    .map { it.subcategory }
                    .filter { it.isNotBlank() }
                    .distinct()

                CategoryBrowseCard(
                    category = cat,
                    deviceCount = deviceCount,
                    subcategories = subcategories,
                    onCardClick = { onSelectCategory(normalizedCategoryName, null) },
                    onSubcategoryClick = { sub -> onSelectCategory(normalizedCategoryName, sub) },
                    onIssueClick = { issue -> onQuickIssueClick(issue, normalizedCategoryName) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CategoryBrowseCard(
    category: RepairCategory,
    deviceCount: Int,
    subcategories: List<String> = emptyList(),
    onCardClick: () -> Unit,
    onSubcategoryClick: (String) -> Unit = {},
    onIssueClick: (String) -> Unit
) {
    val icon = getCategoryIcon(category.iconName)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
            .testTag("category_card_${category.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.title.replace("\n", " "),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (deviceCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlueContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$deviceCount Modelle",
                                color = AccentCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Text(
                    text = category.description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Öffnen",
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        // Subcategories Preview Chips
        if (subcategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Unterkategorien & Baugruppen:",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(subcategories) { sub ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(0.8.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .clickable { onSubcategoryClick(sub) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = sub,
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// LEVEL 1: CATEGORY DETAIL (HERSTELLER & GERÄTE)
// =============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDetailView(
    categoryName: String,
    allProfiles: List<DeviceProfileEntity>,
    selectedSubcategory: String?,
    selectedManufacturer: String?,
    onSelectSubcategory: (String?) -> Unit,
    onSelectManufacturer: (String) -> Unit,
    onSelectDevice: (String) -> Unit,
    onBack: () -> Unit
) {
    val categoryProfiles = allProfiles.filter {
        isProfileInCategory(it.category, categoryName)
    }

    // Dynamic distinct subcategories from all profiles in this category
    val availableSubcategories = categoryProfiles
        .map { it.subcategory }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    // Filter by subcategory
    val subcategoryFiltered = if (!selectedSubcategory.isNullOrBlank()) {
        categoryProfiles.filter { it.subcategory.equals(selectedSubcategory, ignoreCase = true) }
    } else {
        categoryProfiles
    }

    val manufacturers = subcategoryFiltered.map { it.manufacturer }.distinct().sorted()

    val displayedProfiles = if (!selectedManufacturer.isNullOrBlank()) {
        subcategoryFiltered.filter { it.manufacturer.equals(selectedManufacturer, ignoreCase = true) }
    } else {
        subcategoryFiltered
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = categoryName,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (!selectedSubcategory.isNullOrBlank()) {
                            "${displayedProfiles.size} erfasste Modelle in »$selectedSubcategory«"
                        } else {
                            "${categoryProfiles.size} erfasste Geräte in dieser Kategorie"
                        },
                        color = if (!selectedSubcategory.isNullOrBlank()) AccentCyan else TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Subcategory Filter Chips
        if (availableSubcategories.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Unterkategorien & Baugruppen:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!selectedSubcategory.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filter aktiv",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAllSelected = selectedSubcategory.isNullOrBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isAllSelected) PrimaryBlue else DarkSurfaceElevated)
                                .border(1.dp, if (isAllSelected) PrimaryBlue else DarkBorder, RoundedCornerShape(20.dp))
                                .clickable { onSelectSubcategory(null) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Alle Baugruppen (${categoryProfiles.size})",
                                color = if (isAllSelected) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    items(availableSubcategories) { sub ->
                        val isSelected = selectedSubcategory.equals(sub, ignoreCase = true)
                        val subCount = categoryProfiles.count { it.subcategory.equals(sub, ignoreCase = true) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AccentCyan.copy(alpha = 0.25f) else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) AccentCyan else DarkBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isSelected) onSelectSubcategory(null) else onSelectSubcategory(sub)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "$sub ($subCount)",
                                    color = if (isSelected) AccentCyan else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Manufacturer Filter Chips
        if (manufacturers.isNotEmpty()) {
            item {
                Text(
                    text = "Hersteller:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAllSelected = selectedManufacturer.isNullOrBlank()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isAllSelected) PrimaryBlue else DarkSurfaceElevated)
                                .border(1.dp, if (isAllSelected) PrimaryBlue else DarkBorder, RoundedCornerShape(20.dp))
                                .clickable { onSelectManufacturer("") }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Alle (${subcategoryFiltered.size})",
                                color = if (isAllSelected) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    items(manufacturers) { mfr ->
                        val isSelected = selectedManufacturer.equals(mfr, ignoreCase = true)
                        val mfrCount = subcategoryFiltered.count { it.manufacturer.equals(mfr, ignoreCase = true) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PrimaryBlue else DarkSurfaceElevated)
                                .border(1.dp, if (isSelected) PrimaryBlue else DarkBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isSelected) onSelectManufacturer("") else onSelectManufacturer(mfr)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$mfr ($mfrCount)",
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        item {
            val filterSummary = when {
                !selectedManufacturer.isNullOrBlank() && !selectedSubcategory.isNullOrBlank() ->
                    "Modelle von $selectedManufacturer in $selectedSubcategory (${displayedProfiles.size})"
                !selectedManufacturer.isNullOrBlank() ->
                    "Modelle von $selectedManufacturer (${displayedProfiles.size})"
                !selectedSubcategory.isNullOrBlank() ->
                    "Alle Komponenten in $selectedSubcategory (${displayedProfiles.size})"
                else ->
                    "Alle Modelle & Typen (${displayedProfiles.size})"
            }
            Text(
                text = filterSummary,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (displayedProfiles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Keine Geräte für diesen Filter im Katalog vorhanden.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Sobald du im Startbildschirm nach einer Reparatur suchst, kuratiert der KI-Agent automatisch das Profil und ordnet es in die passende Unterkategorie ein.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(displayedProfiles) { prof ->
                DeviceSummaryCard(
                    profile = prof,
                    onClick = { onSelectDevice(prof.fingerprintKey) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DeviceSummaryCard(
    profile: DeviceProfileEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PrimaryBlueContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = profile.manufacturer,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (profile.subcategory.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B2A38))
                            .border(0.8.dp, PrimaryBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = profile.subcategory,
                            color = PrimaryBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                if (profile.releaseYear.isNotBlank()) {
                    Text(
                        text = profile.releaseYear,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = profile.modelName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Typ/Modell: ${profile.modelNumber} • ${profile.category}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Dossier öffnen",
            tint = PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )
    }
}

// =============================================================================
// LEVEL 2: COMPREHENSIVE DEVICE PROFILE / DOSSIER
// =============================================================================

@Composable
fun DeviceProfileView(
    profile: DeviceProfile,
    onBack: () -> Unit,
    onSelectComponent: (DeviceComponent) -> Unit,
    onStartDiagnosis: (DeviceProfile) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Bauteile & Lösungen", "Spezifikationen", "Software & Fehler")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Navigation bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${profile.manufacturer} ${profile.modelName}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Geräte-Dossier & Reparaturdatenbank",
                        color = AccentCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Device Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile.modelName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hersteller: ${profile.manufacturer} • Modell: ${profile.modelNumber}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryBlueContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = profile.category,
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (profile.subcategory.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = profile.subcategory,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onStartDiagnosis(profile) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recherche & Diagnose für dieses Gerät", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = PrimaryBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) PrimaryBlue else TextSecondary
                            )
                        }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // Components & Solutions
                item {
                    Text(
                        text = "Erfasste Baugruppen & Austauschanleitungen (${profile.components.size}):",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (profile.components.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Für dieses Modell wurden noch keine individuellen Bauteile erfasst. Starte eine Recherche, um Bauteile automatisch anzulernen!",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    items(profile.components) { comp ->
                        ComponentItemCard(
                            component = comp,
                            onClick = { onSelectComponent(comp) }
                        )
                    }
                }
            }

            1 -> {
                // Specifications
                item {
                    Text(
                        text = "Technische Spezifikationen & Bauart:",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (profile.specifications.isEmpty()) {
                    item {
                        Text("Keine Spezifikationen hinterlegt.", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    items(profile.specifications) { spec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(0.45f)) {
                                Text(
                                    text = spec.category,
                                    color = PrimaryBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = spec.label,
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = spec.value,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.55f)
                            )
                        }
                    }
                }
            }

            2 -> {
                // Software & Error Codes
                if (profile.softwareProcedures.isNotEmpty()) {
                    item {
                        Text(
                            text = "Software, Reset & Diagnose-Kombinationen:",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(profile.softwareProcedures) { proc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = proc,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                if (profile.errorCodes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bekannte Fehlercodes & Lösungen:",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(profile.errorCodes) { err ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF332025))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = err.code,
                                        color = DangerRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = err.description,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ursache: ${err.cause}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lösung: ${err.solution}",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
fun ComponentItemCard(
    component: DeviceComponent,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.componentName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${component.componentCategory} • ${component.partNumberOrSpec}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val badgeColor = when (component.difficulty) {
                    "Einfach" -> SuccessGreen
                    "Mittel" -> WarningAmber
                    else -> DangerRed
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = component.difficulty,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Anleitung",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Metadata preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "⏱ Dauer ca. ${component.estimatedTimeMinutes} Min. • ${component.repairSteps.size} Schritte",
                color = TextMuted,
                fontSize = 11.sp
            )
            if (component.tools.isNotEmpty()) {
                Text(
                    text = "🛠 ${component.tools.size} Werkzeuge",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// =============================================================================
// LEVEL 3: DETAILED COMPONENT REPLACEMENT GUIDE
// =============================================================================

@Composable
fun ComponentDetailView(
    component: DeviceComponent,
    deviceProfile: DeviceProfile?,
    onBack: () -> Unit,
    onStartRepair: (DeviceComponent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Navigation bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = component.componentName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deviceProfile?.let { "${it.manufacturer} ${it.modelName}" } ?: "Bauteil-Reparatur",
                        color = AccentCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Overview Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = component.componentName,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = component.partNumberOrSpec,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryBlueContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⏱ ${component.estimatedTimeMinutes} Min",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (component.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hinweis: ${component.notes}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onStartRepair(component) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Interaktive Schritt-Reparatur starten", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // RISKS & WARNINGS
        if (component.warnings.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A1C12))
                        .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warnung",
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wichtige Risiken & Sicherheitshinweise:",
                            color = WarningAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    component.warnings.forEach { warning ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("• ", color = WarningAmber, fontSize = 13.sp)
                            Text(
                                text = warning,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // REQUIRED TOOLS
        if (component.tools.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hardware,
                            contentDescription = "Werkzeuge",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nötige Werkzeuge & Hilfsmittel:",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    component.tools.forEach { tool ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tool,
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // STEP-BY-STEP REPLACEMENT GUIDE
        item {
            Text(
                text = "Schritt-für-Schritt Austausch-Anleitung (${component.repairSteps.size} Schritte):",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(component.repairSteps) { step ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = step.stepNumber.toString(),
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = step.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                if (!step.caution.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E2211))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Achtung",
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = step.caution,
                            color = WarningAmber,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =============================================================================
// HELPER FUNCTIONS
// =============================================================================

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
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
}

fun getNormalizedCategoryName(category: RepairCategory): String {
    return when (category.id) {
        "smartphone" -> "Smartphones"
        "laptop" -> "Laptops & Notebooks"
        "tablet" -> "Tablets"
        "haushalt" -> "Haushaltsgeräte"
        "kaffeemaschine" -> "Haushaltsgeräte"
        "gaming" -> "Gaming & Konsolen"
        "audio" -> "Audio & Hi-Fi"
        "fahrrad" -> "Fahrrad & E-Bike"
        "fahrzeug" -> "Fahrzeuge & KFZ"
        "pc_build" -> "PC-Bau & Hardware"
        "maker_robotics" -> "Robotik & Maker"
        else -> category.title.replace("\n", " ").trim()
    }
}

fun isProfileInCategory(profileCategory: String, categoryNameOrId: String): Boolean {
    val normTarget = categoryNameOrId.trim().lowercase()
    val profCat = profileCategory.trim().lowercase()
    if (profCat == normTarget) return true

    return when {
        normTarget.contains("smartphone") || normTarget == "smartphone" ->
            profCat.contains("smartphone") || profCat.contains("handy") || profCat.contains("phone")
        normTarget.contains("laptop") || normTarget.contains("notebook") || normTarget == "laptop" ->
            (profCat.contains("laptop") || profCat.contains("notebook") || profCat.contains("macbook")) && !profCat.contains("pc-bau") && !profCat.contains("hardware")
        normTarget.contains("tablet") || normTarget == "tablet" ->
            profCat.contains("tablet") || profCat.contains("ipad")
        normTarget.contains("haushalt") || normTarget.contains("kaffee") || normTarget == "haushalt" || normTarget == "kaffeemaschine" ->
            profCat.contains("haushalt") || profCat.contains("kaffee")
        normTarget.contains("gaming") || normTarget.contains("konsole") || normTarget == "gaming" ->
            profCat.contains("gaming") || profCat.contains("konsole")
        normTarget.contains("audio") || normTarget.contains("hi-fi") || normTarget == "audio" ->
            profCat.contains("audio") || profCat.contains("hi-fi") || profCat.contains("kopfhörer")
        normTarget.contains("fahrrad") || normTarget.contains("bike") || normTarget == "fahrrad" ->
            profCat.contains("fahrrad") || profCat.contains("bike") || profCat.contains("e-bike")
        normTarget.contains("fahrzeug") || normTarget.contains("kfz") || normTarget.contains("auto") || normTarget == "fahrzeug" ->
            profCat.contains("fahrzeug") || profCat.contains("kfz") || profCat.contains("auto")
        normTarget.contains("pc-bau") || normTarget.contains("hardware") || normTarget == "pc_build" ->
            profCat.contains("pc-bau") || profCat.contains("hardware")
        normTarget.contains("maker") || normTarget.contains("robotik") || normTarget.contains("robotics") || normTarget == "maker_robotics" ->
            profCat.contains("maker") || profCat.contains("robotik") || profCat.contains("robotics")
        else -> profCat.contains(normTarget)
    }
}
