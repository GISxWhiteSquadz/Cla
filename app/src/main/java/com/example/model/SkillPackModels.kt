package com.example.model

data class RepairTemplateItem(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String, // "Einsteiger", "Fortgeschritten", "Experte"
    val estimatedMinutes: Int,
    val toolsNeeded: List<String>,
    val steps: List<String>
)

data class SafetyInstructionItem(
    val id: String,
    val title: String,
    val severity: String, // "KRITISCH", "WARNUNG", "HINWEIS"
    val description: String,
    val protectiveGear: List<String>
)

data class DiagnosticProcedureItem(
    val id: String,
    val title: String,
    val symptomTarget: String,
    val testMethod: String,
    val expectedResult: String,
    val failureMeaning: String
)

data class RepairSkillPack(
    val packId: String,
    val title: String,
    val category: String,
    val description: String,
    val version: String,
    val sizeMb: Double,
    val isInstalled: Boolean = false,
    val installTimestamp: Long? = null,
    val templates: List<RepairTemplateItem> = emptyList(),
    val safetyInstructions: List<SafetyInstructionItem> = emptyList(),
    val diagnosticProcedures: List<DiagnosticProcedureItem> = emptyList(),
    val supportedKeywords: List<String> = emptyList(),
    val iconName: String = "Build"
)
