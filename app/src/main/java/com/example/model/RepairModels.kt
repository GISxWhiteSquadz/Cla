package com.example.model

data class RepairCategory(
    val id: String,
    val title: String,
    val iconName: String,
    val description: String,
    val popularIssues: List<String>,
    val subcategories: List<String> = emptyList()
)

data class DeviceFingerprint(
    val manufacturer: String,
    val model: String,
    val variant: String = ""
) {
    val normalizedKey: String
        get() = "${manufacturer.trim().uppercase()}_${model.trim().uppercase().replace(" ", "_")}"
            .filter { it.isLetterOrDigit() || it == '_' }

    val displayTitle: String
        get() = "$manufacturer $model${if (variant.isNotBlank()) " ($variant)" else ""}".trim()
}

data class RepairStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val caution: String? = null,
    val isCompleted: Boolean = false
)

data class ToolItem(
    val name: String,
    val isRequired: Boolean = true,
    val isChecked: Boolean = false
)

data class RAGChunk(
    val title: String,
    val content: String,
    val source: String,
    val confidence: Float,
    val manufacturer: String,
    val model: String
)

data class GroundingSource(
    val title: String,
    val url: String,
    val domain: String = ""
)

data class MultiAgentAuditStep(
    val title: String,
    val detail: String,
    val isPassed: Boolean = true
)

data class MultiAgentWorkflowReport(
    val researchAgentStatus: String, // z.B. "Recherche & Web-Suche erfolgreich"
    val searchSources: List<String> = emptyList(),
    val extractedSpecsCount: Int = 0,
    val extractedComponentsCount: Int = 0,
    val databaseAgentStatus: String, // z.B. "Validiert & Einsortiert"
    val assignedCategory: String,
    val assignedSubcategory: String,
    val completenessScorePercent: Int = 100,
    val auditSteps: List<MultiAgentAuditStep> = emptyList(),
    val summaryMessage: String = "",
    val liveSearchQueries: List<String> = emptyList(),
    val liveSources: List<GroundingSource> = emptyList()
)

data class DiagnosisResult(
    val deviceFingerprint: DeviceFingerprint,
    val issueSummary: String,
    val safetyWarnings: List<String>,
    val requiredTools: List<ToolItem>,
    val rootCauseAnalysis: String,
    val steps: List<RepairStep>,
    val ragChunks: List<RAGChunk>,
    val modelUsed: String,
    val isLocalInference: Boolean = true,
    val multiAgentReport: MultiAgentWorkflowReport? = null,
    val generatedAt: Long = System.currentTimeMillis(),
    val searchQueries: List<String> = emptyList(),
    val webSources: List<GroundingSource> = emptyList()
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCallInfo: String? = null,
    val searchQueries: List<String> = emptyList(),
    val webSources: List<GroundingSource> = emptyList(),
    val modelTag: String? = null
)

enum class MessageSender {
    USER,
    AGENT,
    SYSTEM
}

data class GGUFModelInfo(
    val id: String,
    val name: String,
    val filename: String,
    val sizeBytes: Long,
    val sizeLabel: String,
    val recommendedRam: String,
    val isVisionCompatible: Boolean,
    val contextLength: Int = 4096,
    val description: String
)

data class DiagnosticReport(
    val ggufEngineStatus: String,
    val selectedModel: String,
    val activeThreads: Int,
    val ramFreeMb: Long,
    val ramTotalMb: Long,
    val storageFreeMb: Long,
    val databaseChunksCount: Int,
    val databaseProjectsCount: Int,
    val embeddingLatencyMs: Long,
    val lastCheckTimestamp: Long = System.currentTimeMillis()
)

data class SpecificationItem(
    val category: String, // z.B. "Display", "Akku", "Schrauben & Gehäuse", "Platine"
    val label: String,    // z.B. "Panel-Typ", "Kapazität", "Schrauben-Dreher"
    val value: String     // z.B. "6.1 Zoll OLED 120Hz", "3200 mAh Li-Ion", "Pentalobe P2 & Tri-Point Y000"
)

data class ErrorCodeItem(
    val code: String,        // z.B. "E18", "Fehler 8", "BLOD", "P0300"
    val description: String, // z.B. "Abpumpfehler / Laugenpumpe blockiert"
    val cause: String,       // z.B. "Fremdkörper im Flusensieb oder defekter Pumpenmotor"
    val solution: String     // z.B. "Notentleerung durchführen, Flusensieb reinigen, Pumpe prüfen"
)

data class DeviceComponent(
    val id: Long = 0,
    val deviceFingerprintKey: String,
    val componentName: String,         // z.B. "Bildschirm / Display", "Akku / Batterie", "Brüheinheit"
    val componentCategory: String,     // z.B. "Optik & Touch", "Energieversorgung", "Mechanik"
    val partNumberOrSpec: String,      // z.B. "Ersatzteil #5332145600", "Li-Ion 3.85V"
    val difficulty: String,            // "Einfach", "Mittel", "Schwierig", "Experte"
    val estimatedTimeMinutes: Int,     // z.B. 45
    val tools: List<String>,           // z.B. ["Pentalobe P2", "Saugnapf", "Heißluftföhn 80°C"]
    val warnings: List<String>,        // z.B. ["Akku vor Arbeiten unbedingt abstecken", "Flexkabel nicht überbiegen"]
    val repairSteps: List<RepairStep>, // Schritt für Schritt
    val notes: String = ""
)

data class DeviceProfile(
    val id: Long = 0,
    val fingerprintKey: String,
    val category: String,              // z.B. "PC-Bau & Hardware", "Smartphones", "Haushalt"
    val subcategory: String = "",      // z.B. "CPU / Prozessor", "Mainboard", "Kaffeevollautomaten"
    val manufacturer: String,          // z.B. "AMD", "Apple", "DeLonghi", "Bosch"
    val modelName: String,             // z.B. "Ryzen 7 7800X3D", "iPhone 14 Pro"
    val modelNumber: String,           // z.B. "100-100000910WOF", "A2890"
    val releaseYear: String,           // z.B. "2023", "2022"
    val specifications: List<SpecificationItem>,
    val softwareProcedures: List<String>, // z.B. ["DFU Modus: Lauter -> Leiser -> Power gedrückt halten", "Diagnose-Menü: *#0*#"]
    val errorCodes: List<ErrorCodeItem>,
    val components: List<DeviceComponent> = emptyList(),
    val createdTimestamp: Long = System.currentTimeMillis()
)
