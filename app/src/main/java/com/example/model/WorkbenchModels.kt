package com.example.model

enum class MeasurementMode(val label: String, val unit: String) {
    DIODE("Diodentest", "V"),
    RESISTANCE("Widerstand", "Ω"),
    VOLTAGE("Gleichspannung", "V")
}

data class MultimeterTestPoint(
    val id: String,
    val category: String, // e.g. "PC ATX Netzteil", "ESP32 & Arduino", "USB-C Port", "Li-Ion Akkus", "Haushaltsgeräte", "Gaming-Konsolen"
    val testPointName: String,
    val mode: MeasurementMode,
    val expectedMin: Double,
    val expectedMax: Double,
    val expectedDisplay: String,
    val description: String,
    val pinoutNotes: String,
    val testMethod: String,
    val shortCircuitConsequence: String,
    val openCircuitConsequence: String
)

data class SmdChip(
    val codeOrName: String,
    val fullPartNumber: String,
    val manufacturer: String,
    val packageType: String,
    val functionCategory: String,
    val description: String,
    val operatingRatings: String,
    val pinoutSummary: List<String>,
    val typicalFailures: String,
    val dropInEquivalents: List<String>,
    val testProcedure: String
)

data class MaterialNorm(
    val category: String, // "Dichtungen (O-Ringe)", "Wärmeleitmaterial", "18650 Akkuzellen", "Schrauben & Bit-Normen"
    val title: String,
    val subTitle: String,
    val temperatureRange: String?,
    val materialSpecs: String,
    val applicationAreas: String,
    val criticalWarnings: String
)

data class EcoSavingsBenchmark(
    val categoryId: String,
    val categoryName: String,
    val co2SavedKg: Double,
    val eWasteSavedKg: Double,
    val averageNewCostEuro: Double
)

data class WorkshopReport(
    val reportId: String,
    val creationDate: String,
    val technicianName: String,
    val deviceModel: String,
    val deviceCategory: String,
    val customerOrOwner: String,
    val faultDescription: String,
    val completedActions: List<String>,
    val replacedComponents: List<String>,
    val testMeasurements: List<String>,
    val finalTestPassed: Boolean,
    val estimatedSavingsEuro: Double,
    val co2SavedKg: Double,
    val generalNotes: String
)
