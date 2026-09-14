package com.example

import com.example.data.repository.WorkbenchData
import com.example.model.MeasurementMode
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testMultimeterDiodeEvaluation() {
        val tp = WorkbenchData.multimeterTestPoints.first { it.id == "esp32_gpio_diode" }
        assertEquals(MeasurementMode.DIODE, tp.mode)

        // Test normal reading (0.58 V)
        val normalResult = WorkbenchData.evaluateReading(tp, 0.58)
        assertEquals(WorkbenchData.EvaluationStatus.OK, normalResult.status)

        // Test short circuit reading (0.005 V)
        val shortResult = WorkbenchData.evaluateReading(tp, 0.005)
        assertEquals(WorkbenchData.EvaluationStatus.SHORT_CIRCUIT, shortResult.status)
        assertTrue(shortResult.title.contains("Kurzschluss", ignoreCase = true))

        // Test open circuit reading (2.5 V)
        val openResult = WorkbenchData.evaluateReading(tp, 2.5)
        assertEquals(WorkbenchData.EvaluationStatus.OPEN_CIRCUIT, openResult.status)
    }

    @Test
    fun testMultimeterResistanceEvaluation() {
        val tp = WorkbenchData.multimeterTestPoints.first { it.id == "esp32_3v3_rail" }
        assertEquals(MeasurementMode.RESISTANCE, tp.mode)

        // Test dead short on 3.3V rail (0.2 Ohm)
        val shortResult = WorkbenchData.evaluateReading(tp, 0.2)
        assertEquals(WorkbenchData.EvaluationStatus.SHORT_CIRCUIT, shortResult.status)

        // Test healthy high impedance rail (12000 Ohm)
        val healthyResult = WorkbenchData.evaluateReading(tp, 12000.0)
        assertEquals(WorkbenchData.EvaluationStatus.OK, healthyResult.status)
    }

    @Test
    fun testSmdChipsAndBenchmarksAvailable() {
        assertTrue(WorkbenchData.smdChips.isNotEmpty())
        assertTrue(WorkbenchData.smdChips.any { it.codeOrName == "M92T36" })
        assertTrue(WorkbenchData.smdChips.any { it.codeOrName == "BQ24193" })
        assertTrue(WorkbenchData.smdChips.any { it.codeOrName == "A7" })

        val pcBm = WorkbenchData.getBenchmarkForCategory("PC-Bau & Hardware")
        assertTrue(pcBm.co2SavedKg >= 150.0)
        assertTrue(pcBm.averageNewCostEuro >= 700.0)
    }
}
