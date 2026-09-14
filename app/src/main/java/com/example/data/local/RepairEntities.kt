package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repair_history")
data class RepairHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceName: String,
    val problemDescription: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val safetyWarningsJson: String,
    val toolsNeededJson: String,
    val rootCauseAnalysis: String,
    val stepsJson: String,
    val modelUsed: String,
    val fingerprintKey: String,
    val imageUri: String? = null
)

@Entity(tableName = "repair_projects")
data class RepairProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val deviceModel: String,
    val fingerprintKey: String,
    val category: String,
    val status: String, // "In Diagnose", "Ersatzteil bestellt", "In Reparatur", "Abgeschlossen"
    val notes: String,
    val checklistJson: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_chunks")
data class KnowledgeChunkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceFingerprintKey: String,
    val manufacturer: String,
    val model: String,
    val category: String,
    val title: String,
    val content: String,
    val vectorTokensCsv: String,
    val source: String,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_manuals")
data class SavedManualEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val deviceFingerprintKey: String,
    val summary: String,
    val fullContent: String,
    val isOfflineReady: Boolean = true,
    val savedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "device_profiles")
data class DeviceProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fingerprintKey: String,
    val category: String,              // z.B. "PC-Bau & Hardware", "Smartphones", "Haushalt"
    val subcategory: String = "",      // z.B. "CPU / Prozessor", "Mainboard", "Kaffeevollautomaten"
    val manufacturer: String,          // z.B. "AMD", "Intel", "Apple", "DeLonghi"
    val modelName: String,             // z.B. "Ryzen 7 7800X3D", "iPhone 14 Pro"
    val modelNumber: String,           // z.B. "100-100000910WOF", "A2890"
    val releaseYear: String,           // z.B. "2023", "2022"
    val specificationsJson: String,    // JSON array of SpecificationItem
    val softwareInfoJson: String,      // JSON array of strings
    val knownErrorCodesJson: String,   // JSON array of ErrorCodeItem
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "device_components")
data class DeviceComponentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deviceFingerprintKey: String,
    val componentName: String,         // z.B. "Bildschirm / Display"
    val componentCategory: String,     // z.B. "Optik & Touch"
    val partNumberOrSpec: String,      // z.B. "6.1 OLED 120Hz"
    val difficulty: String,            // "Einfach", "Mittel", "Schwierig"
    val estimatedTimeMinutes: Int,     // z.B. 45
    val toolsJson: String,             // JSON array of strings
    val warningsJson: String,          // JSON array of strings
    val stepsJson: String,             // JSON array of RepairStep
    val notes: String = ""
)

@Entity(tableName = "office_documents")
data class OfficeDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentNumber: String,
    val title: String,
    val docType: String, // "Angebot", "Rechnung", "Auftrag", "Reparaturbericht", "PC-Bau Plan", "Prüfprotokoll", "Notiz"
    val categoryFolder: String, // "Angebote & Kostenvoranschläge", "Rechnungen", "Reparatur- & PC-Aufträge", "Protokolle & Berichte", "PC-Konfigurationen & Budget", "Notizen & Entwürfe"
    val customerName: String = "",
    val customerContact: String = "",
    val deviceOrProject: String = "",
    val status: String = "Entwurf", // "Entwurf", "Offen / Versendet", "In Bearbeitung", "Bezahlt", "Abgeschlossen"
    val content: String = "",
    val lineItemsJson: String = "[]",
    val subtotalEuro: Double = 0.0,
    val taxRatePercent: Double = 19.0,
    val totalEuro: Double = 0.0,
    val budgetTargetEuro: Double = 0.0,
    val fileSizeBytes: Long = 0L,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "part_comparisons")
data class PartComparisonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val targetBudgetEuro: Double = 0.0,
    val partAName: String,
    val partASpecs: String,
    val partAPriceEuro: Double,
    val partAPros: String = "",
    val partBName: String,
    val partBSpecs: String,
    val partBPriceEuro: Double,
    val partBPros: String = "",
    val recommendation: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "repair_skill_packs")
data class RepairSkillPackEntity(
    @PrimaryKey
    val packId: String,
    val title: String,
    val category: String,
    val description: String,
    val version: String,
    val sizeMb: Double,
    val isInstalled: Boolean = false,
    val installTimestamp: Long? = null,
    val templatesJson: String,
    val safetyInstructionsJson: String,
    val diagnosticProceduresJson: String,
    val supportedKeywordsCsv: String,
    val iconName: String = "Build"
)

