package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Repair-Agent by CoreSystems", appName)
  }

  @Test
  fun `verify seeded initial catalog profiles`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.RepairDatabase.getInstance(context)
    val dao = db.repairDao()
    com.example.data.local.InitialCatalogSeeder.seedInitialProfilesAndComponents(dao)
    val profiles = dao.getAllDeviceProfilesSync()
    assertTrue("Seeded catalog should contain device profiles", profiles.isNotEmpty())

    // 1. Check PC-Bau profile and subcategory
    val amdProfile = dao.getDeviceProfileByFingerprint("AMD_RYZEN_7_7800X3D")
    assertTrue("AMD Ryzen profile should exist", amdProfile != null)
    assertEquals("PC-Bau & Hardware", amdProfile?.category)
    assertEquals("CPU / Prozessor", amdProfile?.subcategory)

    val pcComponents = dao.getComponentsForDeviceSync("AMD_RYZEN_7_7800X3D")
    assertTrue("AMD components should be seeded", pcComponents.isNotEmpty())

    // 2. Check Subcategory querying
    val cpuProfiles = dao.getDeviceProfilesByCategoryAndSubcategory("PC-Bau & Hardware", "CPU / Prozessor").first()
    assertTrue("CPU / Prozessor query should return AMD profile", cpuProfiles.any { it.fingerprintKey == "AMD_RYZEN_7_7800X3D" })

    // 3. Check Maker profile and subcategory
    val robotProfile = dao.getDeviceProfileByFingerprint("ESPRESSIF_ESP32_DEVKIT")
    assertTrue("ESP32 Robotics profile should exist", robotProfile != null)
    assertEquals("Robotik & Maker", robotProfile?.category)
    assertEquals("Mikrocontroller-Boards (ESP32/Arduino)", robotProfile?.subcategory)

    val robotComponents = dao.getComponentsForDeviceSync("ESPRESSIF_ESP32_DEVKIT")
    assertTrue("Robotics components should be seeded", robotComponents.isNotEmpty())
  }

  @Test
  fun `verify multi agent pipeline categorizes and stores profile with subcategory`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.RepairDatabase.getInstance(context)
    val dao = db.repairDao()

    val dummyDiagnosis = com.example.model.DiagnosisResult(
      deviceFingerprint = com.example.model.DeviceFingerprint(
        manufacturer = "Apple",
        model = "iPhone 15 Pro",
        variant = "A3102"
      ),
      issueSummary = "Display Glas gesprungen nach Sturz",
      safetyWarnings = listOf("Akku vor Displaywechsel trennen"),
      requiredTools = listOf(
        com.example.model.ToolItem("Pentalobe P2", isRequired = true)
      ),
      rootCauseAnalysis = "OLED-Panel und Schutzglas durch mechanische Einwirkung gebrochen.",
      steps = listOf(
        com.example.model.RepairStep(1, "Pentalobe Schrauben entfernen", "Untere Schrauben lösen")
      ),
      ragChunks = emptyList(),
      modelUsed = "Qwen2.5-VL-3B"
    )

    val report = com.example.data.agent.MultiAgentPipeline.executeCuratedIngestion(
      dao = dao,
      rawDiagnosis = dummyDiagnosis,
      userQuery = "iPhone 15 Pro Display Reparatur",
      categoryHint = "Smartphones",
      webGroundingUsed = false
    )

    assertTrue("Multi-Agent report should be generated", report.auditSteps.isNotEmpty())
    assertEquals("Smartphones", report.assignedCategory)
    assertEquals("Smartphones (iOS)", report.assignedSubcategory)
    assertTrue("Completeness score should be high", report.completenessScorePercent >= 75)

    // Verify it was persisted in Room DB with correct subcategory
    val profile = dao.getDeviceProfileByFingerprint("APPLE_IPHONE_15_PRO")
    assertTrue("Device profile should be saved in DB", profile != null)
    assertEquals("Smartphones (iOS)", profile?.subcategory)

    // Verify subcategory query
    val iosDevices = dao.getDeviceProfilesByCategoryAndSubcategory("Smartphones", "Smartphones (iOS)").first()
    assertTrue("iOS subcategory should contain iPhone 15 Pro", iosDevices.any { it.fingerprintKey == "APPLE_IPHONE_15_PRO" })
  }
}
