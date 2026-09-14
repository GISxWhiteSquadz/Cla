package com.example.data.repository

import com.example.data.agent.MultiAgentPipeline
import com.example.data.llm.GeminiCloudProvider
import com.example.data.llm.LocalGGUFEngine
import com.example.data.local.KnowledgeChunkEntity
import com.example.data.local.OfficeDocumentEntity
import com.example.data.local.PartComparisonEntity
import com.example.data.local.RepairDao
import com.example.data.local.RepairHistoryEntity
import com.example.data.local.RepairProjectEntity
import com.example.data.local.RepairSkillPackEntity
import com.example.data.local.SavedManualEntity
import com.example.data.preferences.AppSettings
import com.example.data.rag.VectorRAGEngine
import com.example.model.DeviceFingerprint
import com.example.model.DiagnosticReport
import com.example.model.DiagnosisResult
import com.example.model.GGUFModelInfo
import com.example.model.RepairCategory
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class RepairRepository(
    private val dao: RepairDao,
    private val ggufEngine: LocalGGUFEngine,
    val settings: AppSettings
) {

    val allHistory: Flow<List<RepairHistoryEntity>> = dao.getAllHistory()
    val allProjects: Flow<List<RepairProjectEntity>> = dao.getAllProjects()
    val allManuals: Flow<List<SavedManualEntity>> = dao.getAllManuals()
    val allDeviceProfiles: Flow<List<com.example.data.local.DeviceProfileEntity>> = dao.getAllDeviceProfiles()
    val allOfficeDocuments: Flow<List<OfficeDocumentEntity>> = dao.getAllOfficeDocuments()
    val allPartComparisons: Flow<List<PartComparisonEntity>> = dao.getAllPartComparisons()

    fun getDeviceProfilesByCategory(category: String): Flow<List<com.example.data.local.DeviceProfileEntity>> =
        dao.getDeviceProfilesByCategory(category)

    fun getDeviceProfilesByCategoryAndSubcategory(category: String, subcategory: String): Flow<List<com.example.data.local.DeviceProfileEntity>> =
        dao.getDeviceProfilesByCategoryAndSubcategory(category, subcategory)

    fun getDeviceProfilesByCategoryAndManufacturer(category: String, manufacturer: String): Flow<List<com.example.data.local.DeviceProfileEntity>> =
        dao.getDeviceProfilesByCategoryAndManufacturer(category, manufacturer)

    fun getComponentsForDevice(fingerprintKey: String): Flow<List<com.example.data.local.DeviceComponentEntity>> =
        dao.getComponentsForDevice(fingerprintKey)

    fun getDeviceProfileFlow(fingerprintKey: String): Flow<com.example.data.local.DeviceProfileEntity?> =
        dao.getDeviceProfileByFingerprintFlow(fingerprintKey)

    suspend fun getDeviceProfile(fingerprintKey: String): com.example.data.local.DeviceProfileEntity? =
        dao.getDeviceProfileByFingerprint(fingerprintKey)

    val availableGGUFModels: List<GGUFModelInfo> = ggufEngine.availableModels
    val availableProjectors: List<String> = ggufEngine.availableProjectors

    val categories: List<RepairCategory> = listOf(
        RepairCategory(
            id = "pc_build",
            title = "PC-Bau & Hardware",
            iconName = "pc_hardware",
            description = "Mainboards, CPUs, GPUs, RAM, Netzteile & Kühlung",
            popularIssues = listOf("Debug-LED Rot / Gelb (RAM/CPU)", "12VHPWR GPU Stromanschluss", "M.2 Kühlkörper Schutzfolie", "Front-Panel Header JFP1"),
            subcategories = listOf("CPU / Prozessor", "Mainboard", "Grafikkarte (GPU)", "Arbeitsspeicher (RAM)", "SSD & Speicher", "Netzteil (PSU)", "PC-Gehäuse & Airflow", "CPU-Kühlung")
        ),
        RepairCategory(
            id = "laptop",
            title = "Laptops & Notebooks",
            iconName = "laptop",
            description = "MacBooks, ThinkPads, Tastaturen & Akkus",
            popularIssues = listOf("Lüfter laut & überhitzt", "Startet nicht (Black Screen)", "Tastatur prellt / klemmt", "Scharnier gebrochen"),
            subcategories = listOf("Apple Mac & MacBooks", "Business & Windows Laptops", "Gaming-Laptops", "Ultrabooks & Mobile")
        ),
        RepairCategory(
            id = "smartphone",
            title = "Smartphones",
            iconName = "smartphone",
            description = "Displays, Akkus, Kameras & Ladebuchsen",
            popularIssues = listOf("Displayglas gebrochen", "Akku bläht / entlädt schnell", "Ladebuchse Wackelkontakt", "Kein Netz / Antenne"),
            subcategories = listOf("Smartphones (iOS)", "Smartphones (Android)", "Foldables & Flip", "Mittelklasse")
        ),
        RepairCategory(
            id = "tablet",
            title = "Tablets",
            iconName = "tablet",
            description = "iPads, Android- & Windows-Tablets",
            popularIssues = listOf("Touchscreen reagiert nicht", "Akku tauschen", "Gehäuse verzogen", "Lautsprecher kratzt"),
            subcategories = listOf("Apple iPads", "Pro & Creator Tablets", "Standard-Tablets", "E-Reader")
        ),
        RepairCategory(
            id = "gaming",
            title = "Gaming & Konsolen",
            iconName = "gamepad",
            description = "PlayStation, Xbox, Switch & Controller",
            popularIssues = listOf("Stick-Drift Hall-Sensor", "PS5 Überhitzung (3 Beeps)", "HDMI-Port Wackelkontakt", "Laufwerk liest nicht"),
            subcategories = listOf("Heimkonsolen (PS5/Xbox)", "Handhelds & Portable (Switch/Steam Deck)", "Gamepad & Controller", "VR & Zubehör")
        ),
        RepairCategory(
            id = "audio",
            title = "Audio & Hi-Fi",
            iconName = "audio",
            description = "Kopfhörer, Lautsprecher & Soundsysteme",
            popularIssues = listOf("Einseitig kein Ton / Kabelbruch", "Bluetooth-Kopplung fehlgeschlagen", "Ladecase-Akku defekt", "Klinkenbuchse Wackelkontakt"),
            subcategories = listOf("Over-Ear Kopfhörer (ANC)", "In-Ear Kopfhörer & True Wireless", "Bluetooth-Lautsprecher", "Hi-Fi & Verstärker")
        ),
        RepairCategory(
            id = "haushalt",
            title = "Haushaltsgeräte",
            iconName = "haushalt",
            description = "Kaffeevollautomaten, Wasch- & Spülmaschinen",
            popularIssues = listOf("Kaffeeautomat Brüheinheit / Entkalkung", "Fehler E18 Laugenpumpe blockiert", "Mahlwerk blockiert & Thermoblock", "Spülmaschine heizt / pumpt nicht"),
            subcategories = listOf("Kaffeevollautomaten & Siebträger", "Großgeräte (Waschen & Spülen)", "Staubsauger & Roboter", "Küchenkleingeräte")
        ),
        RepairCategory(
            id = "maker_robotics",
            title = "Robotik & Maker",
            iconName = "robotics",
            description = "ESP32, Arduino, Motortreiber & Sensorik",
            popularIssues = listOf("Brownout Reset (ESP32)", "L298N Motortreiber & Common GND", "HC-SR04 Spannungsteiler 3.3V", "I2C Bus & Sensorik 0x68"),
            subcategories = listOf("Mikrocontroller-Boards (ESP32/Arduino)", "Single-Board-Computer (Raspberry Pi)", "Motortreiber & Aktoren", "Sensorik & Busmodule")
        ),
        RepairCategory(
            id = "fahrrad",
            title = "Fahrrad & E-Bike",
            iconName = "bike",
            description = "E-Bikes, Pedelecs, Antriebe & Schaltung",
            popularIssues = listOf("Bosch Fehler 500 Sensor", "Kette springt / Schaltung", "Hydraulikbremse entlüften", "Akkukontakte korrodiert"),
            subcategories = listOf("E-Bikes (Motoren, Akkus & Sensorik)", "Klassische Fahrräder (Schaltung & Bremsen)", "Bremsanlagen", "Federgabeln & Dämpfer")
        ),
        RepairCategory(
            id = "fahrzeug",
            title = "Fahrzeuge & KFZ",
            iconName = "car",
            description = "PKW, Motorrad & OBD-2 Diagnosen",
            popularIssues = listOf("Fehler P0300 Fehlzündung", "Batterie / BMS anlernen", "Bremslichtschalter", "Glühkerze defekt"),
            subcategories = listOf("PKW Wartung & Elektronik", "Motor & Antriebsstrang", "Zündung & Gemisch", "Bremsen & Fahrwerk", "OBD-2 & Bordelektronik")
        )
    )

    fun parseFingerprint(query: String, categoryHint: String = ""): DeviceFingerprint {
        val words = query.trim().split("\\s+".toRegex())
        val manufacturer = when {
            words.any { it.equals("apple", true) || it.equals("iphone", true) || it.equals("macbook", true) || it.equals("ipad", true) } -> "Apple"
            words.any { it.equals("samsung", true) || it.equals("galaxy", true) } -> "Samsung"
            words.any { it.equals("bosch", true) } -> "Bosch"
            words.any { it.equals("delonghi", true) || it.equals("de'longhi", true) || it.equals("magnifica", true) } -> "DeLonghi"
            words.any { it.equals("sony", true) || it.equals("playstation", true) || it.equals("ps5", true) || it.equals("ps4", true) } -> "Sony"
            words.any { it.equals("nintendo", true) || it.equals("switch", true) } -> "Nintendo"
            words.any { it.equals("lenovo", true) || it.equals("thinkpad", true) } -> "Lenovo"
            words.any { it.equals("siemens", true) } -> "Siemens"
            words.any { it.equals("jura", true) } -> "Jura"
            words.any { it.equals("miele", true) } -> "Miele"
            words.any { it.equals("google", true) || it.equals("pixel", true) } -> "Google"
            words.any { it.equals("dell", true) || it.equals("xps", true) } -> "Dell"
            words.any { it.equals("bose", true) } -> "Bose"
            words.any { it.equals("shimano", true) } -> "Shimano"
            words.any { it.equals("volkswagen", true) || it.equals("vw", true) || it.equals("golf", true) } -> "Volkswagen"
            words.any { it.equals("bmw", true) } -> "BMW"
            words.any { it.equals("bequiet", true) || it.equals("be quiet", true) || it.equals("dark rock", true) } -> "be quiet!"
            words.any { it.equals("esp32", true) || it.equals("esp8266", true) || it.equals("espressif", true) } -> "Espressif"
            words.any { it.equals("arduino", true) } -> "Arduino"
            words.any { it.equals("raspberry", true) || it.equals("raspi", true) } -> "Raspberry Pi"
            words.any { it.equals("amd", true) || it.equals("ryzen", true) } -> "AMD"
            words.any { it.equals("intel", true) } -> "Intel"
            words.any { it.equals("nvidia", true) || it.equals("geforce", true) || it.equals("rtx", true) } -> "NVIDIA"
            words.any { it.equals("asus", true) } -> "ASUS"
            words.any { it.equals("msi", true) } -> "MSI"
            words.any { it.equals("gigabyte", true) } -> "Gigabyte"
            words.any { it.equals("corsair", true) } -> "Corsair"
            else -> words.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Universal"
        }

        val model = query.replace(Regex("(?i)\\b(apple|samsung|bosch|delonghi|sony|nintendo|lenovo|siemens|espressif|arduino|raspberry|intel|amd|nvidia|asus|msi|gigabyte|corsair)\\b"), "").trim()
            .ifBlank { query.trim() }

        return DeviceFingerprint(
            manufacturer = manufacturer,
            model = model.ifBlank { "Standard-Modell" },
            variant = categoryHint
        )
    }

    suspend fun diagnose(
        deviceQuery: String,
        categoryHint: String = "",
        imageUri: String? = null,
        useSearchGrounding: Boolean = false
    ): DiagnosisResult {
        val fingerprint = parseFingerprint(deviceQuery, categoryHint)
        val allChunks = dao.getAllChunks()
        val retrievedRAG = VectorRAGEngine.search(deviceQuery, fingerprint, allChunks, limit = 3)

        val selectedModelId = settings.selectedModelId.value
        val modelInfo = availableGGUFModels.find { it.id == selectedModelId }
        val hasProjector = settings.selectedProjector.value.isNotBlank()

        val (diagnosis, searchQueries, webSources) = if (useSearchGrounding && settings.webResearchAllowed.value) {
            // Online search grounding with gemini-3.5-flash
            val groundingResp = GeminiCloudProvider.queryWithSearchGrounding(
                prompt = "Reparaturanleitung für ${fingerprint.displayTitle}: Problem: $deviceQuery. Nenne Sicherheitswarnungen, Werkzeuge, Ursache und 4 Reparatur-Schritte.",
                systemInstruction = "Du bist der leitende Hardware-Diagnostiker. Recherchiere per Google Search die exakten Spezifikationen, Teilenummern und typische Fehlerursachen für das Gerät."
            )
            val localBase = ggufEngine.runDiagnosis(
                deviceFingerprint = fingerprint,
                problemDescription = deviceQuery,
                selectedModel = modelInfo,
                hasVisionProjector = hasProjector,
                ragChunks = retrievedRAG,
                imageProvided = imageUri != null
            )
            Triple(
                localBase.copy(
                    modelUsed = "Gemini 3.5 Flash (mit Google Web-Suche)",
                    isLocalInference = false,
                    rootCauseAnalysis = "${groundingResp.text}\n\n[Lokale Ergänzung]: ${localBase.rootCauseAnalysis}",
                    searchQueries = groundingResp.searchQueries,
                    webSources = groundingResp.sources
                ),
                groundingResp.searchQueries,
                groundingResp.sources
            )
        } else {
            val localBase = ggufEngine.runDiagnosis(
                deviceFingerprint = fingerprint,
                problemDescription = deviceQuery,
                selectedModel = modelInfo,
                hasVisionProjector = hasProjector,
                ragChunks = retrievedRAG,
                imageProvided = imageUri != null
            )
            Triple(localBase, emptyList<String>(), emptyList<com.example.model.GroundingSource>())
        }

        // Save into history automatically
        saveToHistory(diagnosis, imageUri)

        // Agent-basierte Aufnahme: Agent 1 (Recherche) + Agent 2 (Datenbank & Kuration)
        val workflowReport = MultiAgentPipeline.executeCuratedIngestion(
            dao = dao,
            rawDiagnosis = diagnosis,
            userQuery = deviceQuery,
            categoryHint = categoryHint,
            webGroundingUsed = useSearchGrounding && settings.webResearchAllowed.value,
            liveSearchQueries = searchQueries,
            liveSources = webSources
        )

        return diagnosis.copy(multiAgentReport = workflowReport)
    }

    suspend fun chatWithAgent(
        history: List<com.example.data.llm.GeminiMessage>,
        fingerprint: DeviceFingerprint?,
        ragChunks: List<com.example.model.RAGChunk> = emptyList(),
        roleSystemInstruction: String = "Du bist der CoreRepair Master AI Reparatur- & Hardware-Experte. Nutze Google-Recherche für exakte Daten, Pinbelegungen, Drehmomente und Bauteil-Spezifikationen.",
        useSearchGrounding: Boolean = true,
        modelName: String = "gemini-3.5-flash"
    ): com.example.data.llm.GroundingResponse {
        return if (settings.webResearchAllowed.value && useSearchGrounding) {
            GeminiCloudProvider.multiTurnChat(
                history = history,
                systemInstruction = roleSystemInstruction,
                useSearchGrounding = true,
                modelName = modelName
            )
        } else {
            val lastUserMsg = history.lastOrNull { it.role == "user" }?.text ?: ""
            val reply = if (fingerprint != null) {
                ggufEngine.answerQuestion(lastUserMsg, fingerprint, ragChunks)
            } else {
                "Lokale Wissensbasis: Frage zu Hardware & Reparatur entgegengenommen."
            }
            com.example.data.llm.GroundingResponse(
                text = reply,
                modelName = "Lokales Edge-Modell (Offline)"
            )
        }
    }

    suspend fun askAgent(
        question: String,
        fingerprint: DeviceFingerprint,
        ragChunks: List<com.example.model.RAGChunk>
    ): String {
        return if (settings.webResearchAllowed.value && (question.contains("suche", true) || question.contains("preis", true))) {
            GeminiCloudProvider.queryWithSearchGroundingText(
                "Frage zu Reparatur von ${fingerprint.displayTitle}: $question"
            )
        } else {
            ggufEngine.answerQuestion(question, fingerprint, ragChunks)
        }
    }

    private suspend fun saveToHistory(result: DiagnosisResult, imageUri: String?) {
        val stepsArray = JSONArray().apply {
            result.steps.forEach { step ->
                put(JSONObject().apply {
                    put("stepNumber", step.stepNumber)
                    put("title", step.title)
                    put("description", step.description)
                    put("caution", step.caution ?: "")
                    put("isCompleted", step.isCompleted)
                })
            }
        }

        val toolsArray = JSONArray().apply {
            result.requiredTools.forEach { tool ->
                put(JSONObject().apply {
                    put("name", tool.name)
                    put("isRequired", tool.isRequired)
                    put("isChecked", tool.isChecked)
                })
            }
        }

        val warningsArray = JSONArray().apply {
            result.safetyWarnings.forEach { put(it) }
        }

        val entity = RepairHistoryEntity(
            deviceName = result.deviceFingerprint.displayTitle,
            problemDescription = result.issueSummary,
            category = result.deviceFingerprint.variant.ifBlank { "Allgemein" },
            timestamp = System.currentTimeMillis(),
            safetyWarningsJson = warningsArray.toString(),
            toolsNeededJson = toolsArray.toString(),
            rootCauseAnalysis = result.rootCauseAnalysis,
            stepsJson = stepsArray.toString(),
            modelUsed = result.modelUsed,
            fingerprintKey = result.deviceFingerprint.normalizedKey,
            imageUri = imageUri
        )

        dao.insertHistory(entity)
    }

    suspend fun createProject(
        title: String,
        deviceModel: String,
        category: String,
        notes: String
    ): Long {
        val fingerprint = parseFingerprint(deviceModel, category)
        val defaultChecklist = JSONArray().apply {
            put(JSONObject().apply { put("text", "Sichtprüfung & Typenschild erfassen"); put("done", true) })
            put(JSONObject().apply { put("text", "Schaltplan & RAG-Anleitung laden"); put("done", true) })
            put(JSONObject().apply { put("text", "Gehäuse öffnen & Strom trennen"); put("done", false) })
            put(JSONObject().apply { put("text", "Ersatzteil einbauen & testen"); put("done", false) })
            put(JSONObject().apply { put("text", "Zusammenbau & Endkontrolle"); put("done", false) })
        }

        val project = RepairProjectEntity(
            title = title,
            deviceModel = deviceModel,
            fingerprintKey = fingerprint.normalizedKey,
            category = category,
            status = "In Diagnose",
            notes = notes,
            checklistJson = defaultChecklist.toString()
        )
        return dao.insertProject(project)
    }

    suspend fun updateProject(project: RepairProjectEntity) {
        dao.updateProject(project.copy(updatedTimestamp = System.currentTimeMillis()))
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun runDiagnostics(): DiagnosticReport {
        val chunksCount = dao.getChunksCount()
        val projectsCount = dao.getProjectCount()
        return ggufEngine.runSystemDiagnostics(chunksCount, projectsCount)
    }

    // Office Documents CRUD
    suspend fun insertOfficeDocument(document: OfficeDocumentEntity): Long =
        dao.insertOfficeDocument(document)

    suspend fun updateOfficeDocument(document: OfficeDocumentEntity) =
        dao.updateOfficeDocument(document.copy(updatedTimestamp = System.currentTimeMillis()))

    suspend fun deleteOfficeDocument(id: Long) =
        dao.deleteOfficeDocumentById(id)

    // Part Comparisons CRUD
    suspend fun insertPartComparison(comparison: PartComparisonEntity): Long =
        dao.insertPartComparison(comparison)

    suspend fun deletePartComparison(id: Long) =
        dao.deletePartComparisonById(id)

    // Repair Skill Packs (Offline)
    val allSkillPacks: Flow<List<RepairSkillPackEntity>> = dao.getAllSkillPacks()
    val installedSkillPacks: Flow<List<RepairSkillPackEntity>> = dao.getInstalledSkillPacks()

    suspend fun getInstalledSkillPacksSync(): List<RepairSkillPackEntity> =
        dao.getInstalledSkillPacksSync()

    suspend fun getSkillPackById(packId: String): RepairSkillPackEntity? =
        dao.getSkillPackById(packId)

    suspend fun installSkillPack(packId: String) {
        dao.updateSkillPackInstallState(packId, true, System.currentTimeMillis())
    }

    suspend fun uninstallSkillPack(packId: String) {
        dao.updateSkillPackInstallState(packId, false, null)
    }

    suspend fun saveSkillPack(pack: RepairSkillPackEntity) {
        dao.insertSkillPack(pack)
    }
}

