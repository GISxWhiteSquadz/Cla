package com.example.data.local

import org.json.JSONArray
import org.json.JSONObject

object OfficeDataSeeder {

    suspend fun seedOfficeData(dao: RepairDao) {
        if (dao.getOfficeDocumentsCount() == 0) {
            val docs = createInitialDocuments()
            dao.insertAllOfficeDocuments(docs)
        }

        val comparisons = createInitialComparisons()
        dao.insertAllPartComparisons(comparisons)
    }

    private fun createInitialDocuments(): List<OfficeDocumentEntity> {
        val now = System.currentTimeMillis()

        // 1. Angebot / Kostenvoranschlag iPhone 14 Pro
        val quote1Items = JSONArray().apply {
            put(JSONObject().apply {
                put("description", "Super Retina XDR OLED Displayeinheit (Original Refurbished Grade A+)")
                put("partNumber", "OEM-APL-IP14P-DISP")
                put("quantity", 1)
                put("unitPrice", 179.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "Li-Ion Akkumulator 3200mAh inkl. Dichtungskleber & Isolationsfolie")
                put("partNumber", "BATT-APL-IP14P")
                put("quantity", 1)
                put("unitPrice", 45.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "Fachgerechte Montage, TrueTone-Serialisierung & IP68-Dichtigkeitsprüfung")
                put("partNumber", "SRV-LAB-01")
                put("quantity", 1)
                put("unitPrice", 59.00)
                put("isLabor", true)
            })
        }

        val quote1 = OfficeDocumentEntity(
            documentNumber = "ANG-2026-0042",
            title = "Kostenvoranschlag: Display- & Akkutausch iPhone 14 Pro",
            docType = "Angebot",
            categoryFolder = "Angebote & Kostenvoranschläge",
            customerName = "Maximilian Weber",
            customerContact = "m.weber@email.de | +49 171 9283741",
            deviceOrProject = "Apple iPhone 14 Pro (A2890)",
            status = "Offen / Versendet",
            content = """
                # Kostenvoranschlag gem. § 632 Abs. 3 BGB
                **Empfänger:** Maximilian Weber
                **Gerät:** Apple iPhone 14 Pro (Modell A2890)
                **Fehlerbefund:** Glasbruch nach Sturzschaden, Akku-Restkapazität bei 78% (Service-Meldung).
                
                Die Reparatur erfolgt mit zertifizierten Ersatzteilen unter ESD-Schutzbedingungen.
                Gültigkeit dieses Angebots: 14 Tage ab Ausstellungsdatum.
            """.trimIndent(),
            lineItemsJson = quote1Items.toString(),
            subtotalEuro = 283.00,
            taxRatePercent = 19.0,
            totalEuro = 336.77,
            budgetTargetEuro = 350.00,
            fileSizeBytes = 4120L,
            createdTimestamp = now - 86400000L * 2,
            updatedTimestamp = now - 86400000L * 2
        )

        // 2. Rechnung High-End PC-Bau
        val inv1Items = JSONArray().apply {
            put(JSONObject().apply {
                put("description", "Fachgerechter Zusammenbau Custom Gaming PC & Custom Cable-Management")
                put("partNumber", "SRV-PC-BUILD")
                put("quantity", 1)
                put("unitPrice", 130.00)
                put("isLabor", true)
            })
            put(JSONObject().apply {
                put("description", "BIOS/UEFI Flash auf neuestes AGESA Release, EXPO DDR5-6000 & Lüfterkurven")
                put("partNumber", "SRV-BIOS-OPT")
                put("quantity", 1)
                put("unitPrice", 25.00)
                put("isLabor", true)
            })
            put(JSONObject().apply {
                put("description", "24-Stunden System-Stresstest (Prime95 Small-FFTs, FurMark, MemTest86)")
                put("partNumber", "SRV-BURNIN")
                put("quantity", 1)
                put("unitPrice", 45.00)
                put("isLabor", true)
            })
            put(JSONObject().apply {
                put("description", "Thermal Grizzly Kryonaut Extreme Wärmeleitpaste (Auftrag auf AM5 Heatspreader)")
                put("partNumber", "MAT-TG-KRYO")
                put("quantity", 1)
                put("unitPrice", 12.50)
                put("isLabor", false)
            })
        }

        val inv1 = OfficeDocumentEntity(
            documentNumber = "REC-2026-0118",
            title = "Rechnung: High-End Custom PC Montage & Stresstest",
            docType = "Rechnung",
            categoryFolder = "Rechnungen",
            customerName = "Dr. Stefan Lindemann",
            customerContact = "s.lindemann@tech-solutions.de | 089-4920194",
            deviceOrProject = "Custom PC: AMD Ryzen 7 7800X3D + RTX 4080 Super",
            status = "Bezahlt",
            content = """
                # Rechnung nach GoBD / § 14 UStG
                **Auftraggeber:** Dr. Stefan Lindemann
                **Projekt:** Montage & Inbetriebnahme High-End Gaming Workstation
                **Leistungszeitraum:** 10.09.2026 - 12.09.2026
                
                Zahlungseingang per Banküberweisung dankend erhalten am 12.09.2026.
                Garantie auf handwerkliche Montagearbeiten: 24 Monate.
            """.trimIndent(),
            lineItemsJson = inv1Items.toString(),
            subtotalEuro = 212.50,
            taxRatePercent = 19.0,
            totalEuro = 252.88,
            budgetTargetEuro = 260.00,
            fileSizeBytes = 5280L,
            createdTimestamp = now - 86400000L * 3,
            updatedTimestamp = now - 86400000L * 1
        )

        // 3. Reparaturauftrag DeLonghi Kaffeemaschine
        val order1Items = JSONArray().apply {
            put(JSONObject().apply {
                put("description", "Dichtungssatz Premium O-Ringe EPDM Lebensmittelecht für Brühgruppe & Thermoblock")
                put("partNumber", "DL-SEAL-KIT-01")
                put("quantity", 1)
                put("unitPrice", 18.90)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "Ultraschall-Tiefenreinigung Brüheinheit, Drainageventil & Mahlwerk-Justage")
                put("partNumber", "SRV-COFFEE-MAIN")
                put("quantity", 1)
                put("unitPrice", 59.00)
                put("isLabor", true)
            })
            put(JSONObject().apply {
                put("description", "Amidosulfonsäure Intensiventkalkung & Drucktest 15 bar")
                put("partNumber", "SRV-COFFEE-DESCAL")
                put("quantity", 1)
                put("unitPrice", 22.00)
                put("isLabor", true)
            })
        }

        val order1 = OfficeDocumentEntity(
            documentNumber = "AUF-2026-0034",
            title = "Reparaturauftrag: DeLonghi ECAM Mahlwerk & Brühgruppe Wartung",
            docType = "Auftrag",
            categoryFolder = "Reparatur- & PC-Aufträge",
            customerName = "Karin Schultze",
            customerContact = "karin.schultze@gmx.net | 030-8472911",
            deviceOrProject = "DeLonghi Magnifica S ECAM 22.110.B",
            status = "In Bearbeitung",
            content = """
                # Werkstatt-Reparaturauftrag
                **Kunde:** Karin Schultze
                **Gerät:** DeLonghi Magnifica S (ECAM 22.110.B)
                **Symptom:** Kaffee läuft sehr langsam, Mahlwerk quietscht, Trester nass und matschig.
                **Vereinbarte Fertigstellung:** 16.09.2026
            """.trimIndent(),
            lineItemsJson = order1Items.toString(),
            subtotalEuro = 99.90,
            taxRatePercent = 19.0,
            totalEuro = 118.88,
            budgetTargetEuro = 120.00,
            fileSizeBytes = 3890L,
            createdTimestamp = now - 3600000L * 10,
            updatedTimestamp = now - 3600000L * 1
        )

        // 4. Prüfprotokoll E-Bike
        val proto1 = OfficeDocumentEntity(
            documentNumber = "BER-2026-0007",
            title = "Prüfbericht & Akkukapazitätstest Bosch PowerPack 500",
            docType = "Prüfprotokoll",
            categoryFolder = "Protokolle & Berichte",
            customerName = "Blitz Express Kurierdienst",
            customerContact = "werkstatt@blitz-express.de",
            deviceOrProject = "Bosch PowerPack 500 Wh Frame Battery",
            status = "Abgeschlossen",
            content = """
                # Sicherheitsprüfbericht nach DGUV V3 & VDE 0701
                **Prüfling:** Bosch PowerPack 500 (36V / 13.4Ah)
                **BMS-Status:** Keine Fehlereinträge (Fehler 500 behoben durch Kontaktreinigung & Reset).
                **Gemessene Restkapazität:** 472 Wh (94.4% SoH - State of Health).
                **Isolationswiderstand:** > 20 MOhm (Grenzwert > 0.3 MOhm eingehalten).
                **Schutzleiterprüfung:** Erfolgreich bestanden.
                **Empfehlung:** Akku voll betriebsbereit. Nächste Sicherheitsprüfung in 12 Monaten.
            """.trimIndent(),
            lineItemsJson = "[]",
            subtotalEuro = 49.00,
            taxRatePercent = 19.0,
            totalEuro = 58.31,
            budgetTargetEuro = 60.00,
            fileSizeBytes = 6120L,
            createdTimestamp = now - 86400000L * 5,
            updatedTimestamp = now - 86400000L * 5
        )

        // 5. PC-Bau Plan 1500€ WQHD
        val pcPlanItems = JSONArray().apply {
            put(JSONObject().apply {
                put("description", "AMD Ryzen 7 7800X3D Prozessor (8C/16T, 96MB 3D V-Cache)")
                put("partNumber", "100-100000910WOF")
                put("quantity", 1)
                put("unitPrice", 379.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "ASUS TUF Gaming B650-Plus WiFi Mainboard AM5")
                put("partNumber", "90MB1BY0-M0EAY0")
                put("quantity", 1)
                put("unitPrice", 189.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "G.Skill Flare X5 32GB DDR5-6000 CL30 Dual-Kit")
                put("partNumber", "F5-6000J3038F16GX2-FX5")
                put("quantity", 1)
                put("unitPrice", 112.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "Sapphire Pulse Radeon RX 7800 XT 16GB Gaming GPU")
                put("partNumber", "11330-02-20G")
                put("quantity", 1)
                put("unitPrice", 499.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "Samsung 990 PRO 2TB NVMe PCIe 4.0 SSD (7450 MB/s)")
                put("partNumber", "MZ-V9P2T0BW")
                put("quantity", 1)
                put("unitPrice", 159.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "be quiet! Pure Power 12 M 750W ATX 3.0 80 PLUS Gold")
                put("partNumber", "BN343")
                put("quantity", 1)
                put("unitPrice", 109.00)
                put("isLabor", false)
            })
            put(JSONObject().apply {
                put("description", "be quiet! Shadow Base 800 DX Gehäuse & Dark Rock Pro 5 Kühler")
                put("partNumber", "BK036")
                put("quantity", 1)
                put("unitPrice", 185.00)
                put("isLabor", false)
            })
        }

        val pcPlan = OfficeDocumentEntity(
            documentNumber = "PC-2026-0015",
            title = "PC-Bau Konfiguration: 1.600 € WQHD Sweet-Spot Build",
            docType = "PC-Bau Plan",
            categoryFolder = "PC-Konfigurationen & Budget",
            customerName = "Werkstatt Referenzbau",
            customerContact = "intern@coresystems.local",
            deviceOrProject = "Custom PC AM5 WQHD Ultra",
            status = "In Bearbeitung",
            content = """
                # 1.600 € WQHD Gaming & Creator Konfiguration
                **Plattform:** AMD AM5 (Upgrade-sicher bis mindestens 2027)
                **Fokus:** Maximale FPS pro Euro in 2560x1440, geringe Abwärme, flüsterleiser Betrieb.
                **Kompatibilitäts-Check:**
                - CPU & RAM: EXPO DDR5-6000 CL30 harmonisiert 1:1 mit AMD Infinity Fabric (UCLK=MCLK 3000 MHz).
                - Netzteil: ATX 3.0 mit nativem 12VHPWR Anschluss für zukünftige GPU Upgrades.
                - Gehäuse-Airflow: 3x 140mm Pure Wings 3 Lüfter im Überdruck-Verfahren zur Staubvermeidung.
            """.trimIndent(),
            lineItemsJson = pcPlanItems.toString(),
            subtotalEuro = 1632.00,
            taxRatePercent = 19.0,
            totalEuro = 1632.00, // Endverbraucher-Bruttopreise im Bauplan
            budgetTargetEuro = 1650.00,
            fileSizeBytes = 7450L,
            createdTimestamp = now - 86400000L * 1,
            updatedTimestamp = now
        )

        return listOf(quote1, inv1, order1, proto1, pcPlan)
    }

    private fun createInitialComparisons(): List<PartComparisonEntity> {
        val now = System.currentTimeMillis()

        val comp1 = PartComparisonEntity(
            title = "Gaming WQHD GPU: RTX 4070 Ti Super vs. Radeon RX 7900 XT",
            category = "PC-Bau & Hardware",
            targetBudgetEuro = 850.00,
            partAName = "NVIDIA GeForce RTX 4070 Ti Super 16GB",
            partASpecs = "16GB GDDR6X, 256-bit, DLSS 3.5 Frame Gen, Raytracing Cores Gen 3, 285W TDP",
            partAPriceEuro = 829.00,
            partAPros = "Herausragende Raytracing-Leistung, DLSS 3.5 & Frame Generation, CUDA-Beschleunigung für Blender/Premiere, hohe Energieeffizienz",
            partBName = "AMD Radeon RX 7900 XT 20GB",
            partBSpecs = "20GB GDDR6, 320-bit, FSR 3 Fluid Motion, riesiger VRAM-Puffer, 315W TDP",
            partBPriceEuro = 749.00,
            partBPros = "80 € günstiger, 4GB mehr VRAM (20GB Zukunftssicherheit bei 4K-Texturen), sehr starke native Rasterizing-FPS in WQHD",
            recommendation = "Für Spieler mit Raytracing- und KI/Streaming-Anspruch ist die RTX 4070 Ti Super die technisch vielseitigere Wahl. Wer maximale native Raster-FPS pro Euro und mehr Videospeicher wünscht, greift zur 80 € günstigeren RX 7900 XT.",
            createdTimestamp = now - 86400000L * 2
        )

        val comp2 = PartComparisonEntity(
            title = "Displaytausch iPhone 15: Original Refurbished OLED vs. Hard OLED Aftermarket",
            category = "Smartphones",
            targetBudgetEuro = 160.00,
            partAName = "Original Apple OLED Refurbished Grade A+",
            partASpecs = "Originales Samsung/LG Super Retina XDR Panel, neues Frontglas, 2000 Nits Helligkeit",
            partAPriceEuro = 149.00,
            partAPros = "Originale Blickwinkelstabilität & Farbtreue, keine Helligkeitsverluste im Sonnenlicht, TrueTone programmierbar, werksgetreues Touch-Feedback",
            partBName = "Aftermarket Hard OLED (GX / JK)",
            partBSpecs = "Kompatibles Hard-OLED Dritthersteller, ca. 1200 Nits Spitzenhelligkeit, minimal dickerer Rahmen",
            partBPriceEuro = 85.00,
            partBPros = "64 € Ersparnis, gute Kontraste für Alltagsanwendungen, budgetfreundlich für ältere Dienstgeräte",
            recommendation = "Für Privatkunden und geschäftliche Dauernutzung wird dringend das Original Refurbished Panel empfohlen, um Bruchsicherheit, Sonnenlichttauglichkeit und Touch-Präzision auf Werkstandard zu halten.",
            createdTimestamp = now - 86400000L * 3
        )

        val comp3 = PartComparisonEntity(
            title = "Gaming CPU Sweet-Spot: AMD Ryzen 7 7800X3D vs. Intel Core i7-14700K",
            category = "PC-Bau & Hardware",
            targetBudgetEuro = 400.00,
            partAName = "AMD Ryzen 7 7800X3D (8C / 16T)",
            partASpecs = "Sockel AM5, 96MB 3D V-Cache, 120W TDP (real ~55W Gaming), bis 5.0 GHz",
            partAPriceEuro = 379.00,
            partAPros = "Schnellste Gaming-CPU der Welt, extrem niedriger Stromverbrauch beim Spielen, leise zu kühlen, AM5 Plattform-Zukunftssicherheit",
            partBName = "Intel Core i7-14700K (20C: 8P + 12E)",
            partBSpecs = "Sockel LGA1700, bis 5.6 GHz Turbo, 253W Max Turbo Power, Hybrid-Architektur",
            partBPriceEuro = 389.00,
            partBPros = "Massiv höhere Multi-Thread-Leistung für Video-Rendern, Code-Kompilieren und CAD, Intel QuickSync Video Decoder",
            recommendation = "Für einen reinen Gaming-PC ist der Ryzen 7 7800X3D ungeschlagen effizient und kühl. Wer den PC täglich als Workstation für 4K-Videoschnitt und Produktiv-Workloads nutzt, profitiert von den 20 Kernen des 14700K.",
            createdTimestamp = now - 86400000L * 4
        )

        return listOf(comp1, comp2, comp3)
    }
}
