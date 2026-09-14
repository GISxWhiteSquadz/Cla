package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.llm.GeminiMessage
import com.example.data.llm.LocalGGUFEngine
import com.example.data.local.OfficeDocumentEntity
import com.example.data.local.PartComparisonEntity
import com.example.data.local.RepairDatabase
import com.example.data.local.RepairHistoryEntity
import com.example.data.local.RepairProjectEntity
import com.example.data.local.RepairSkillPackEntity
import com.example.data.local.RepairSkillPacksCatalog
import com.example.data.local.SavedManualEntity
import com.example.data.preferences.AppSettings
import com.example.data.repository.RepairRepository
import com.example.model.ChatMessage
import com.example.model.DiagnosisResult
import com.example.model.DiagnosticProcedureItem
import com.example.model.DiagnosticReport
import com.example.model.GGUFModelInfo
import com.example.model.MessageSender
import com.example.model.OfficeLineItem
import com.example.model.OfficeTemplates
import com.example.model.RepairCategory
import com.example.model.RepairSkillPack
import com.example.model.RepairTemplateItem
import com.example.model.SafetyInstructionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class RepairViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RepairDatabase.getInstance(application)
    private val settings = AppSettings(application)
    private val ggufEngine = LocalGGUFEngine(application)
    private val repository = RepairRepository(db.repairDao(), ggufEngine, settings)

    // Navigation Tab (0: Start, 1: Kategorien, 2: Projekte, 3: Verlauf, 4: Profil)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // History and Projects from Room
    val historyList: StateFlow<List<RepairHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectsList: StateFlow<List<RepairProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedManuals: StateFlow<List<SavedManualEntity>> = repository.allManuals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Office Documents & Part Comparisons
    val officeDocuments: StateFlow<List<OfficeDocumentEntity>> = repository.allOfficeDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val partComparisons: StateFlow<List<PartComparisonEntity>> = repository.allPartComparisons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Office & Project Sub-Tab (0: Projekte & Werkbank, 1: Office & Aufträge, 2: Dokumenten-Editor & PDF, 3: Budget & Teile-Vergleich, 4: Dateiarchiv)
    private val _selectedOfficeSubTab = MutableStateFlow(0)
    val selectedOfficeSubTab: StateFlow<Int> = _selectedOfficeSubTab.asStateFlow()

    private val _editingDocument = MutableStateFlow<OfficeDocumentEntity?>(null)
    val editingDocument: StateFlow<OfficeDocumentEntity?> = _editingDocument.asStateFlow()

    private val _selectedArchiveFolder = MutableStateFlow("Alle")
    val selectedArchiveFolder: StateFlow<String> = _selectedArchiveFolder.asStateFlow()

    private val _archiveSearchQuery = MutableStateFlow("")
    val archiveSearchQuery: StateFlow<String> = _archiveSearchQuery.asStateFlow()

    // Settings
    val selectedModelId: StateFlow<String> = settings.selectedModelId
    val selectedProjector: StateFlow<String> = settings.selectedProjector
    val webResearchAllowed: StateFlow<Boolean> = settings.webResearchAllowed
    val availableModels: List<GGUFModelInfo> = repository.availableGGUFModels
    val availableProjectors: List<String> = repository.availableProjectors
    val categories: List<RepairCategory> = repository.categories

    // Active Diagnosis
    private val _activeDiagnosis = MutableStateFlow<DiagnosisResult?>(null)
    val activeDiagnosis: StateFlow<DiagnosisResult?> = _activeDiagnosis.asStateFlow()

    // Device Catalog State & Navigation
    val allDeviceProfiles: StateFlow<List<com.example.data.local.DeviceProfileEntity>> = repository.allDeviceProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _selectedManufacturer = MutableStateFlow<String?>(null)
    val selectedManufacturer: StateFlow<String?> = _selectedManufacturer.asStateFlow()

    private val _selectedDeviceProfile = MutableStateFlow<com.example.model.DeviceProfile?>(null)
    val selectedDeviceProfile: StateFlow<com.example.model.DeviceProfile?> = _selectedDeviceProfile.asStateFlow()

    private val _selectedComponentDetail = MutableStateFlow<com.example.model.DeviceComponent?>(null)
    val selectedComponentDetail: StateFlow<com.example.model.DeviceComponent?> = _selectedComponentDetail.asStateFlow()

    private val _catalogSearchQuery = MutableStateFlow("")
    val catalogSearchQuery: StateFlow<String> = _catalogSearchQuery.asStateFlow()

    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    // Chat in Diagnosis
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAgentThinking = MutableStateFlow(false)
    val isAgentThinking: StateFlow<Boolean> = _isAgentThinking.asStateFlow()

    // Diagnostics report dialog
    private val _diagnosticReport = MutableStateFlow<DiagnosticReport?>(null)
    val diagnosticReport: StateFlow<DiagnosticReport?> = _diagnosticReport.asStateFlow()

    private val _isLoadingDiagnostics = MutableStateFlow(false)
    val isLoadingDiagnostics: StateFlow<Boolean> = _isLoadingDiagnostics.asStateFlow()

    // Interactive Workbench & Diagnostic Suite
    private val _showWorkbench = MutableStateFlow(false)
    val showWorkbench: StateFlow<Boolean> = _showWorkbench.asStateFlow()

    // Workbench Active Requested Device & Component State
    private val _workbenchActiveProfile = MutableStateFlow<com.example.model.DeviceProfile?>(null)
    val workbenchActiveProfile: StateFlow<com.example.model.DeviceProfile?> = _workbenchActiveProfile.asStateFlow()

    private val _workbenchSelectedComponent = MutableStateFlow<com.example.model.DeviceComponent?>(null)
    val workbenchSelectedComponent: StateFlow<com.example.model.DeviceComponent?> = _workbenchSelectedComponent.asStateFlow()

    private val _workbenchSoftwareIssue = MutableStateFlow<com.example.model.ErrorCodeItem?>(null)
    val workbenchSoftwareIssue: StateFlow<com.example.model.ErrorCodeItem?> = _workbenchSoftwareIssue.asStateFlow()

    private val _isWorkbenchLoading = MutableStateFlow(false)
    val isWorkbenchLoading: StateFlow<Boolean> = _isWorkbenchLoading.asStateFlow()

    private val _workbenchChatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            id = "wb_init_1",
            sender = MessageSender.AGENT,
            text = "Hallo! Ich bin dein Live-Hardware-Assistent für die Werkbank. Nenne mir ein Gerät oder stelle gezielte Fragen zu Pin-Belegungen, Messwerten, Schrauben und Bauteilen.",
            modelTag = "Gemini 3.5 Flash • Hardware-Meister"
        )
    ))
    val workbenchChatMessages: StateFlow<List<ChatMessage>> = _workbenchChatMessages.asStateFlow()

    // Beginner Guide & Technical Glossary
    private val _showGlossary = MutableStateFlow(false)
    val showGlossary: StateFlow<Boolean> = _showGlossary.asStateFlow()

    // App Language (DE / EN)
    val language: StateFlow<String> = settings.language

    // Legal Safety Disclaimer
    val disclaimerAccepted: StateFlow<Boolean> = settings.disclaimerAccepted
    private val _showDisclaimerModal = MutableStateFlow(false)
    val showDisclaimerModal: StateFlow<Boolean> = _showDisclaimerModal.asStateFlow()

    // Launch Splash Animation
    private val _isSplashVisible = MutableStateFlow(true)
    val isSplashVisible: StateFlow<Boolean> = _isSplashVisible.asStateFlow()

    fun dismissSplash() {
        _isSplashVisible.value = false
    }

    fun openGlossary() {
        _showGlossary.value = true
    }

    fun closeGlossary() {
        _showGlossary.value = false
    }

    fun setLanguage(lang: String) {
        settings.setLanguage(lang)
    }

    fun acceptDisclaimer() {
        settings.setDisclaimerAccepted(true)
        _showDisclaimerModal.value = false
    }

    fun openDisclaimerModal() {
        _showDisclaimerModal.value = true
    }

    fun closeDisclaimerModal() {
        _showDisclaimerModal.value = false
    }

    fun openWorkbench() {
        if (_workbenchActiveProfile.value == null) {
            if (_selectedDeviceProfile.value != null) {
                _workbenchActiveProfile.value = _selectedDeviceProfile.value
                _workbenchSelectedComponent.value = _selectedDeviceProfile.value?.components?.firstOrNull()
            } else if (_activeDiagnosis.value != null) {
                val fpKey = _activeDiagnosis.value!!.deviceFingerprint.normalizedKey
                loadDeviceByFingerprintToWorkbench(fpKey)
            } else {
                viewModelScope.launch {
                    val all = db.repairDao().getAllDeviceProfilesSync()
                    if (all.isNotEmpty()) {
                        val first = all.first()
                        val comps = db.repairDao().getComponentsForDeviceSync(first.fingerprintKey)
                        _workbenchActiveProfile.value = parseDeviceProfile(first, comps)
                        _workbenchSelectedComponent.value = _workbenchActiveProfile.value?.components?.firstOrNull()
                    }
                }
            }
        }
        _showWorkbench.value = true
    }

    fun closeWorkbench() {
        _showWorkbench.value = false
    }

    fun clearWorkbenchDevice() {
        _workbenchActiveProfile.value = null
        _workbenchSelectedComponent.value = null
        _workbenchSoftwareIssue.value = null
        _workbenchChatMessages.value = _workbenchChatMessages.value + ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.SYSTEM,
            text = "Werkbank geleert. Die Werkbank ist nun frei für ein neues Reparaturprojekt."
        )
    }

    fun selectWorkbenchComponent(component: com.example.model.DeviceComponent?) {
        _workbenchSelectedComponent.value = component
        _workbenchSoftwareIssue.value = null
    }

    fun selectWorkbenchSoftwareIssue(issue: com.example.model.ErrorCodeItem?) {
        _workbenchSoftwareIssue.value = issue
        _workbenchSelectedComponent.value = null
    }

    fun loadProfileToWorkbench(profile: com.example.model.DeviceProfile) {
        _workbenchActiveProfile.value = profile
        _workbenchSelectedComponent.value = profile.components.firstOrNull()
        _workbenchSoftwareIssue.value = null
        _workbenchChatMessages.value = _workbenchChatMessages.value + ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.SYSTEM,
            text = "Gerät '${profile.manufacturer} ${profile.modelName}' (${profile.category} > ${profile.subcategory}) auf die Werkbank geladen."
        )
    }

    fun loadDeviceByFingerprintToWorkbench(fingerprintKey: String) {
        viewModelScope.launch {
            _isWorkbenchLoading.value = true
            try {
                val profileEntity = repository.getDeviceProfile(fingerprintKey)
                if (profileEntity != null) {
                    var components = db.repairDao().getComponentsForDeviceSync(fingerprintKey)
                    if (components.size <= 1) {
                        enrichComponentsForDevice(profileEntity.fingerprintKey, profileEntity.category, profileEntity.manufacturer, profileEntity.modelName)
                        components = db.repairDao().getComponentsForDeviceSync(fingerprintKey)
                    }
                    val profile = parseDeviceProfile(profileEntity, components)
                    _workbenchActiveProfile.value = profile
                    _workbenchSelectedComponent.value = profile.components.firstOrNull()
                    _workbenchSoftwareIssue.value = null
                    _workbenchChatMessages.value = _workbenchChatMessages.value + ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.SYSTEM,
                        text = "Gerät '${profile.manufacturer} ${profile.modelName}' (${profile.category} > ${profile.subcategory}) auf die Werkbank geladen."
                    )
                }
            } finally {
                _isWorkbenchLoading.value = false
            }
        }
    }

    fun requestDeviceForWorkbench(query: String, categoryHint: String = "") {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isWorkbenchLoading.value = true
            try {
                val fp = repository.parseFingerprint(query, categoryHint)
                var profileEntity = repository.getDeviceProfile(fp.normalizedKey)
                if (profileEntity == null) {
                    val all = db.repairDao().getAllDeviceProfilesSync()
                    profileEntity = all.find {
                        it.modelName.contains(query, ignoreCase = true) ||
                        query.contains(it.modelName, ignoreCase = true) ||
                        it.fingerprintKey.contains(fp.normalizedKey, ignoreCase = true)
                    }
                }

                if (profileEntity != null) {
                    var components = db.repairDao().getComponentsForDeviceSync(profileEntity.fingerprintKey)
                    if (components.size <= 1) {
                        enrichComponentsForDevice(profileEntity.fingerprintKey, profileEntity.category, profileEntity.manufacturer, profileEntity.modelName)
                        components = db.repairDao().getComponentsForDeviceSync(profileEntity.fingerprintKey)
                    }
                    val profile = parseDeviceProfile(profileEntity, components)
                    _workbenchActiveProfile.value = profile
                    _workbenchSelectedComponent.value = profile.components.firstOrNull()
                    _workbenchSoftwareIssue.value = null
                    _workbenchChatMessages.value = _workbenchChatMessages.value + ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.SYSTEM,
                        text = "Gerät '${profile.manufacturer} ${profile.modelName}' (${profile.category} > ${profile.subcategory}) auf die Werkbank geladen."
                    )
                } else {
                    val diag = repository.diagnose(
                        deviceQuery = query,
                        categoryHint = categoryHint,
                        useSearchGrounding = settings.webResearchAllowed.value
                    )
                    val newEntity = repository.getDeviceProfile(diag.deviceFingerprint.normalizedKey)
                    if (newEntity != null) {
                        enrichComponentsForDevice(newEntity.fingerprintKey, newEntity.category, newEntity.manufacturer, newEntity.modelName)
                        val components = db.repairDao().getComponentsForDeviceSync(newEntity.fingerprintKey)
                        val profile = parseDeviceProfile(newEntity, components)
                        _workbenchActiveProfile.value = profile
                        _workbenchSelectedComponent.value = profile.components.firstOrNull()
                        _workbenchSoftwareIssue.value = null
                        _workbenchChatMessages.value = _workbenchChatMessages.value + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = MessageSender.AGENT,
                            text = "Neues Gerät '${profile.manufacturer} ${profile.modelName}' recherchiert, in der Datenbank unter '${profile.category} > ${profile.subcategory}' dauerhaft gesichert und auf die Werkbank geladen."
                        )
                    }
                }
            } catch (e: Exception) {
                _workbenchChatMessages.value = _workbenchChatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.SYSTEM,
                    text = "Hinweis: ${e.message}"
                )
            } finally {
                _isWorkbenchLoading.value = false
            }
        }
    }

    fun sendWorkbenchChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = text
        )
        _workbenchChatMessages.value = _workbenchChatMessages.value + userMsg

        val qLower = text.lowercase().trim()
        val loadKeywords = listOf("lade ", "öffne ", "repariere ", "gerät ", "untersuche ", "prüfe ")
        val isExplicitLoad = loadKeywords.any { qLower.startsWith(it) }

        if (isExplicitLoad) {
            val queryClean = text.replace(Regex("(?i)^(lade|öffne|repariere|untersuche|prüfe|gerät)\\s+"), "").trim()
            requestDeviceForWorkbench(queryClean.ifBlank { text })
            return
        }

        viewModelScope.launch {
            val active = _workbenchActiveProfile.value
            val contextInfo = if (active != null) {
                "Aktives Gerät auf der Werkbank: ${active.manufacturer} ${active.modelName} (${active.category} > ${active.subcategory}, Typ: ${active.modelNumber}).\n" +
                "Verfügbare Baugruppen: " + active.components.joinToString(", ") { it.componentName }
            } else {
                "Kein Gerät aktuell auf der Werkbank geladen."
            }

            val reply: String = try {
                if (settings.webResearchAllowed.value) {
                    val resp = com.example.data.llm.GeminiCloudProvider.queryWithSearchGrounding(
                        prompt = "$contextInfo\n\nTechniker-Frage auf der Werkbank: $text\n\nAntworte präzise mit technischen Sollwerten, Schraubentypen, Sicherheitsregeln oder Messwerten.",
                        systemInstruction = "Du bist der leitende Hardware- und Reparaturmeister an der Werkbank. Antworte auf Deutsch präzise, technisch exakt und strukturiert."
                    )
                    resp.text
                } else {
                    val resp = com.example.data.llm.GeminiCloudProvider.queryLowLatency(
                        prompt = "$contextInfo\n\nTechniker-Frage auf der Werkbank: $text\n\nAntworte mit präzisen technischen Sollwerten, Schrauben und Sicherheitsregeln als Reparaturmeister."
                    )
                    resp.ifBlank { "Bitte prüfe die entsprechenden Kontakte mit dem Multimeter und beachte die Sicherheitsregeln." }
                }
            } catch (e: Exception) {
                "Technischer Hinweis: Bei Arbeiten an $text stets Spannungsfreiheit sicherstellen und ESD-Schutz beachten."
            }

            _workbenchChatMessages.value = _workbenchChatMessages.value + ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.AGENT,
                text = reply,
                modelTag = "Gemini 3.5 Flash"
            )
        }
    }

    private suspend fun enrichComponentsForDevice(
        fingerprintKey: String,
        category: String,
        mfr: String,
        model: String
    ) {
        val existing = db.repairDao().getComponentsForDeviceSync(fingerprintKey)
        if (existing.size > 1) return

        val newComps = mutableListOf<com.example.data.local.DeviceComponentEntity>()
        when (category) {
            "Smartphones", "Tablets" -> {
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Display & Touchscreen Einheit",
                        componentCategory = "Optik & Touch",
                        partNumberOrSpec = "$mfr $model OLED/LCD Panel mit Digitizer",
                        difficulty = "Mittel",
                        estimatedTimeMinutes = 35,
                        toolsJson = JSONArray(listOf("Saugnapf", "Pentalobe / Torx", "Heizmatte 80°C", "Plektrum", "Isopropanol 99.9%")).toString(),
                        warningsJson = JSONArray(listOf("Display nicht über 90° aufklappen - Flexkabel-Bruchgefahr!", "Akku zwingend vor Display-Kabel trennen!")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Gehäuseschrauben lösen").put("description", "Untere Gehäuseschrauben entfernen und sicher im Magnethalter ablegen."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Klebefalz erwärmen").put("description", "Kanten auf 75-80°C erwärmen, um den Dichtungskleber zu erweichen."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Display vorsichtig anheben").put("description", "Mit Saugnapf und Plektrum Displayspalt öffnen, Flexkabel-Laufrichtung beachten."))
                            put(JSONObject().put("stepNumber", 4).put("title", "Akkukontakt trennen & neues Panel anschließen").put("description", "Spannungsfreiheit herstellen, Panel-Konnektoren verbinden und Bildtest durchführen."))
                        }.toString()
                    )
                )
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Akkumodul & Ladeelektronik",
                        componentCategory = "Energie & Power",
                        partNumberOrSpec = "$mfr $model Li-Ion Batterie mit BMS",
                        difficulty = "Mittel",
                        estimatedTimeMinutes = 25,
                        toolsJson = JSONArray(listOf("Präzisionsschraubendreher", "ESD-Spatel", "Zuglaschen-Pinzette")).toString(),
                        warningsJson = JSONArray(listOf("Niemals mit metallischen Werkzeugen in den Akku stechen (Brandgefahr)!", "Klebstreifen flach im 30°-Winkel ziehen.")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Gerät öffnen & Akku freilegen").put("description", "Schutzbleche über dem Akkukonnektor abschrauben."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Klebstreifen herausziehen").put("description", "Zuglaschen langsam und gleichmäßig ohne Abreißen ziehen."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Neuen Akku einsetzen & kalibrieren").put("description", "Akku fixieren, Konnektor aufstecken und Ladezyklus testen."))
                        }.toString()
                    )
                )
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Ladebuchse & Flex-Subboard",
                        componentCategory = "Konnektivität & Port",
                        partNumberOrSpec = "$mfr $model USB-C / Lightning Port Assembly",
                        difficulty = "Mittel",
                        estimatedTimeMinutes = 30,
                        toolsJson = JSONArray(listOf("Kreuzschlitz / Torx", "Pinzette", "Heißluft 100°C")).toString(),
                        warningsJson = JSONArray(listOf("Mikrofondichtungen nicht beschädigen!", "Antennen-Koaxialkabel vorsichtig aushängen.")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Lautsprecherblock entnehmen").put("description", "Unteren Resonanzkörper abschrauben."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Antennenkabel trennen").put("description", "Koaxialstecker mit Hebelwerkzeug nach oben lösen."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Port tauschen").put("description", "Verklebtes Subboard anheben und Neuteil passgenau einsetzen."))
                        }.toString()
                    )
                )
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Logic Board & PMIC",
                        componentCategory = "Hauptplatine & Chips",
                        partNumberOrSpec = "$mfr $model Mainboard mit SoC & Powermanagement",
                        difficulty = "Experte",
                        estimatedTimeMinutes = 60,
                        toolsJson = JSONArray(listOf("Multimeter", "Mikroskop", "Standoff-Dreher", "Wärmebildkamera")).toString(),
                        warningsJson = JSONArray(listOf("ESD-Schutzmatte & Erdungsarmband zwingend erforderlich!", "Keine Schrauben verwechseln.")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Platine isolieren").put("description", "Kameras, Antennen und Verbindungskabel abstecken."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Diodenmessung an Hauptversorgung").put("description", "Prüfpunkt VDD_MAIN gegen Masse messen: Sollwert > 0.350V."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Kurzschluss beseitigen").put("description", "Defekten Filterkondensator lokalisieren und erneuern."))
                        }.toString()
                    )
                )
            }
            "Gaming & Konsolen" -> {
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "HDMI 2.1 Video-Port",
                        componentCategory = "Video & Schnittstelle",
                        partNumberOrSpec = "$mfr $model HDMI-Buchse mit verstärkten Haltebeinchen",
                        difficulty = "Schwierig",
                        estimatedTimeMinutes = 45,
                        toolsJson = JSONArray(listOf("Lötstation", "Heißluft 380°C", "Flussmittel", "Entlötlitze")).toString(),
                        warningsJson = JSONArray(listOf("Benachbarte SMD-Kondensatoren mit Kapton-Band abkleben!")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Board freilegen").put("description", "Gehäuseschalen und Abschirmbleche demontieren."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Defekten Port entlöten").put("description", "Flussmittel auftragen, Heißluft von unten ansetzen und Port abheben."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Pads säubern & Neuteil verlöten").put("description", "Lötpads mit Entlötlitze ebnen und neuen Port mikroskopisch prüfen."))
                        }.toString()
                    )
                )
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Kühlsystem & Radiallüfter",
                        componentCategory = "Kühlung & Thermik",
                        partNumberOrSpec = "$mfr $model Hochleistungslüfter & Heatsink",
                        difficulty = "Einfach",
                        estimatedTimeMinutes = 25,
                        toolsJson = JSONArray(listOf("Torx Security T8/T9", "Pinsel", "Wärmeleitpaste")).toString(),
                        warningsJson = JSONArray(listOf("Lüfterflügel vorsichtig behandeln, Unwucht vermeiden.")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Lüftergitter abnehmen").put("description", "Sicherheitsschrauben lösen und Lüfterkabel trennen."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Kühlrippen reinigen").put("description", "Staubfangkanäle absaugen und Lüfterlager prüfen."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Wärmeleitpaste erneuern").put("description", "Kühlkörper reinigen und Paste frisch auftragen."))
                        }.toString()
                    )
                )
            }
            "PC-Bau & Hardware", "Laptops & Notebooks" -> {
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Kühler & Wärmeleitpaste",
                        componentCategory = "Thermik & Kühlung",
                        partNumberOrSpec = "High-Performance Wärmeleitpaste & Heatsink",
                        difficulty = "Einfach",
                        estimatedTimeMinutes = 20,
                        toolsJson = JSONArray(listOf("Kreuzschlitz PH2", "Isopropanol 99.9%", "Papiertücher")).toString(),
                        warningsJson = JSONArray(listOf("Schutzfolie am Kühlerboden vor Montage zwingend abziehen!")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Alte Paste entfernen").put("description", "CPU-Heatspreader und Kühler mit IPA reinigen."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Neue Paste auftragen").put("description", "Erbsengroßen Punkt oder X-Muster mittig platzieren."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Über Kreuz anziehen").put("description", "Schrauben gleichmäßig über Kreuz festziehen."))
                        }.toString()
                    )
                )
            }
            "Haushaltsgeräte" -> {
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Brüheinheit / Pumpe / Motor",
                        componentCategory = "Mechanik & Hydraulik",
                        partNumberOrSpec = "$mfr $model Mechanische Antriebseinheit",
                        difficulty = "Mittel",
                        estimatedTimeMinutes = 40,
                        toolsJson = JSONArray(listOf("Torx T15/T20", "Silikonfett O-Ringe", "Entkalker")).toString(),
                        warningsJson = JSONArray(listOf("Netzstecker ziehen! Schläuche stehen unter Druck (bis 15 bar).")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Wartungsklappe öffnen").put("description", "Brühgruppe oder Pumpenmodul entriegeln und entnehmen."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Dichtungen erneuern").put("description", "O-Ringe an den Druckschläuchen tauschen und fetten."))
                            put(JSONObject().put("stepNumber", 3).put("title", "Druckprüfung durchführen").put("description", "Probelauf starten und auf Dichtigkeit kontrollieren."))
                        }.toString()
                    )
                )
            }
            else -> {
                newComps.add(
                    com.example.data.local.DeviceComponentEntity(
                        deviceFingerprintKey = fingerprintKey,
                        componentName = "Hauptplatine & Spannungsversorgung",
                        componentCategory = "Elektronik & Strom",
                        partNumberOrSpec = "$mfr $model Steuerplatine",
                        difficulty = "Mittel",
                        estimatedTimeMinutes = 35,
                        toolsJson = JSONArray(listOf("Multimeter", "Schraubendreher")).toString(),
                        warningsJson = JSONArray(listOf("Netzstecker trennen vor Arbeiten an offenen Kontakten!")).toString(),
                        stepsJson = JSONArray().apply {
                            put(JSONObject().put("stepNumber", 1).put("title", "Spannungsfreiheit prüfen").put("description", "Multimeter auf AC/DC stellen und Eingang messen."))
                            put(JSONObject().put("stepNumber", 2).put("title", "Sichtprüfung der Platine").put("description", "Auf geblähte Elkos, Brandspuren oder kalte Lötstellen prüfen."))
                        }.toString()
                    )
                )
            }
        }

        if (newComps.isNotEmpty()) {
            db.repairDao().insertAllComponents(newComps)
        }
    }

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun startDiagnosis(
        deviceQuery: String,
        categoryHint: String = "",
        imageUri: String? = null
    ) {
        if (deviceQuery.isBlank()) return
        viewModelScope.launch {
            _isDiagnosing.value = true
            try {
                val result = repository.diagnose(
                    deviceQuery = deviceQuery,
                    categoryHint = categoryHint,
                    imageUri = imageUri,
                    useSearchGrounding = settings.webResearchAllowed.value
                )
                _activeDiagnosis.value = result
                _chatMessages.value = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.SYSTEM,
                        text = "Geräte-Fingerprint: ${result.deviceFingerprint.normalizedKey}\nModell: ${result.modelUsed}\nSicherheitswarnungen beachten!"
                    ),
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.AGENT,
                        text = "Hallo! Ich habe die Reparaturanleitung für ${result.deviceFingerprint.displayTitle} geladen. Hake die Schritte ab oder stelle mir Fragen zu Messwerten, Bauteilen und Werkzeugen."
                    )
                )
            } finally {
                _isDiagnosing.value = false
            }
        }
    }

    fun toggleStepCompleted(stepNumber: Int) {
        val current = _activeDiagnosis.value ?: return
        val updatedSteps = current.steps.map { step ->
            if (step.stepNumber == stepNumber) {
                step.copy(isCompleted = !step.isCompleted)
            } else step
        }
        _activeDiagnosis.value = current.copy(steps = updatedSteps)
    }

    fun toggleToolChecked(toolName: String) {
        val current = _activeDiagnosis.value ?: return
        val updatedTools = current.requiredTools.map { tool ->
            if (tool.name == toolName) {
                tool.copy(isChecked = !tool.isChecked)
            } else tool
        }
        _activeDiagnosis.value = current.copy(requiredTools = updatedTools)
    }

    // Global Gemini Chatbot State
    private val _showGeminiChatModal = MutableStateFlow(false)
    val showGeminiChatModal: StateFlow<Boolean> = _showGeminiChatModal.asStateFlow()

    private val _globalChatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            id = "welcome_ai",
            sender = MessageSender.AGENT,
            text = "Hallo! Ich bin dein Gemini KI-Hardware-Assistent. Ausgestattet mit Google Search Grounding recherchiere ich in Echtzeit Schaltpläne, Drehmomente, Pinbelegungen und aktuelle Bauteilspezifikationen.",
            searchQueries = listOf("Google Search Grounding aktiv"),
            webSources = listOf(
                com.example.model.GroundingSource("Google Live Search System", "https://google.com", "google.com")
            ),
            modelTag = "Gemini 3.5 Flash • Live Grounding"
        )
    ))
    val globalChatMessages: StateFlow<List<ChatMessage>> = _globalChatMessages.asStateFlow()

    private val _isGlobalChatThinking = MutableStateFlow(false)
    val isGlobalChatThinking: StateFlow<Boolean> = _isGlobalChatThinking.asStateFlow()

    private val _selectedChatbotRole = MutableStateFlow("Hardware-Meister")
    val selectedChatbotRole: StateFlow<String> = _selectedChatbotRole.asStateFlow()

    private val _selectedChatbotModel = MutableStateFlow("gemini-3.5-flash")
    val selectedChatbotModel: StateFlow<String> = _selectedChatbotModel.asStateFlow()

    private val _chatUseSearchGrounding = MutableStateFlow(true)
    val chatUseSearchGrounding: StateFlow<Boolean> = _chatUseSearchGrounding.asStateFlow()

    fun openGeminiChat() {
        _showGeminiChatModal.value = true
    }

    fun closeGeminiChat() {
        _showGeminiChatModal.value = false
    }

    fun setChatbotRole(role: String) {
        _selectedChatbotRole.value = role
    }

    fun setChatbotModel(model: String) {
        _selectedChatbotModel.value = model
    }

    fun toggleChatSearchGrounding() {
        _chatUseSearchGrounding.value = !_chatUseSearchGrounding.value
    }

    fun clearGlobalChat() {
        _globalChatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.AGENT,
                text = "Chat zurückgesetzt. Wie kann ich dir bei deiner Reparatur oder PC-Planung helfen?",
                modelTag = _selectedChatbotModel.value
            )
        )
    }

    fun sendGlobalChatMessage(question: String) {
        if (question.isBlank()) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = question
        )
        _globalChatMessages.value = _globalChatMessages.value + userMsg

        viewModelScope.launch {
            _isGlobalChatThinking.value = true
            try {
                val history = _globalChatMessages.value
                    .filter { it.sender != MessageSender.SYSTEM }
                    .map { GeminiMessage(role = if (it.sender == MessageSender.USER) "user" else "model", text = it.text) }

                val roleInstruction = when (_selectedChatbotRole.value) {
                    "PC-Konfigurator" -> "Du bist ein erfahrener PC-Systembuilder. Analysiere Hardware-Komponenten, Bottlenecks, Kühlung, TDP-Dimensionierung und Kompatibilität. Nutze Google-Recherche für genaue Produktabmessungen und Benchmark-Daten."
                    "Werkstatt-Kalkulator" -> "Du bist Meister für Kostenvoranschläge und Reparaturkalkulation in einer Fachwerkstatt. Berate bei Ersatzteilpreisen, Reparaturdauer, typischem Werkstattaufwand und wirtschaftlicher Sinnhaftigkeit."
                    else -> "Du bist der CoreRepair Master AI Reparatur- & Hardware-Experte. Nutze Google-Recherche für exakte Daten, Pinbelegungen, Drehmomente und Bauteil-Spezifikationen."
                }

                val resp = repository.chatWithAgent(
                    history = history,
                    fingerprint = null,
                    roleSystemInstruction = roleInstruction,
                    useSearchGrounding = _chatUseSearchGrounding.value,
                    modelName = _selectedChatbotModel.value
                )

                val agentMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AGENT,
                    text = resp.text,
                    searchQueries = resp.searchQueries,
                    webSources = resp.sources,
                    modelTag = "${resp.modelName}${if (resp.isLiveSearch) " • Google Live" else ""}"
                )
                _globalChatMessages.value = _globalChatMessages.value + agentMsg
            } catch (e: Exception) {
                _globalChatMessages.value = _globalChatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AGENT,
                    text = "Hinweis zur Anfrage: ${e.localizedMessage ?: "Verbindung prüfen."}"
                )
            } finally {
                _isGlobalChatThinking.value = false
            }
        }
    }

    fun sendChatMessage(question: String) {
        val diagnosis = _activeDiagnosis.value ?: return
        if (question.isBlank()) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = question
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAgentThinking.value = true
            try {
                val history = _chatMessages.value
                    .filter { it.sender != MessageSender.SYSTEM }
                    .map { GeminiMessage(role = if (it.sender == MessageSender.USER) "user" else "model", text = it.text) }

                val roleInstruction = "Du bist der leitende Hardware-Diagnostiker für das Gerät ${diagnosis.deviceFingerprint.displayTitle}. Nutze Google Search Grounding für exakte Spezifikationen, Pinouts, Drehmomente und Baugruppen."

                val resp = repository.chatWithAgent(
                    history = history,
                    fingerprint = diagnosis.deviceFingerprint,
                    ragChunks = diagnosis.ragChunks,
                    roleSystemInstruction = roleInstruction,
                    useSearchGrounding = true,
                    modelName = "gemini-3.5-flash"
                )

                val agentMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AGENT,
                    text = resp.text,
                    searchQueries = resp.searchQueries,
                    webSources = resp.sources,
                    modelTag = "${resp.modelName}${if (resp.isLiveSearch) " • Google Live" else ""}"
                )
                _chatMessages.value = _chatMessages.value + agentMsg
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AGENT,
                    text = "Antwort-Fehler: ${e.localizedMessage}"
                )
            } finally {
                _isAgentThinking.value = false
            }
        }
    }

    fun closeDiagnosis() {
        _activeDiagnosis.value = null
        _chatMessages.value = emptyList()
    }

    fun loadDiagnosisFromHistory(history: RepairHistoryEntity) {
        val fingerprint = repository.parseFingerprint(history.deviceName, history.category)

        // Parse steps
        val steps = mutableListOf<com.example.model.RepairStep>()
        try {
            val arr = JSONArray(history.stepsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                steps.add(
                    com.example.model.RepairStep(
                        stepNumber = obj.getInt("stepNumber"),
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        caution = if (obj.has("caution") && !obj.isNull("caution")) obj.getString("caution").ifBlank { null } else null,
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }

        // Parse tools
        val tools = mutableListOf<com.example.model.ToolItem>()
        try {
            val arr = JSONArray(history.toolsNeededJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                tools.add(
                    com.example.model.ToolItem(
                        name = obj.getString("name"),
                        isRequired = obj.optBoolean("isRequired", true),
                        isChecked = obj.optBoolean("isChecked", false)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }

        // Parse warnings
        val warnings = mutableListOf<String>()
        try {
            val arr = JSONArray(history.safetyWarningsJson)
            for (i in 0 until arr.length()) {
                warnings.add(arr.getString(i))
            }
        } catch (e: Exception) {
            // fallback
        }

        val result = DiagnosisResult(
            deviceFingerprint = fingerprint,
            issueSummary = history.problemDescription,
            safetyWarnings = warnings,
            requiredTools = tools,
            rootCauseAnalysis = history.rootCauseAnalysis,
            steps = steps,
            ragChunks = emptyList(),
            modelUsed = history.modelUsed,
            isLocalInference = true
        )

        _activeDiagnosis.value = result
        _chatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.AGENT,
                text = "Recherche für ${history.deviceName} wiederhergestellt. Du kannst an deinen Fortschritten weiterarbeiten."
            )
        )
    }

    fun saveDiagnosisAsProject(notes: String = "") {
        val diagnosis = _activeDiagnosis.value ?: return
        viewModelScope.launch {
            repository.createProject(
                title = "${diagnosis.deviceFingerprint.displayTitle} - Reparatur",
                deviceModel = diagnosis.deviceFingerprint.displayTitle,
                category = diagnosis.deviceFingerprint.variant.ifBlank { "Allgemein" },
                notes = if (notes.isNotBlank()) notes else diagnosis.issueSummary
            )
            selectTab(2) // Jump to Projekte tab
        }
    }

    fun createNewProject(title: String, deviceModel: String, category: String, notes: String) {
        viewModelScope.launch {
            repository.createProject(title, deviceModel, category, notes)
        }
    }

    fun updateProjectChecklist(project: RepairProjectEntity, itemIndex: Int, done: Boolean) {
        viewModelScope.launch {
            try {
                val arr = JSONArray(project.checklistJson)
                val obj = arr.getJSONObject(itemIndex)
                obj.put("done", done)
                repository.updateProject(project.copy(checklistJson = arr.toString()))
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun addProjectChecklistItem(project: RepairProjectEntity, stepText: String) {
        if (stepText.isBlank()) return
        viewModelScope.launch {
            try {
                val arr = JSONArray(project.checklistJson)
                val newObj = org.json.JSONObject().apply {
                    put("text", stepText.trim())
                    put("done", false)
                }
                arr.put(newObj)
                repository.updateProject(project.copy(checklistJson = arr.toString()))
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun updateProjectStatus(project: RepairProjectEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateProject(project.copy(status = newStatus))
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    // Office & Project Sub-Tab Navigation
    fun selectOfficeSubTab(index: Int) {
        _selectedOfficeSubTab.value = index
    }

    fun setEditingDocument(doc: OfficeDocumentEntity?) {
        _editingDocument.value = doc
    }

    fun setArchiveFolder(folder: String) {
        _selectedArchiveFolder.value = folder
    }

    fun setArchiveSearchQuery(query: String) {
        _archiveSearchQuery.value = query
    }

    fun saveOfficeDocument(doc: OfficeDocumentEntity) {
        viewModelScope.launch {
            if (doc.id == 0L) {
                val newId = repository.insertOfficeDocument(doc)
                _editingDocument.value = doc.copy(id = newId)
            } else {
                repository.updateOfficeDocument(doc)
                _editingDocument.value = doc
            }
        }
    }

    fun createOfficeDocument(
        title: String,
        docType: String,
        categoryFolder: String,
        customerName: String,
        customerContact: String,
        deviceOrProject: String,
        content: String,
        lineItemsJson: String,
        subtotal: Double,
        taxRate: Double,
        total: Double,
        budgetTarget: Double = 0.0
    ) {
        viewModelScope.launch {
            val prefix = when (docType) {
                "Angebot" -> "ANG"
                "Rechnung" -> "REC"
                "Auftrag" -> "AUF"
                "Prüfprotokoll" -> "BER"
                "PC-Bau Plan" -> "PC"
                else -> "DOC"
            }
            val docNumber = "$prefix-2026-${(100..999).random()}"
            val doc = OfficeDocumentEntity(
                documentNumber = docNumber,
                title = title,
                docType = docType,
                categoryFolder = categoryFolder,
                customerName = customerName,
                customerContact = customerContact,
                deviceOrProject = deviceOrProject,
                status = if (docType == "Rechnung") "Offen / Versendet" else if (docType == "Auftrag") "In Bearbeitung" else "Entwurf",
                content = content,
                lineItemsJson = lineItemsJson,
                subtotalEuro = subtotal,
                taxRatePercent = taxRate,
                totalEuro = total,
                budgetTargetEuro = budgetTarget,
                fileSizeBytes = (content.length * 2 + 1024).toLong()
            )
            repository.insertOfficeDocument(doc)
            _editingDocument.value = doc
        }
    }

    fun convertOfferToInvoice(offer: OfficeDocumentEntity) {
        viewModelScope.launch {
            val invNumber = "REC-2026-${(100..999).random()}"
            val invoice = offer.copy(
                id = 0L,
                documentNumber = invNumber,
                title = "Rechnung: ${offer.title.removePrefix("Kostenvoranschlag: ").removePrefix("Angebot: ")}",
                docType = "Rechnung",
                categoryFolder = "Rechnungen",
                status = "Offen / Versendet",
                content = """
                    # Rechnung (erstellt aus Angebot ${offer.documentNumber})
                    **Kunde:** ${offer.customerName}
                    **Projekt/Gerät:** ${offer.deviceOrProject}
                    **Rechnungsdatum:** ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMANY).format(java.util.Date())}
                    
                    ${offer.content}
                """.trimIndent(),
                createdTimestamp = System.currentTimeMillis(),
                updatedTimestamp = System.currentTimeMillis()
            )
            val newId = repository.insertOfficeDocument(invoice)
            _editingDocument.value = invoice.copy(id = newId)
            _selectedOfficeSubTab.value = 2 // Switch to editor
        }
    }

    fun deleteOfficeDocument(id: Long) {
        viewModelScope.launch {
            repository.deleteOfficeDocument(id)
            if (_editingDocument.value?.id == id) {
                _editingDocument.value = null
            }
        }
    }

    fun createPartComparison(
        title: String,
        category: String,
        targetBudget: Double,
        partAName: String,
        partASpecs: String,
        partAPrice: Double,
        partAPros: String,
        partBName: String,
        partBSpecs: String,
        partBPrice: Double,
        partBPros: String,
        recommendation: String
    ) {
        viewModelScope.launch {
            val comparison = PartComparisonEntity(
                title = title,
                category = category,
                targetBudgetEuro = targetBudget,
                partAName = partAName,
                partASpecs = partASpecs,
                partAPriceEuro = partAPrice,
                partAPros = partAPros,
                partBName = partBName,
                partBSpecs = partBSpecs,
                partBPriceEuro = partBPrice,
                partBPros = partBPros,
                recommendation = recommendation
            )
            repository.insertPartComparison(comparison)
        }
    }

    fun deletePartComparison(id: Long) {
        viewModelScope.launch {
            repository.deletePartComparison(id)
        }
    }


    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Settings actions
    fun setGGUFModel(modelId: String) {
        settings.setSelectedModelId(modelId)
    }

    fun setProjector(filename: String) {
        settings.setSelectedProjector(filename)
    }

    fun setWebResearchAllowed(allowed: Boolean) {
        settings.setWebResearchAllowed(allowed)
    }

    fun runDiagnostics() {
        viewModelScope.launch {
            _isLoadingDiagnostics.value = true
            try {
                val report = repository.runDiagnostics()
                _diagnosticReport.value = report
            } finally {
                _isLoadingDiagnostics.value = false
            }
        }
    }

    fun dismissDiagnostics() {
        _diagnosticReport.value = null
    }

    // Catalog Navigation Actions
    private val _isCatalogOpen = MutableStateFlow(false)
    val isCatalogOpen: StateFlow<Boolean> = _isCatalogOpen.asStateFlow()

    fun openCategory(categoryName: String, initialSubcategory: String? = null) {
        _selectedCategory.value = categoryName
        _selectedSubcategory.value = initialSubcategory
        _selectedManufacturer.value = null
        _selectedDeviceProfile.value = null
        _selectedComponentDetail.value = null
        _catalogSearchQuery.value = ""
        _isCatalogOpen.value = true
    }

    fun openCatalogAll() {
        _selectedCategory.value = null
        _selectedSubcategory.value = null
        _selectedManufacturer.value = null
        _selectedDeviceProfile.value = null
        _selectedComponentDetail.value = null
        _catalogSearchQuery.value = ""
        _isCatalogOpen.value = true
    }

    fun closeCatalog() {
        _isCatalogOpen.value = false
        _selectedCategory.value = null
        _selectedSubcategory.value = null
        _selectedManufacturer.value = null
        _selectedDeviceProfile.value = null
        _selectedComponentDetail.value = null
    }

    fun selectSubcategory(subcategory: String?) {
        _selectedSubcategory.value = subcategory
        _selectedDeviceProfile.value = null
        _selectedComponentDetail.value = null
    }

    fun openManufacturer(manufacturer: String) {
        _selectedManufacturer.value = manufacturer
        _selectedDeviceProfile.value = null
        _selectedComponentDetail.value = null
    }

    fun openDeviceProfile(fingerprintKey: String) {
        viewModelScope.launch {
            val profileEntity = repository.getDeviceProfile(fingerprintKey)
            if (profileEntity != null) {
                val components = db.repairDao().getComponentsForDeviceSync(fingerprintKey)
                _selectedDeviceProfile.value = parseDeviceProfile(profileEntity, components)
                _selectedComponentDetail.value = null
            }
        }
    }

    fun openComponentDetail(component: com.example.model.DeviceComponent) {
        _selectedComponentDetail.value = component
    }

    fun closeComponentDetail() {
        _selectedComponentDetail.value = null
    }

    fun setCatalogSearchQuery(query: String) {
        _catalogSearchQuery.value = query
    }

    fun navigateBackInCatalog(): Boolean {
        if (_selectedComponentDetail.value != null) {
            _selectedComponentDetail.value = null
            return true
        }
        if (_selectedDeviceProfile.value != null) {
            _selectedDeviceProfile.value = null
            return true
        }
        if (_selectedSubcategory.value != null) {
            _selectedSubcategory.value = null
            return true
        }
        if (_selectedManufacturer.value != null) {
            _selectedManufacturer.value = null
            return true
        }
        if (_selectedCategory.value != null) {
            _selectedCategory.value = null
            return true
        }
        if (_isCatalogOpen.value) {
            _isCatalogOpen.value = false
            return true
        }
        return false
    }

    fun startDiagnosisFromComponent(
        profile: com.example.model.DeviceProfile,
        component: com.example.model.DeviceComponent
    ) {
        val query = "${profile.manufacturer} ${profile.modelName} ${component.componentName} reparieren / austauschen"
        startDiagnosis(
            deviceQuery = query,
            categoryHint = profile.category
        )
    }

    private fun parseDeviceProfile(
        entity: com.example.data.local.DeviceProfileEntity,
        components: List<com.example.data.local.DeviceComponentEntity>
    ): com.example.model.DeviceProfile {
        val specs = mutableListOf<com.example.model.SpecificationItem>()
        try {
            val arr = JSONArray(entity.specificationsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                specs.add(
                    com.example.model.SpecificationItem(
                        category = obj.optString("category", "Allgemein"),
                        label = obj.optString("label", ""),
                        value = obj.optString("value", "")
                    )
                )
            }
        } catch (_: Exception) {}

        val software = mutableListOf<String>()
        try {
            val arr = JSONArray(entity.softwareInfoJson)
            for (i in 0 until arr.length()) {
                software.add(arr.getString(i))
            }
        } catch (_: Exception) {}

        val errors = mutableListOf<com.example.model.ErrorCodeItem>()
        try {
            val arr = JSONArray(entity.knownErrorCodesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                errors.add(
                    com.example.model.ErrorCodeItem(
                        code = obj.optString("code", ""),
                        description = obj.optString("description", ""),
                        cause = obj.optString("cause", ""),
                        solution = obj.optString("solution", "")
                    )
                )
            }
        } catch (_: Exception) {}

        val domainComponents = components.map { parseDeviceComponent(it) }

        return com.example.model.DeviceProfile(
            id = entity.id,
            fingerprintKey = entity.fingerprintKey,
            category = entity.category,
            subcategory = entity.subcategory,
            manufacturer = entity.manufacturer,
            modelName = entity.modelName,
            modelNumber = entity.modelNumber,
            releaseYear = entity.releaseYear,
            specifications = specs,
            softwareProcedures = software,
            errorCodes = errors,
            components = domainComponents,
            createdTimestamp = entity.createdTimestamp
        )
    }

    private fun parseDeviceComponent(entity: com.example.data.local.DeviceComponentEntity): com.example.model.DeviceComponent {
        val tools = mutableListOf<String>()
        try {
            val arr = JSONArray(entity.toolsJson)
            for (i in 0 until arr.length()) tools.add(arr.getString(i))
        } catch (_: Exception) {}

        val warnings = mutableListOf<String>()
        try {
            val arr = JSONArray(entity.warningsJson)
            for (i in 0 until arr.length()) warnings.add(arr.getString(i))
        } catch (_: Exception) {}

        val steps = mutableListOf<com.example.model.RepairStep>()
        try {
            val arr = JSONArray(entity.stepsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                steps.add(
                    com.example.model.RepairStep(
                        stepNumber = obj.optInt("stepNumber", i + 1),
                        title = obj.optString("title", "Schritt ${i + 1}"),
                        description = obj.optString("description", ""),
                        caution = if (obj.has("caution") && !obj.isNull("caution") && obj.getString("caution").isNotBlank()) obj.getString("caution") else null,
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (_: Exception) {}

        return com.example.model.DeviceComponent(
            id = entity.id,
            deviceFingerprintKey = entity.deviceFingerprintKey,
            componentName = entity.componentName,
            componentCategory = entity.componentCategory,
            partNumberOrSpec = entity.partNumberOrSpec,
            difficulty = entity.difficulty,
            estimatedTimeMinutes = entity.estimatedTimeMinutes,
            tools = tools,
            warnings = warnings,
            repairSteps = steps,
            notes = entity.notes
        )
    }

    // ==========================================
    // OFFLINE REPAIR SKILL PACKS
    // ==========================================
    val allSkillPacks: StateFlow<List<RepairSkillPackEntity>> = repository.allSkillPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installedSkillPacks: StateFlow<List<RepairSkillPackEntity>> = repository.installedSkillPacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showSkillPacksManager = MutableStateFlow(false)
    val showSkillPacksManager: StateFlow<Boolean> = _showSkillPacksManager.asStateFlow()

    private val _selectedSkillPack = MutableStateFlow<RepairSkillPack?>(null)
    val selectedSkillPack: StateFlow<RepairSkillPack?> = _selectedSkillPack.asStateFlow()

    private val _skillPackCategoryFilter = MutableStateFlow("Alle")
    val skillPackCategoryFilter: StateFlow<String> = _skillPackCategoryFilter.asStateFlow()

    private val _skillPackSearchQuery = MutableStateFlow("")
    val skillPackSearchQuery: StateFlow<String> = _skillPackSearchQuery.asStateFlow()

    private val _downloadingPackId = MutableStateFlow<String?>(null)
    val downloadingPackId: StateFlow<String?> = _downloadingPackId.asStateFlow()

    fun openSkillPacksManager(packIdToInspect: String? = null) {
        if (packIdToInspect != null) {
            val found = allSkillPacks.value.find { it.packId == packIdToInspect }
            if (found != null) {
                _selectedSkillPack.value = RepairSkillPacksCatalog.toDomain(found)
            }
        }
        _showSkillPacksManager.value = true
    }

    fun closeSkillPacksManager() {
        _showSkillPacksManager.value = false
        _selectedSkillPack.value = null
    }

    fun selectSkillPack(pack: RepairSkillPack?) {
        _selectedSkillPack.value = pack
    }

    fun setSkillPackCategoryFilter(category: String) {
        _skillPackCategoryFilter.value = category
    }

    fun setSkillPackSearchQuery(query: String) {
        _skillPackSearchQuery.value = query
    }

    fun downloadSkillPack(packId: String) {
        viewModelScope.launch {
            _downloadingPackId.value = packId
            kotlinx.coroutines.delay(1100) // realistic download animation
            repository.installSkillPack(packId)
            _downloadingPackId.value = null
            _selectedSkillPack.value?.let { current ->
                if (current.packId == packId) {
                    val updated = repository.getSkillPackById(packId)
                    if (updated != null) {
                        _selectedSkillPack.value = RepairSkillPacksCatalog.toDomain(updated)
                    }
                }
            }
        }
    }

    fun uninstallSkillPack(packId: String) {
        viewModelScope.launch {
            repository.uninstallSkillPack(packId)
            _selectedSkillPack.value?.let { current ->
                if (current.packId == packId) {
                    val updated = repository.getSkillPackById(packId)
                    if (updated != null) {
                        _selectedSkillPack.value = RepairSkillPacksCatalog.toDomain(updated)
                    }
                }
            }
        }
    }

    fun applySkillTemplateToWorkbench(template: RepairTemplateItem, packTitle: String) {
        viewModelScope.launch {
            repository.createProject(
                title = template.title,
                deviceModel = packTitle,
                category = "Offline-Skill Vorlage",
                notes = "${template.description}\n\nSchwierigkeit: ${template.difficulty} • Geschätzte Dauer: ${template.estimatedMinutes} Min.\nBenötigte Werkzeuge: ${template.toolsNeeded.joinToString(", ")}"
            )
            closeSkillPacksManager()
            selectTab(1) // Jump to Workbench / Projects tab
        }
    }

    fun applySkillTemplateToActiveDiagnosis(template: RepairTemplateItem) {
        val current = _activeDiagnosis.value
        if (current != null) {
            val newSteps = template.steps.mapIndexed { idx, stepText ->
                com.example.model.RepairStep(
                    stepNumber = current.steps.size + idx + 1,
                    title = "Offline-Schritt: ${template.title} (#${idx + 1})",
                    description = stepText,
                    isCompleted = false
                )
            }
            val newTools = template.toolsNeeded.map {
                com.example.model.ToolItem(name = it, isRequired = true, isChecked = false)
            }
            _activeDiagnosis.value = current.copy(
                steps = current.steps + newSteps,
                requiredTools = current.requiredTools + newTools
            )
        }
    }

    fun getOfflineSkillsContextForQuery(query: String): String {
        val installed = installedSkillPacks.value
        if (installed.isEmpty()) return ""

        val queryLower = query.lowercase()
        val matchingPacks = installed.map { RepairSkillPacksCatalog.toDomain(it) }.filter { pack ->
            pack.supportedKeywords.any { keyword -> queryLower.contains(keyword.lowercase()) } ||
                pack.title.lowercase().contains(queryLower) ||
                pack.category.lowercase().contains(queryLower)
        }

        if (matchingPacks.isEmpty()) return ""

        val sb = StringBuilder()
        sb.append("=== LOKALE OFFLINE-REPARATURSKILLS AKTIV ===\n")
        matchingPacks.take(2).forEach { pack ->
            sb.append("\n[Skill-Pack: ${pack.title} (${pack.category})]\n")
            if (pack.safetyInstructions.isNotEmpty()) {
                sb.append("WICHTIGE SICHERHEITSHINWEISE (OFFLINE):\n")
                pack.safetyInstructions.forEach { safe ->
                    sb.append("• [${safe.severity}] ${safe.title}: ${safe.description}\n")
                }
            }
            if (pack.diagnosticProcedures.isNotEmpty()) {
                sb.append("OFFLINE-DIAGNOSEVERFAHREN:\n")
                pack.diagnosticProcedures.forEach { diag ->
                    sb.append("• ${diag.title}: ${diag.testMethod} -> Soll: ${diag.expectedResult}\n")
                }
            }
        }
        return sb.toString()
    }
}
