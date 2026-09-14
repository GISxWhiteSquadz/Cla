package com.example.data.local

import com.example.model.DiagnosticProcedureItem
import com.example.model.RepairSkillPack
import com.example.model.RepairTemplateItem
import com.example.model.SafetyInstructionItem
import org.json.JSONArray
import org.json.JSONObject

object RepairSkillPacksCatalog {

    fun parseTemplatesJson(json: String): List<RepairTemplateItem> {
        val list = mutableListOf<RepairTemplateItem>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val toolsArr = obj.optJSONArray("toolsNeeded") ?: JSONArray()
                val tools = mutableListOf<String>()
                for (t in 0 until toolsArr.length()) tools.add(toolsArr.getString(t))

                val stepsArr = obj.optJSONArray("steps") ?: JSONArray()
                val steps = mutableListOf<String>()
                for (s in 0 until stepsArr.length()) steps.add(stepsArr.getString(s))

                list.add(
                    RepairTemplateItem(
                        id = obj.optString("id", "tpl_$i"),
                        title = obj.optString("title", "Template"),
                        description = obj.optString("description", ""),
                        difficulty = obj.optString("difficulty", "Mittel"),
                        estimatedMinutes = obj.optInt("estimatedMinutes", 30),
                        toolsNeeded = tools,
                        steps = steps
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseSafetyJson(json: String): List<SafetyInstructionItem> {
        val list = mutableListOf<SafetyInstructionItem>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val gearArr = obj.optJSONArray("protectiveGear") ?: JSONArray()
                val gear = mutableListOf<String>()
                for (g in 0 until gearArr.length()) gear.add(gearArr.getString(g))

                list.add(
                    SafetyInstructionItem(
                        id = obj.optString("id", "safe_$i"),
                        title = obj.optString("title", "Sicherheitshinweis"),
                        severity = obj.optString("severity", "WARNUNG"),
                        description = obj.optString("description", ""),
                        protectiveGear = gear
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseDiagnosticsJson(json: String): List<DiagnosticProcedureItem> {
        val list = mutableListOf<DiagnosticProcedureItem>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DiagnosticProcedureItem(
                        id = obj.optString("id", "diag_$i"),
                        title = obj.optString("title", "Prüfverfahren"),
                        symptomTarget = obj.optString("symptomTarget", ""),
                        testMethod = obj.optString("testMethod", ""),
                        expectedResult = obj.optString("expectedResult", ""),
                        failureMeaning = obj.optString("failureMeaning", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun toDomain(entity: RepairSkillPackEntity): RepairSkillPack {
        return RepairSkillPack(
            packId = entity.packId,
            title = entity.title,
            category = entity.category,
            description = entity.description,
            version = entity.version,
            sizeMb = entity.sizeMb,
            isInstalled = entity.isInstalled,
            installTimestamp = entity.installTimestamp,
            templates = parseTemplatesJson(entity.templatesJson),
            safetyInstructions = parseSafetyJson(entity.safetyInstructionsJson),
            diagnosticProcedures = parseDiagnosticsJson(entity.diagnosticProceduresJson),
            supportedKeywords = entity.supportedKeywordsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            iconName = entity.iconName
        )
    }

    private fun buildJson(templates: List<RepairTemplateItem>, safeties: List<SafetyInstructionItem>, diagnostics: List<DiagnosticProcedureItem>): Triple<String, String, String> {
        val tplArray = JSONArray()
        templates.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("difficulty", t.difficulty)
            obj.put("estimatedMinutes", t.estimatedMinutes)
            val tools = JSONArray()
            t.toolsNeeded.forEach { tools.put(it) }
            obj.put("toolsNeeded", tools)
            val steps = JSONArray()
            t.steps.forEach { steps.put(it) }
            obj.put("steps", steps)
            tplArray.put(obj)
        }

        val safeArray = JSONArray()
        safeties.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("title", s.title)
            obj.put("severity", s.severity)
            obj.put("description", s.description)
            val gear = JSONArray()
            s.protectiveGear.forEach { gear.put(it) }
            obj.put("protectiveGear", gear)
            safeArray.put(obj)
        }

        val diagArray = JSONArray()
        diagnostics.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("title", d.title)
            obj.put("symptomTarget", d.symptomTarget)
            obj.put("testMethod", d.testMethod)
            obj.put("expectedResult", d.expectedResult)
            obj.put("failureMeaning", d.failureMeaning)
            diagArray.put(obj)
        }

        return Triple(tplArray.toString(), safeArray.toString(), diagArray.toString())
    }

    suspend fun seedInitialSkillPacks(dao: RepairDao) {
        val packs = mutableListOf<RepairSkillPackEntity>()

        // 1. Display & Touch OLED Master Pack (Pre-installed by default)
        val (p1T, p1S, p1D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_oled_replacement",
                    title = "OLED-Panel Demontage & Thermische Öffnung",
                    description = "Schonende Displaytrennung unter 80°C Wärmematte ohne Beschädigung des AMOLED-Layers.",
                    difficulty = "Fortgeschritten",
                    estimatedMinutes = 45,
                    toolsNeeded = listOf("Wärmematte / iOpener 80°C", "Saugnapf-Zange", "0.1mm Kunststoff-Plektrum", "Isopropanol 99.9%"),
                    steps = listOf(
                        "Gerät auf 80°C vorwärmen für mindestens 5-7 Minuten.",
                        "Saugnapf am unteren Bildschirmrand ansetzen und leichten Zug aufbauen.",
                        "Mit 0.1mm Kunststoff-Plektrum unter den Rahmen gleiten; Klebering durchtrennen.",
                        "Achtung: Keine Metallwerkzeuge nahe den FPC-Flachbandkabeln verwenden!",
                        "Display vorsichtig anklappen und Akku-Konnektor sofort physisch trennen."
                    )
                ),
                RepairTemplateItem(
                    id = "tpl_digitizer_calibration",
                    title = "Digitizer & Touchscreen IC-Transfer / Kalibrierung",
                    description = "Übertragung des EEPROM/Display-ICs zur Vermeidung von 'Unbekanntes Bauteil' Warnungen.",
                    difficulty = "Experte",
                    estimatedMinutes = 60,
                    toolsNeeded = listOf("Heißluft-Lötstation 240°C", "Flussmittel No-Clean", "Pinzette Titan", "EEPROM Programmer"),
                    steps = listOf(
                        "Altes Display vorsichtig vom originalen Touch-IC befreien.",
                        "Pads mit Entlötlitze reinigen und frisches bleifreies Lot (SAC305) aufbringen.",
                        "Neuen Screen vorbereiten und IC präzise unter Heißluft ausrichten.",
                        "Touch-Kalibrierungsroutine im Service-Menü durchführen."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_li_puncture",
                    title = "Akkubeschädigung durch Displaywerkzeug",
                    severity = "KRITISCH",
                    description = "Wird ein Plektrum tiefer als 3mm eingeführt, droht Durchstich des Lithium-Akkus mit sofortiger Stichflamme!",
                    protectiveGear = listOf("Schutzbrille", "Feuerfeste Unterlage", "Li-Ion Feuerlöschgranulat")
                ),
                SafetyInstructionItem(
                    id = "safe_glass_splinters",
                    title = "Splitterflug bei gesprungenem Deckglas",
                    severity = "WARNUNG",
                    description = "Vor dem Erwärmen zersplitterte Glasflächen lückenlos mit transparentem Paketklebeband abkleben.",
                    protectiveGear = listOf("Schnittfeste ESD-Handschuhe", "Schutzbrille")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_touch_matrix",
                    title = "Touch-Matrix & Phantomberührungen",
                    symptomTarget = "Eingaben reagieren verzögert oder verselbstständigen sich.",
                    testMethod = "Im Entwicklermenü 'Zeigerposition anzeigen' aktivieren und diagonale Linien ziehen.",
                    expectedResult = "Durchgehende Linien ohne Abriss oder Sprünge.",
                    failureMeaning = "Haarriss in der ITO-Sensorschicht des Digitizers."
                ),
                DiagnosticProcedureItem(
                    id = "diag_disp_rails",
                    title = "Display Power Rails (VDD/ELVDD/ELVSS)",
                    symptomTarget = "Display bleibt schwarz trotz Vibration/Ton.",
                    testMethod = "Spannung an den Spulen L_DISP_BOOST unter Last mit Multimeter messen.",
                    expectedResult = "ELVDD ca. +4.6V und ELVSS ca. -4.0V stabil.",
                    failureMeaning = "Display-PMIC defekt oder Kurzschluss auf der Hintergrundbeleuchtungs-Schiene."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_display_oled",
                title = "Display- & Glas-Austausch Master Pack",
                category = "Smartphones & Tablets",
                description = "Vollständige Vorlagen für OLED-, AMOLED- & LCD-Instandsetzung, Klebereste-Entfernung, Rahmenabdichtung und Display-IC-Transfers.",
                version = "1.4.2",
                sizeMb = 4.2,
                isInstalled = true,
                installTimestamp = System.currentTimeMillis() - 86400000L,
                templatesJson = p1T,
                safetyInstructionsJson = p1S,
                diagnosticProceduresJson = p1D,
                supportedKeywordsCsv = "display,glas,screen,oled,amoled,lcd,touch,splitter,streifen,touchscreen",
                iconName = "Smartphone"
            )
        )

        // 2. Akku-, BMS- & Lademanagement Pack (Pre-installed)
        val (p2T, p2S, p2D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_bms_transplant",
                    title = "BMS-Flexkabel Transplant & Spot-Welding",
                    description = "Sicheres Punktschweißen der Nickellaschen an neue Akkuzellen unter Beibehaltung des Original-BMS.",
                    difficulty = "Experte",
                    estimatedMinutes = 50,
                    toolsNeeded = listOf("Punktschweißgerät (Spot Welder)", "Kapton-Isolierband", "Keramik-Schere", "Nickelband 0.15mm"),
                    steps = listOf(
                        "Alte Zelle vorsichtig mit Keramik-Schere von BMS-Laschen trennen.",
                        "Niemals beide Pole gleichzeitig mit metallischen Werkzeugen berühren.",
                        "Kapton-Tape zur Isolierung zwischen Platinenrand und Zelle aufbringen.",
                        "Mit 4-5 Schweißpunkten pro Pol dauerhafte Verbindung schaffen.",
                        "Akkuzustand und Temperaturfühler (NTC) vor Gehäusemontage testen."
                    )
                ),
                RepairTemplateItem(
                    id = "tpl_trickle_charge",
                    title = "Reaktivierung tiefentladener Li-Ion Zellen",
                    description = "Sichere Vorladung von Zellen unter 2.8V mit Strombegrenzung auf 0.05C bis 3.2V erreicht sind.",
                    difficulty = "Fortgeschritten",
                    estimatedMinutes = 35,
                    toolsNeeded = listOf("Regelbares Labornetzteil", "Multimeter", "Temperatur-Messfühler"),
                    steps = listOf(
                        "Netzteil auf 3.7V und Stromgrenze auf 100mA einstellen.",
                        "Spannung alle 3 Minuten überwachen; Temperatur darf 35°C nicht überschreiten.",
                        "Sobald 3.2V überschritten sind, auf normale CC/CV Ladekurve umschalten."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_thermal_runaway",
                    title = "Lithium-Brand (Thermal Runaway)",
                    severity = "KRITISCH",
                    description = "Lithium-Akkus brennen ohne externen Sauerstoff mit bis zu 900°C ab. Kein Wasser verwenden! Zelle isolieren und in Brandschutzbox kühlen.",
                    protectiveGear = listOf("LiPo-Sicherheitstasche", "Hitzeschutzhandschuhe", "Brandschutzsand / PyroBubbles")
                ),
                SafetyInstructionItem(
                    id = "safe_bms_short",
                    title = "Verpolungs- und Kurzschlussgefahr",
                    severity = "WARNUNG",
                    description = "Vor Lötarbeiten am BMS immer die Gate-Steuerung der Schutz-MOSFETs spannungsfrei schalten.",
                    protectiveGear = listOf("ESD-Armband", "Keramik-Pinzette")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_battery_esr",
                    title = "Innenwiderstand & Last-Spannungsabfall",
                    symptomTarget = "Gerät schaltet bei 20-30% Restladung plötzlich ab.",
                    testMethod = "Spannung vor und während eines CPU-Stresstests messen.",
                    expectedResult = "Spannungsabfall kleiner als 0.25V bei Spitzenlast.",
                    failureMeaning = "Hoher Innenwiderstand durch Dendritenbildung; Zelle verschlissen."
                ),
                DiagnosticProcedureItem(
                    id = "diag_leakage_current",
                    title = "Ruhestrommessung im Standby (Parasitic Drain)",
                    symptomTarget = "Akku entlädt sich über Nacht ohne Nutzung.",
                    testMethod = "Labornetzteil an Akkukontakte anschließen und Stromaufnahme im Deep Sleep messen.",
                    expectedResult = "Stromaufnahme unter 5 mA im Standby.",
                    failureMeaning = "Leckstrom durch defekten Tristar/Hydra IC oder korrodierten Entkoppelkondensator."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_battery_bms",
                title = "Akku-, BMS- & Lademanagement Pack",
                category = "Batterietechnik & Power",
                description = "Offline-Expertenwissen zu Lithium-Zellendiagnose, BMS-Rekalibrierung, Tiefentladungs-Rettung und Power-Delivery Schnellladeprotokollen.",
                version = "1.3.0",
                sizeMb = 3.8,
                isInstalled = true,
                installTimestamp = System.currentTimeMillis() - 43200000L,
                templatesJson = p2T,
                safetyInstructionsJson = p2S,
                diagnosticProceduresJson = p2D,
                supportedKeywordsCsv = "akku,batterie,bms,ladung,lädt nicht,power,usb-c,heat,laufzeit,watt",
                iconName = "BatteryChargingFull"
            )
        )

        // 3. SMD Micro-Soldering & LogicBoard Pack (Available for Download)
        val (p3T, p3S, p3D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_short_circuit_detection",
                    title = "Kurzschluss-Ortung mit Kältespray & Labornetzteil",
                    description = "Einspeisung von 1.2V in verdächtige Power-Rails zur optischen Identifikation überhitzter Bauteile.",
                    difficulty = "Experte",
                    estimatedMinutes = 40,
                    toolsNeeded = listOf("Labornetzteil mit Feineinstellung", "Kältespray / Kolophonium", "Stereomikroskop"),
                    steps = listOf(
                        "Kurzgeschlossene Schiene (z.B. PP_VDD_MAIN) mit Diodenmodus gegen Masse identifizieren.",
                        "Netzteil auf maximal 1.2V (niemals Schienenspannung überschreiten!) und 2A limitieren.",
                        "Platine mit Kältespray vereisen; Masseklemme an Mainboard-GND.",
                        "Pluspol an die Spule der Schiene halten: Der defekte MLCC-Kondensator taut als erster blitzschnell auf."
                    )
                ),
                RepairTemplateItem(
                    id = "tpl_jumper_wire",
                    title = "Mikro-Jumper Drahtbrücke (0.02mm Kupfer)",
                    description = "Rekonstruktion abgerissener Lötaugen und interner Leiterbahnen mit isoliertem Kupferlackdraht.",
                    difficulty = "Experte",
                    estimatedMinutes = 55,
                    toolsNeeded = listOf("0.02mm Kupferlackdraht", "UV-Schutzlack grün", "UV-Aushärtungslampe", "Feine Lötspitze C115"),
                    steps = listOf(
                        "Via oder Restleiterbahn unter Mikroskop vorsichtig freikratzen.",
                        "Lackdraht mit Tropfen Flussmittel anlöten.",
                        "Draht in sauberer Kurve zum Zielpad führen.",
                        "Mit UV-Maskierlack fixieren und 60 Sekunden unter UV-LED aushärten."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_fumes_extraction",
                    title = "Giftige Kolophonium- und Bleidämpfe",
                    severity = "KRITISCH",
                    description = "Beim Verdampfen von Flussmitteln entstehen lungengängige Harzsäuren und Aerosole.",
                    protectiveGear = listOf("Aktivkohle-Absauganlage", "Partikelfilter FFP3")
                ),
                SafetyInstructionItem(
                    id = "safe_esd_protection",
                    title = "Elektrostatische Entladung (ESD)",
                    severity = "WARNUNG",
                    description = "MOSFET-Gates können bereits durch 100V statische Aufladung unbemerkt durchlegieren.",
                    protectiveGear = listOf("ESD-Tischmatte 10^6 Ohm", "Geerdetes Handgelenkband")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_diode_mode",
                    title = "Diodenmodus-Referenzmessung aller Power-Rails",
                    symptomTarget = "Mainboard startet nicht (0.000A Stromaufnahme).",
                    testMethod = "Rote Messspitze an Masse (GND), schwarze Messspitze an Testpunkt.",
                    expectedResult = "Spannungsabfall zwischen 0.250V und 0.650V je nach Schiene.",
                    failureMeaning = "0.000V bedeutet satter Kurzschluss nach Masse; OL bedeutet Unterbrechung."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_micro_soldering",
                title = "SMD Micro-Soldering & LogicBoard Pack",
                category = "Elektronik & SMD",
                description = "Schaltplan-Lesen, Boardview-Analyse, Kurzschluss-Ortung mit Thermografie, Jumper-Wire Verlegung und SMD-Bauteiltausch.",
                version = "2.1.0",
                sizeMb = 5.6,
                isInstalled = false,
                templatesJson = p3T,
                safetyInstructionsJson = p3S,
                diagnosticProceduresJson = p3D,
                supportedKeywordsCsv = "smd,löten,platine,logicboard,kurzschluss,diode,leiterbahn,widerstand,chip",
                iconName = "Memory"
            )
        )

        // 4. Wasser- & Korrosionsschaden Notfall-Pack
        val (p4T, p4S, p4D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_ultrasonic_cleaning",
                    title = "Ultraschallbad-Reinigung mit Isopropanol 99.9%",
                    description = "Tiefenreinigung unter ICs und BGAs zur Beseitigung von Mineralsalzen und Korrosionsbrücken.",
                    difficulty = "Mittel",
                    estimatedMinutes = 45,
                    toolsNeeded = listOf("Ultraschall-Reinigungsbad 40kHz", "Isopropanol 99.9%", "Antistatische Pinsel", "Heißlufttrockner"),
                    steps = listOf(
                        "Kameras, Mikrofone und Vibrationsmotoren vor dem Bad zwingend demontieren.",
                        "Platine für 6-8 Minuten bei Raumtemperatur in 40kHz Ultraschall eintauchen.",
                        "Reste von Kalk und Grünspan mit Pinsel mechanisch nachbearbeiten.",
                        "Mindestens 30 Minuten bei 55°C im Trockenofen restfeuchtefrei temperieren."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_electrolysis",
                    title = "Sofortiger Batterietrenn-Befehl",
                    severity = "KRITISCH",
                    description = "Ein feuchtes Gerät unter Spannung zerstört seine Leiterbahnen durch Elektrolyse binnen 60 Sekunden!",
                    protectiveGear = listOf("ESD-Handschuhe")
                ),
                SafetyInstructionItem(
                    id = "safe_flammable_iso",
                    title = "Explosionsgefahr durch Lösungsmitteldämpfe",
                    severity = "WARNUNG",
                    description = "Isopropanol bildet mit Luft zündfähige Gemische. Niemals Heißluftföhn in der Nähe offener Bäder betreiben.",
                    protectiveGear = listOf("Belüfteter Arbeitsplatz", "CO2-Löscher bereitstellen")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_corrosion_underfill",
                    title = "Underfill-Riss & Korrosion unter Chips",
                    symptomTarget = "Gerät stürzt bei Erwärmung ab oder zeigt unplausible Sensorwerte.",
                    testMethod = "Inspektion der IC-Ränder mit UV-Fluoreszenzlampe und Mikroskop.",
                    expectedResult = "Glatte, unbeschädigte Underfill-Kanten ohne weiße Ablagerungen.",
                    failureMeaning = "Kriechströme unter dem SoC oder PMIC; Bauteil muss gereballt werden."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_liquid_damage",
                title = "Wasser- & Korrosionsschaden Notfall-Pack",
                category = "Rettung & Reinigung",
                description = "Sofortmaßnahmen bei Kaffee-, Wasser- oder Salzwassereintritt. Ultraschall-Prozedere und Beseitigung von Kriechströmen.",
                version = "1.1.5",
                sizeMb = 3.1,
                isInstalled = false,
                templatesJson = p4T,
                safetyInstructionsJson = p4S,
                diagnosticProceduresJson = p4D,
                supportedKeywordsCsv = "wasser,flüssigkeit,nass,korrosion,ultraschall,grünspan,kaffee,reinigung",
                iconName = "WaterDrop"
            )
        )

        // 5. PC-Bau, CPU, GPU & VRM Hardware Pack
        val (p5T, p5S, p5D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_socket_pin_bend",
                    title = "Sockel-Pin Begradigung (AM5 / LGA1700)",
                    description = "Präzises Rückbiegen verbogener Mainboard-Kontakte unter 40x Stereomikroskop.",
                    difficulty = "Experte",
                    estimatedMinutes = 50,
                    toolsNeeded = listOf("0.3mm Akupunkturnadel / Injektionskanüle", "Stereomikroskop 40x", "Kaltlichtquelle"),
                    steps = listOf(
                        "Lichtwinkel so einstellen, dass Reflexionen der Pin-Köpfe eine gleichmäßige Linie bilden.",
                        "Kanüle über den verbogenen Pin stülpen und ohne Hebelkraft auf die Platine sanft ausrichten.",
                        "Federkraft mit feiner Pinzette prüfen – darf beim Einsetzen der CPU nicht wegknicken."
                    )
                ),
                RepairTemplateItem(
                    id = "tpl_liquid_metal_delid",
                    title = "Flüssigmetall & VRM Kühlflächen-Restaurierung",
                    description = "Auftragen von Gallium-Indium Kühlmittel mit doppelter Schutzlackierung gegen Kurzschlüsse.",
                    difficulty = "Fortgeschritten",
                    estimatedMinutes = 40,
                    toolsNeeded = listOf("Flüssigmetall (Thermal Grizzly Conductonaut)", "Silikon-Schutzlack MG Chemicals 422B", "Schaumstoff-Applikator"),
                    steps = listOf(
                        "SMD-Bauteile rund um den Die mit 2 Schichten Schutzlack versiegeln.",
                        "Flüssigmetall mikrofein verreiben, bis Spiegelfläche ohne Tröpfchenbildung entsteht.",
                        "Schaumstoff-Barriere an den Kühlkörperrändern einsetzen."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_psu_caps",
                    title = "Restladung in Schaltnetzteilen (400V DC)",
                    severity = "KRITISCH",
                    description = "Primärkondensatoren in PC-Netzteilen halten tödliche Spannungen auch Stunden nach dem Netztrennen!",
                    protectiveGear = listOf("Entladewiderstand 5W / 1kOhm", "Hochvolthandschuhe")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_pcie_lanes",
                    title = "PCIe Lane Impedanzmessung",
                    symptomTarget = "Grafikkarte wird nur mit x4 statt x16 Lanes erkannt.",
                    testMethod = "Differenzielle RX/TX Paare der PCIe-Leitungen im Diodenmodus gegen Masse prüfen.",
                    expectedResult = "Alle 16 Paare müssen nahezu identische Diodenwerte (~0.420V) aufweisen.",
                    failureMeaning = "Unterbrechung durch abgerissenen Koppelkondensator oder Sockel-Pin Schaden."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_pc_hardware",
                title = "PC-Bau, CPU, GPU & VRM Hardware Pack",
                category = "PC & Workstations",
                description = "Diagnose und Reparatur von Mainboards, Sockel-Pins, Grafikkarten-Spannungswandlern, BIOS-Flash per SPI-Programmer und RAM-Trainingsproblemen.",
                version = "2.0.1",
                sizeMb = 6.2,
                isInstalled = false,
                templatesJson = p5T,
                safetyInstructionsJson = p5S,
                diagnosticProceduresJson = p5D,
                supportedKeywordsCsv = "pc,cpu,gpu,mainboard,grafikkarte,ram,netzteil,lüfter,bios,sockel",
                iconName = "Computer"
            )
        )

        // 6. E-Bike, Scooter & BLDC Controller Pack
        val (p6T, p6S, p6D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_bldc_hall_sensors",
                    title = "BLDC Nabenmotor Hall-Sensor Austausch",
                    description = "Ersetzen defekter Hallsensoren (Typ 41F) in E-Bike Nabenmotoren bei ruckelndem Anlaufverhalten.",
                    difficulty = "Fortgeschritten",
                    estimatedMinutes = 60,
                    toolsNeeded = listOf("3x Hall-Sensoren SS41F", "Schrumpfschlauch", "Epoxid-Klebemittel hitzefest", "Lötkolben"),
                    steps = listOf(
                        "Motorgehäuse mit Abzieher öffnen; Stator vorsichtig aus Glocke heben (Achtung Neodym-Magneten!).",
                        "Alte Sensoren aus dem Statorblechpaket entnehmen und Ausrichtung (Markierung oben) notieren.",
                        "Neue Sensoren mit 5V Speisung vor Einbau auf High/Low Signalwechsel testen.",
                        "Mit hitzefestem Epoxidkleber im 120° Versatz einkleben."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_dc_high_voltage",
                    title = "Berührungsspannung an 48V/52V Akkupacks",
                    severity = "KRITISCH",
                    description = "Ab 60V Gleichspannung besteht Lebensgefahr; bei Kurzschluss entstehen Schmelzperlen und explosionsartige Lichtbögen!",
                    protectiveGear = listOf("1000V VDE isoliertes Werkzeug", "Gesichtsvisier")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_controller_mosfets",
                    title = "Controller 3-Phasen MOSFET Prüfung",
                    symptomTarget = "Hinterrad blockiert mechanisch schwergängig beim Schieben.",
                    testMethod = "Diodentest zwischen Phase (U/V/W) und Plus/Minus-Schiene des Motorcontrollers.",
                    expectedResult = "Durchlassspannung ca. 0.5V, Sperrrichtung unendlich (OL).",
                    failureMeaning = "Mindestens ein High-Side oder Low-Side MOSFET ist niederohmig durchlegiert."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_ebike_highvoltage",
                title = "E-Bike, Scooter & BLDC Controller Pack",
                category = "Mobilität & Akkus",
                description = "Fehlersuche an 36V/48V E-Bike Antrieben, bürstenlosen Motoren, Drehmomentsensoren, BMS-Zellendrift und Gasgriff-Signalen.",
                version = "1.0.8",
                sizeMb = 4.9,
                isInstalled = false,
                templatesJson = p6T,
                safetyInstructionsJson = p6S,
                diagnosticProceduresJson = p6D,
                supportedKeywordsCsv = "ebike,fahrrad,scooter,bldc,motor,controller,mosfet,hall,gasgriff",
                iconName = "DirectionsBike"
            )
        )

        // 7. Konsolen APU, HDMI & Netzteil Pack
        val (p7T, p7S, p7D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_hdmi21_port",
                    title = "HDMI 2.1 Port SMD Austausch",
                    description = "Austausch abgerissener oder verbogener HDMI-Buchsen ohne Beschädigung der inneren Massepins.",
                    difficulty = "Experte",
                    estimatedMinutes = 55,
                    toolsNeeded = listOf("Vorheizplatte 150°C", "Heißluft 380°C", "Bleihaltiges Wismut-Lot zur Schmelzpunktsenkung", "HDMI 2.1 Ersatzport"),
                    steps = listOf(
                        "Masseanker mit Sn42Bi58 Lot legieren, um Schmelztemperatur auf ~180°C zu senken.",
                        "Platine auf Unterheizplatte vorwärmen.",
                        "Defekten Port mit Heißluft von unten/oben sanft abheben.",
                        "Lötaugen mit Mikroskop prüfen; neuen Port mit Flussmittel zentrieren und verlöten."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_liquid_metal_ps5",
                    title = "Flüssigmetall-Oxidation bei APU",
                    severity = "WARNUNG",
                    description = "Oxidiertes Flüssigmetall bildet trockene Hotspots. Nur mit Isopropanol und Wattestäbchen vorsichtig polieren.",
                    protectiveGear = listOf("Schutzbrille", "Nitrilhandschuhe")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_hdmi_diodes",
                    title = "HDMI Pin 18 (+5V) & HPD Diodenwerte",
                    symptomTarget = "Konsole startet weißes Licht (WLoD), aber Fernseher meldet 'Kein Signal'.",
                    testMethod = "Diodenmodus an ESD-Schutzdioden hinter dem HDMI-Encoder messen.",
                    expectedResult = "Alle 4 Datenpaare symmetrisch ~0.550V.",
                    failureMeaning = "Schutzdioden oder HDMI-Encoder IC durch Blitzschlag / Überspannung zerstört."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_console_hdmi",
                title = "Konsolen APU, HDMI & Netzteil Pack",
                category = "Gaming & Entertainment",
                description = "PS5-, Xbox Series- und Switch-Reparaturen. HDMI-Port Tausch, BLOD/WLOD Fehlercodes, Netzteilreparatur und APU-Flüssigmetall Erneuerung.",
                version = "1.5.0",
                sizeMb = 4.1,
                isInstalled = false,
                templatesJson = p7T,
                safetyInstructionsJson = p7S,
                diagnosticProceduresJson = p7D,
                supportedKeywordsCsv = "playstation,ps5,xbox,konsole,hdmi,gaming,switch,lüfter,wlod,blod",
                iconName = "SportsEsports"
            )
        )

        // 8. Haushaltsgeräte & Kaffeevollautomaten Pack
        val (p8T, p8S, p8D) = buildJson(
            templates = listOf(
                RepairTemplateItem(
                    id = "tpl_thermoblock_descale",
                    title = "Thermoblock & Heizelement Revision",
                    description = "Beseitigung von Verkalkungen in Edelstahl-Heizrohren und Erneuerung der EPDM-Dichtringe.",
                    difficulty = "Mittel",
                    estimatedMinutes = 45,
                    toolsNeeded = listOf("Amidosulfonsäure-Entkalker", "EPDM O-Ringe Set", "Silikonfett lebensmittelecht", "Torx T20"),
                    steps = listOf(
                        "Netzstecker ziehen und Gerät 60 Minuten abkühlen lassen.",
                        "Druckschläuche durch Lösen der Sicherungssplinte entfernen.",
                        "Thermoblock demontieren, auf Lochfraß prüfen und O-Ringe fetten.",
                        "Dichtheitsprüfung bei vollem Pumpendruck (15 bar) durchführen."
                    )
                )
            ),
            safeties = listOf(
                SafetyInstructionItem(
                    id = "safe_mains_voltage",
                    title = "230V Netzspannung Lebensgefahr",
                    severity = "KRITISCH",
                    description = "In Haushaltsgeräten liegen unisolierte 230V Klemmen an Pumpen, Magnetventilen und Heizungen offen!",
                    protectiveGear = listOf("Spannungsprüfer / Duspol", "Trenn-Transformator am Prüfplatz")
                )
            ),
            diagnostics = listOf(
                DiagnosticProcedureItem(
                    id = "diag_flowmeter_pulses",
                    title = "Flowmeter Hall-Sensor Impulsprüfung",
                    symptomTarget = "Kaffeezubereitung bricht nach 2 Sekunden ab; Fehlermeldung 'Wasserkreislauf füllen'.",
                    testMethod = "Spannung am Signalpin des Flowmeters mit Oszilloskop oder Multimeter-Frequenzmessung prüfen.",
                    expectedResult = "Rechtecksignal ca. 20-50 Hz während des Wasserbezugs.",
                    failureMeaning = "Turbinenrad durch Kalk blockiert oder Hall-Sensor defekt."
                )
            )
        )
        packs.add(
            RepairSkillPackEntity(
                packId = "pack_home_appliances",
                title = "Haushaltsgeräte & Kaffeevollautomaten Pack",
                category = "Haushalt & Mechanik",
                description = "Fehlerbehebung für DeLonghi, Jura, Siemens Kaffeeautomaten, Waschmaschinen, Thermoblöcke, Pumpen, Ventile und Mikroschalter.",
                version = "1.2.0",
                sizeMb = 3.5,
                isInstalled = false,
                templatesJson = p8T,
                safetyInstructionsJson = p8S,
                diagnosticProceduresJson = p8D,
                supportedKeywordsCsv = "kaffee,kaffeemaschine,haushalt,thermoblock,pumpe,waschmaschine,ventil,delonghi",
                iconName = "Coffee"
            )
        )

        dao.insertInitialSkillPacks(packs)
    }
}
