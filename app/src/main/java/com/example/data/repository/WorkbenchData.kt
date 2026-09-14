package com.example.data.repository

import com.example.model.EcoSavingsBenchmark
import com.example.model.MaterialNorm
import com.example.model.MeasurementMode
import com.example.model.MultimeterTestPoint
import com.example.model.SmdChip

object WorkbenchData {

    enum class EvaluationStatus {
        OK, SHORT_CIRCUIT, OPEN_CIRCUIT, OUT_OF_TOLERANCE
    }

    data class EvaluationResult(
        val status: EvaluationStatus,
        val title: String,
        val message: String,
        val recommendedAction: String
    )

    fun evaluateReading(point: MultimeterTestPoint, value: Double): EvaluationResult {
        return when (point.mode) {
            MeasurementMode.DIODE -> {
                when {
                    value < 0.05 -> EvaluationResult(
                        status = EvaluationStatus.SHORT_CIRCUIT,
                        title = "Kurzschluss nach Masse (0.00 V)",
                        message = point.shortCircuitConsequence,
                        recommendedAction = "Parallel liegende Entkoppelkondensatoren prüfen oder IC mit Heißluft/Kältespray lokalisieren."
                    )
                    value > 2.0 -> EvaluationResult(
                        status = EvaluationStatus.OPEN_CIRCUIT,
                        title = "Leiterbahnunterbrechung / Hochohmig (OL)",
                        message = point.openCircuitConsequence,
                        recommendedAction = "Leiterbahn auf Abriss untersuchen oder Lötpad auf Kaltlötstelle prüfen."
                    )
                    value in point.expectedMin..point.expectedMax -> EvaluationResult(
                        status = EvaluationStatus.OK,
                        title = "Messwert im Sollbereich (${point.expectedDisplay})",
                        message = "Der Spannungsabfall der internen Schutzdiode ist intakt.",
                        recommendedAction = "Messpunkt ist elektrisch in Ordnung. Weiter mit dem nächsten Testpunkt."
                    )
                    value < point.expectedMin -> EvaluationResult(
                        status = EvaluationStatus.OUT_OF_TOLERANCE,
                        title = "Spannungsabfall zu niedrig (${value} V)",
                        message = "Erhöhter Leckstrom im Halbleiter oder teilweiser Nebenschluss.",
                        recommendedAction = "Gegentest bei Raumtemperatur durchführen; Verdächtigen Chip thermisch überwachen."
                    )
                    else -> EvaluationResult(
                        status = EvaluationStatus.OUT_OF_TOLERANCE,
                        title = "Spannungsabfall zu hoch (${value} V)",
                        message = "Übergangswiderstand an der Schutzdiode erhöht.",
                        recommendedAction = "Prüfspitzenkontakt reinigen und Lötstellen nachlöten."
                    )
                }
            }
            MeasurementMode.RESISTANCE -> {
                when {
                    value < 5.0 && point.expectedMin > 50.0 -> EvaluationResult(
                        status = EvaluationStatus.SHORT_CIRCUIT,
                        title = "Niederohmiger Kurzschluss (${value} Ω)",
                        message = point.shortCircuitConsequence,
                        recommendedAction = "Stromkreis sofort spannungsfrei halten. Bauteil entlöten und separat messen."
                    )
                    value > 10_000_000.0 || (point.expectedMax < 50_000.0 && value > 200_000.0) -> EvaluationResult(
                        status = EvaluationStatus.OPEN_CIRCUIT,
                        title = "Unterbrechung / Unendlich hochohmig (OL)",
                        message = point.openCircuitConsequence,
                        recommendedAction = "Wicklungsdraht, Thermosicherung oder Vorwiderstand erneuern."
                    )
                    value in point.expectedMin..point.expectedMax -> EvaluationResult(
                        status = EvaluationStatus.OK,
                        title = "Widerstand im Sollbereich (${point.expectedDisplay})",
                        message = "Die Wicklung bzw. der Messpunkt hat den exakten Gleichstrom-Sollwiderstand.",
                        recommendedAction = "Messung bestanden."
                    )
                    else -> EvaluationResult(
                        status = EvaluationStatus.OUT_OF_TOLERANCE,
                        title = "Widerstand weicht ab (${value} Ω)",
                        message = "Sollwert liegt bei ${point.expectedDisplay}.",
                        recommendedAction = "Messleitungen prüfen (Nullabgleich) und Messpunkt von Nachbarbauteilen isolieren."
                    )
                }
            }
            MeasurementMode.VOLTAGE -> {
                when {
                    value < (point.expectedMin * 0.7) -> EvaluationResult(
                        status = EvaluationStatus.OUT_OF_TOLERANCE,
                        title = "Spannungseinbruch (${value} V)",
                        message = point.shortCircuitConsequence,
                        recommendedAction = "Last vom Netzteil trennen; LDO oder Abwärtswandler prüfen."
                    )
                    value in point.expectedMin..point.expectedMax -> EvaluationResult(
                        status = EvaluationStatus.OK,
                        title = "Spannung im Nennbereich (${point.expectedDisplay})",
                        message = "Spannungsschiene ist stabil.",
                        recommendedAction = "Schiene in Ordnung."
                    )
                    else -> EvaluationResult(
                        status = EvaluationStatus.OUT_OF_TOLERANCE,
                        title = "Überspannung gemessen (${value} V)",
                        message = "Achtung: Gefahr von Bauteilschäden durch Überspannung!",
                        recommendedAction = "Sofort abschalten! Spannungsregler / Feedback-Widerstand prüfen."
                    )
                }
            }
        }
    }

    val multimeterTestPoints: List<MultimeterTestPoint> = listOf(
        MultimeterTestPoint(
            id = "atx_ps_on",
            category = "PC ATX Netzteil",
            testPointName = "PS_ON# (Pin 16 Grün) - Büroklammer-Test",
            mode = MeasurementMode.VOLTAGE,
            expectedMin = 3.0,
            expectedMax = 5.25,
            expectedDisplay = "3.3V - 5.0V (High im Standby)",
            description = "Signal zum Einschalten des Netzteils. Liegt im Standby auf High (grünes Kabel).",
            pinoutNotes = "Pin 16 (Grün) gegen Pin 15/17 (COM / Schwarz).",
            testMethod = "Multimeter DC 20V: Schwarze Spitze an COM, rote Spitze an Pin 16. Büroklammer-Test: Pin 16 mit Masse brücken, Lüfter muss anlaufen!",
            shortCircuitConsequence = "Dauerhafter Kurzschluss nach Masse schaltet das Netzteil sofort unkontrolliert ein.",
            openCircuitConsequence = "Netzteil lässt sich über den Gehäuse-Power-Taster nicht starten."
        ),
        MultimeterTestPoint(
            id = "atx_pwr_ok",
            category = "PC ATX Netzteil",
            testPointName = "PWR_OK / Power Good (Pin 8 Grau)",
            mode = MeasurementMode.VOLTAGE,
            expectedMin = 4.5,
            expectedMax = 5.25,
            expectedDisplay = "5.0V (aktiv nach 100-500ms)",
            description = "Meldet dem Mainboard, dass alle internen Spannungen (+12V, +5V, +3.3V) stabil anliegen.",
            pinoutNotes = "Pin 8 (Grau) gegen Masse (Schwarz).",
            testMethod = "Netzteil einschalten. Nach ca. 0,3s muss Pin 8 auf +5V springen.",
            shortCircuitConsequence = "Ohne PWR_OK bleibt das Mainboard dauerhaft im Reset-Zustand (Lüfter drehen, kein Bild).",
            openCircuitConsequence = "Rechner schaltet nach 1 Sekunde wieder ab oder bootet nicht."
        ),
        MultimeterTestPoint(
            id = "atx_5vsb",
            category = "PC ATX Netzteil",
            testPointName = "+5VSB Standby-Spannung (Pin 9 Lila)",
            mode = MeasurementMode.VOLTAGE,
            expectedMin = 4.75,
            expectedMax = 5.25,
            expectedDisplay = "5.0V DC Dauerstrom",
            description = "Versorgt BIOS, Wake-on-LAN und Front-Taster, sobald das Netzkabel eingesteckt ist.",
            pinoutNotes = "Pin 9 (Lila) gegen Masse.",
            testMethod = "Multimeter DC 20V. Muss auch bei ausgeschaltetem PC exakt 5V zeigen.",
            shortCircuitConsequence = "Kurzschluss auf +5VSB lässt das Netzteil 'ticken' (Hiccup Mode) oder gar nicht anlaufen.",
            openCircuitConsequence = "PC reagiert überhaupt nicht auf den Power-Knopf (keine LED leuchtet)."
        ),
        MultimeterTestPoint(
            id = "esp32_3v3_rail",
            category = "ESP32 & Arduino",
            testPointName = "ESP32 3.3V Rail Widerstand gegen Masse (GND)",
            mode = MeasurementMode.RESISTANCE,
            expectedMin = 10_000.0,
            expectedMax = 500_000.0,
            expectedDisplay = "> 10.000 Ω (10 kΩ - 500 kΩ)",
            description = "Widerstandsmessung der Hauptversorgungsleitung bei spannungsfreiem Board.",
            pinoutNotes = "3V3 Pin gegen GND Pin.",
            testMethod = "Board komplett von USB und Akku trennen! Multimeter auf 200 kΩ stellen, rote Spitze an 3V3, schwarze an GND.",
            shortCircuitConsequence = "Unter 10 Ω: ESP32 Chip oder Onboard-LDO (AMS1117) durch Überspannung durchgeschlagen. Board wird heiß!",
            openCircuitConsequence = "Über 1 MΩ bei frisch entladenem Board kann normal sein, wenn Kondensatoren entladen sind."
        ),
        MultimeterTestPoint(
            id = "esp32_gpio_diode",
            category = "ESP32 & Arduino",
            testPointName = "ESP32 GPIO ESD-Schutzdiode (Diodentest)",
            mode = MeasurementMode.DIODE,
            expectedMin = 0.520,
            expectedMax = 0.720,
            expectedDisplay = "0.550 V - 0.700 V",
            description = "Prüfung der internen ESD-Klemmdiode eines GPIO-Pins gegen Masse.",
            pinoutNotes = "Rote Spitze an GND (!), schwarze Spitze an den GPIO Pin.",
            testMethod = "Multimeter im Diodentest: ROTE Prüfspitze an Masse legen, SCHWARZE Prüfspitze an den GPIO (z.B. GPIO 18).",
            shortCircuitConsequence = "0.000 V: GPIO durch statische Entladung (ESD) oder 5V Überspannung dauerhaft zerstört.",
            openCircuitConsequence = "OL / Unendlich: Interner Bonddraht im IC-Gehäuse geschmolzen."
        ),
        MultimeterTestPoint(
            id = "usbc_data_lines",
            category = "USB-C & Ladebuchsen",
            testPointName = "USB-C D+ / D- Datenleitungen (Diodenabfall)",
            mode = MeasurementMode.DIODE,
            expectedMin = 0.580,
            expectedMax = 0.750,
            expectedDisplay = "0.600 V - 0.700 V",
            description = "Test der USB 2.0 Differenzial-Datenpaare in Smartphones, Konsolen und Notebooks.",
            pinoutNotes = "Rote Spitze an USB-C Metallschirm (GND), schwarze Spitze an D+ bzw. D-.",
            testMethod = "USB-C Breakout-Board oder Messnadel an Buchsen-Pins D+ / D- führen.",
            shortCircuitConsequence = "0.00 V: TVS-Schutzdiode oder SoC-USB-PHY durch defektes Billig-Netzteil zerstört. PC erkennt Gerät nicht.",
            openCircuitConsequence = "OL: Buchsenpin verbogen, abgerissen oder Schutzwiderstand defekt. Schnelles Laden/Daten scheitert."
        ),
        MultimeterTestPoint(
            id = "usbc_cc_pins",
            category = "USB-C & Ladebuchsen",
            testPointName = "USB-C CC1 & CC2 Konfigurations-Widerstand",
            mode = MeasurementMode.RESISTANCE,
            expectedMin = 4800.0,
            expectedMax = 5400.0,
            expectedDisplay = "5.100 Ω (5.1 kΩ ±10%)",
            description = "UFP-Geräte (z.B. Tastaturen, ESP32, Handys) müssen an CC1/CC2 je 5.1 kΩ nach Masse besitzen.",
            pinoutNotes = "Pin CC1 bzw. CC2 gegen GND.",
            testMethod = "Widerstandsmessung bei abgestecktem Kabel. Fehlt dieser Widerstand, lädt das Gerät an USB-C-auf-USB-C Netzteilen NICHT!",
            shortCircuitConsequence = "Kurzschluss verhindert Power Delivery Aushandlung (bleibt bei 0V / 5V 100mA).",
            openCircuitConsequence = "Offener CC-Pin führt dazu, dass moderne USB-PD Ladegeräte gar keinen Strom freischalten."
        ),
        MultimeterTestPoint(
            id = "li_ion_voltage",
            category = "Lithium-Ionen Akkus",
            testPointName = "18650 / LiPo Zellenspannung (Ruhespannung)",
            mode = MeasurementMode.VOLTAGE,
            expectedMin = 3.00,
            expectedMax = 4.25,
            expectedDisplay = "3.00V (Leer) bis 4.20V (Voll)",
            description = "Sicherheitsprüfung von zylindrischen und Pouch-Zellen vor dem Laden.",
            pinoutNotes = "Pluspol (flache Noppe) gegen Minuspol (Boden).",
            testMethod = "Multimeter DC 20V. 4.20V = 100%, 3.70V = 50% Nennspannung, 3.00V = 0%.",
            shortCircuitConsequence = "< 2.50V: TIEFENTLADUNG! Kupferbrücken bilden sich im Separator. Bei erneutem Schnellladen besteht akute Brandgefahr!",
            openCircuitConsequence = "0.00V: BMS hat getrennt oder interner CID-Überdruckschalter hat ausgelöst."
        ),
        MultimeterTestPoint(
            id = "washer_pump_coil",
            category = "Haushaltsgeräte",
            testPointName = "Waschmaschinen Laugenpumpe (Spulenwiderstand)",
            mode = MeasurementMode.RESISTANCE,
            expectedMin = 150.0,
            expectedMax = 240.0,
            expectedDisplay = "160 Ω - 220 Ω",
            description = "Messung der Antriebsspule der Entleerungspumpe (z.B. Bosch, Siemens, Miele).",
            pinoutNotes = "Beide Flachstecker-Anschlüsse der Pumpe (Kabel vorher abziehen!).",
            testMethod = "Gerät vom 230V Netz trennen! Multimeter 2000 Ω Bereich an beide Pumpenkontakte halten.",
            shortCircuitConsequence = "< 50 Ω: Wicklungsschluss. Zerstört beim Starten sofort den Triac auf der Steuerelektronik!",
            openCircuitConsequence = "OL / Unendlich: Thermische Sicherung im Wicklungsdraht durch Fremdkörper-Blockade durchgebrannt."
        ),
        MultimeterTestPoint(
            id = "heater_isolation",
            category = "Haushaltsgeräte",
            testPointName = "Heizstab Isolationswiderstand gegen Schutzleiter (PE)",
            mode = MeasurementMode.RESISTANCE,
            expectedMin = 2_000_000.0,
            expectedMax = 100_000_000.0,
            expectedDisplay = "> 2.000.000 Ω (> 2 MΩ)",
            description = "Prüfung auf Riss im Edelstahlmantel und Feuchtigkeit im Magnesiumoxid-Füllstoff.",
            pinoutNotes = "Ein Heizstab-Kontakt gegen den Masse-/Erdungsflansch (PE).",
            testMethod = "Gerät ausgesteckt! Höchsten Widerstandsbereich (20 MΩ) oder Isolationsprüfer nutzen.",
            shortCircuitConsequence = "< 2 MΩ: Leckstrom überschreitet 30mA -> Fehlerstrom-Schutzschalter (FI / RCD) löst sofort aus!",
            openCircuitConsequence = "Unendlich (> 20 MΩ) ist hier der ideale Sollzustand."
        ),
        MultimeterTestPoint(
            id = "switch_m92t36_vbus",
            category = "Gaming & Konsolen",
            testPointName = "Nintendo Switch M92T36 Pin 5/6 VBUS Diodentest",
            mode = MeasurementMode.DIODE,
            expectedMin = 0.400,
            expectedMax = 0.520,
            expectedDisplay = "0.450 V ±10%",
            description = "Standard-Diagnose bei Nintendo Switch 'Lädt nicht' oder 'Schwarzer Bildschirm'.",
            pinoutNotes = "Kondensator direkt oberhalb von Pin 5 & 6 des M92T36 ICs.",
            testMethod = "Akku abstecken! Rote Spitze an Mainboard-Kupfer-Masse, schwarze Spitze an den Kondensator über Pin 5/6.",
            shortCircuitConsequence = "0.000 V: M92T36 Power-Management IC durch Spannungsspitze zerstört (oft nach Dritthersteller-Dock). Muss ersetzt werden!",
            openCircuitConsequence = "OL: Lötpad abgerissen oder Eingangs-Drosselspule durchgebrannt."
        )
    )

    val smdChips: List<SmdChip> = listOf(
        SmdChip(
            codeOrName = "M92T36",
            fullPartNumber = "ROHM Semiconductor M92T36",
            manufacturer = "ROHM Semiconductor",
            packageType = "QFN-40 (5x5 mm)",
            functionCategory = "USB Type-C & Power Delivery Controller",
            description = "Verantwortlich für die USB-C Verhandlung, 15V Schnellladung und Dock-Erkennung in der Nintendo Switch.",
            operatingRatings = "Eingang: bis 20V VBUS, Standby-Strom < 50µA",
            pinoutSummary = listOf(
                "Pin 5 & 6: VBUS Eingang (Kondensator-Sollwert 0.45V im Diodentest)",
                "Pin 1 & 38: Audio-Filter / Audio-Routing",
                "Pin 32 & 33: CC1 / CC2 Kommunikation mit dem USB-C Netzteil",
                "Thermal Pad (GND): Großes Lötpad auf der Unterseite"
            ),
            typicalFailures = "Zerstörung durch billige Drittanbieter-Docks (Überspannung auf CC-Pins). Symptom: Switch bleibt bei 0.46A Ladeleistung hängen oder schaltet gar nicht ein.",
            dropInEquivalents = listOf("M92T36 (Original ROHM)", "M92T17 (eingeschränkt kompatibel nur für ältere Revisionen)"),
            testProcedure = "Diodentest an den Entkoppelkondensatoren rings um den Chip. Zeigt der Kondensator über Pin 5/6 0.00V gegen Masse, ist der Chip defekt."
        ),
        SmdChip(
            codeOrName = "BQ24193",
            fullPartNumber = "Texas Instruments BQ24193RGER",
            manufacturer = "Texas Instruments",
            packageType = "VQFN-24 (4x4 mm)",
            functionCategory = "Li-Ion Akku Lade- & System-Power-Management",
            description = "Schaltendes I2C-Lade-IC mit NVDC-1 Power-Path Management. Versorgt das System und lädt den Li-Ion Akku.",
            operatingRatings = "Eingang: 3.9V bis 17V, Ladestrom programmierbar bis 4.5A",
            pinoutSummary = listOf(
                "Pin 1 (VBUS): Stromeingang von USB",
                "Pin 13 & 14 (SW): Schaltknoten zur Speicherinduktivität",
                "Pin 15 & 16 (SYS): Systemspannungs-Ausgang (ca. 3.8V-4.2V)",
                "Pin 17 & 18 (BAT): Akku-Anschluss"
            ),
            typicalFailures = "Kondensator an Pin 1 (VBUS) oder Pin 15 (SYS) hat Kurzschluss nach Masse. Symptom: Konsole lädt nicht oder schaltet unter Last sofort ab.",
            dropInEquivalents = listOf("BQ24193RGER (Original)", "BQ24192 (baugleich mit anderer I2C Default-Adresse)", "BQ24190"),
            testProcedure = "Widerstand an der großen Speicherdrossel (Induktivität) gegen Masse messen. Sollwert > 10 kΩ."
        ),
        SmdChip(
            codeOrName = "AMS1117-3.3",
            fullPartNumber = "Advanced Monolithic Systems AMS1117-3.3",
            manufacturer = "AMS / Verschiedene Hersteller",
            packageType = "SOT-223 / TO-252",
            functionCategory = "Linearer Low-Dropout (LDO) Spannungsregler",
            description = "Der allgegenwärtige 3.3V Linearregler auf Arduino-, ESP8266- und ESP32-Entwicklungsboards.",
            operatingRatings = "Eingang: max. 15V (empfohlen max. 5-9V), Ausgang: 3.3V fest, Strom: max. 1.0A",
            pinoutSummary = listOf(
                "Pin 1 (links): Masse (GND / ADJ)",
                "Pin 2 & Kühlfahne (Mitte): VOUT (+3.3V Ausgang)",
                "Pin 3 (rechts): VIN (Eingangsspannung +5V)"
            ),
            typicalFailures = "Überhitzung bei mehr als 7V Eingangsspannung. Typischer Fehler: Chip schlägt durch und gibt die vollen 12V an den ESP32 weiter -> Zerstört den Mikrocontroller!",
            dropInEquivalents = listOf("LM1117-3.3", "LD1117V33", "NCP1117-3.3", "ME6211 (deutlich geringerer Ruhestrom & Drop-out)"),
            testProcedure = "Spannung an Pin 2 messen: Muss exakt 3.3V ±0.05V betragen. Wird der Chip im Leerlauf heiß, liegt ein Kurzschluss am 3.3V Bus vor."
        ),
        SmdChip(
            codeOrName = "TP4056",
            fullPartNumber = "NanJing Top Power TP4056",
            manufacturer = "Top Power ASIC",
            packageType = "SOP-8 / ESOP-8 mit Exposed Pad",
            functionCategory = "1A Standalone Li-Ion Linear-Ladegerät",
            description = "Standard-Lade-IC in wiederaufladbaren Maker-Projekten, LED-Leuchten und DIY-Powerbanks.",
            operatingRatings = "Eingangsspannung: 4.0V bis 8.0V (ideal 5V USB), Ladeschlussspannung: exakt 4.20V ±1%",
            pinoutSummary = listOf(
                "Pin 2 (PROG): Ladestrom-Einstellung über Widerstand Rprog nach GND (1.2kΩ = 1000mA, 2.4kΩ = 500mA)",
                "Pin 4 (VCC): 5V Eingang",
                "Pin 5 (BAT): Akku-Pluspol",
                "Pin 6 (STDBY) & Pin 7 (CHRG): Status-LEDs (Grün/Rot)"
            ),
            typicalFailures = "Verpolung am Akku-Ausgang zerstört den TP4056 sofort (besitzt keinen Verpolungsschutz!). Symptom: Chip brennt durch oder Lade-LEDs glimmen nur noch schwach.",
            dropInEquivalents = listOf("TC4056A", "TP4056E", "LP4056"),
            testProcedure = "Spannung an Pin 5 ohne Akku messen: Soll 4.2V pulsieren. Widerstand an Pin 2 gegen GND messen, um programmierten Strom zu prüfen."
        ),
        SmdChip(
            codeOrName = "L298N",
            fullPartNumber = "STMicroelectronics L298N / L298P",
            manufacturer = "STMicroelectronics",
            packageType = "Multiwatt15 / PowerSO20 (SMD)",
            functionCategory = "Dual Full-Bridge Motortreiber (H-Brücke)",
            description = "Klassischer Treiber für 2 Gleichstrommotoren oder einen 4-Phasen Schrittmotor in Robotik-Projekten.",
            operatingRatings = "Motorspannung: bis 46V, Spitzenstrom: 2A pro Brücke, Logikpegel: 5V TTL",
            pinoutSummary = listOf(
                "Pins OUT1 & OUT2: Motor A Ausgang",
                "Pins OUT3 & OUT4: Motor B Ausgang",
                "Pins IN1 bis IN4: Logikeingänge vom Mikrocontroller",
                "Pins ENA & ENB: PWM Geschwindigkeitsregelung (mit Jumper auf 5V High)"
            ),
            typicalFailures = "Spannungsabfall von ca. 1.4V bis 2.0V über den internen Bipolar-Transistoren erzeugt enorme Hitze. Ohne Freilaufdioden zerstören Induktionsspitzen die H-Brücke.",
            dropInEquivalents = listOf("TB6612FNG (moderner MOSFET-Treiber mit 95% weniger Abwärme!)", "DRV8833", "L293D (kleinere Leistung bis 600mA)"),
            testProcedure = "Diodentest an OUT-Pins gegen GND: Interne Schutzdioden prüfen. Bei 0.00V ist die jeweilige Brückenhälfte durchlegiert."
        ),
        SmdChip(
            codeOrName = "CH340G",
            fullPartNumber = "Jiangsu Qinheng Microelectronics CH340G",
            manufacturer = "WCH",
            packageType = "SOP-16",
            functionCategory = "USB zu UART / Serial Interface Wandler",
            description = "Weit verbreiteter USB-Interface-Chip auf chinesischen ESP32- und Arduino Nano-Boards.",
            operatingRatings = "Versorgung: 3.3V oder 5V, Baudraten von 50 bps bis 2 Mbps",
            pinoutSummary = listOf(
                "Pin 2 (TXD): Sendesignal zum ESP32 RX-Pin",
                "Pin 3 (RXD): Empfangssignal vom ESP32 TX-Pin",
                "Pin 5 (V3): 3.3V Stabilisierungskondensator (0.1µF zwingend!)",
                "Pin 7 & 8 (XI / XO): 12 MHz Quarz-Anschluss"
            ),
            typicalFailures = "PC meldet 'Unbekanntes USB-Gerät (Fehlercode 43)'. Ursache meist fehlender CH340-Treiber, defekter 12MHz Quarz oder abgerissene USB-Datenleitungen.",
            dropInEquivalents = listOf("CH340C (integrierter Quarz)", "CP2102", "FT232RL (andere Pinbelegung)"),
            testProcedure = "Loopback-Test: Pin 2 (TX) mit Pin 3 (RX) kurzschließen. Im Terminal getippte Zeichen müssen 1:1 zurückgesendet werden."
        ),
        SmdChip(
            codeOrName = "A7",
            fullPartNumber = "BAV99 Dual Switching Diode",
            manufacturer = "NXP / Diodes Inc. / ON Semi",
            packageType = "SOT-23 (Code Aufdruck 'A7')",
            functionCategory = "High-Speed Doppeldiode in Reihe",
            description = "Extrem häufiger SMD-Code auf PC-Mainboards, Grafikkarten und Tastaturen zum Überspannungsschutz.",
            operatingRatings = "Sperrspannung: 70V, Durchlassstrom: 215mA, Schaltzeit: 4 ns",
            pinoutSummary = listOf(
                "Pin 1: Anode Diode 1",
                "Pin 2: Kathode Diode 2",
                "Pin 3: Gemeinsamer Knotenpunkt (Kathode 1 / Anode 2)"
            ),
            typicalFailures = "Durchschlag bei Überspannung oder ESD-Ereignis an I/O-Ports. Zieht das Signal dauerhaft auf Masse.",
            dropInEquivalents = listOf("BAV99W (SOT-323)", "MMBD7000", "1N4148 (Bedrahtet)"),
            testProcedure = "Diodentest: Pin 1 nach Pin 3 = 0.65V; Pin 3 nach Pin 2 = 0.65V. In Gegenrichtung muss OL angezeigt werden."
        ),
        SmdChip(
            codeOrName = "1AM",
            fullPartNumber = "MMBT3904 NPN Bipolartransistor",
            manufacturer = "ON Semiconductor / Verschiedene",
            packageType = "SOT-23 (Code Aufdruck '1AM')",
            functionCategory = "Universal NPN Kleinsignal-Transistor",
            description = "Standard SMD-Transistor für Treiberschaltungen, Relais, LEDs und Pegelwandler.",
            operatingRatings = "Vceo: 40V, Kollektorstrom Ic: 200mA, hFE: 100 bis 300",
            pinoutSummary = listOf(
                "Pin 1 (unten links): Basis (B)",
                "Pin 2 (unten rechts): Emitter (E)",
                "Pin 3 (oben Mitte): Kollektor (C)"
            ),
            typicalFailures = "Basis-Emitter Überlastung durch fehlenden Vorwiderstand. Symptom: Transistor sperrt nicht mehr oder schaltet nicht durch.",
            dropInEquivalents = listOf("2N3904 (TO-92)", "BC847", "PMBT3904"),
            testProcedure = "Diodentest von Basis (Pin 1) zu Kollektor (Pin 3) = ca. 0.68V; Basis zu Emitter (Pin 2) = ca. 0.70V."
        ),
        SmdChip(
            codeOrName = "2N7002",
            fullPartNumber = "2N7002 N-Channel Enhancement MOSFET",
            manufacturer = "Vishay / Diodes Inc.",
            packageType = "SOT-23 (Code oft '702' oder 'K72')",
            functionCategory = "N-Kanal Signal-MOSFET (Logic Level)",
            description = "Der Standard-MOSFET für I2C-Pegelwandler (3.3V <-> 5V) und digitale Schaltsignale.",
            operatingRatings = "Vds: 60V, Id: 115mA (Dauer), Gate-Schwellenspannung Vgs(th): 1.0V bis 2.5V, Rds(on): ca. 5 Ω",
            pinoutSummary = listOf(
                "Pin 1: Gate (G)",
                "Pin 2: Source (S)",
                "Pin 3: Drain (D)"
            ),
            typicalFailures = "Gate-Oxid Durchschlag durch ESD (statische Elektrizität). Symptom: Gate hat Durchgang zu Drain oder Source.",
            dropInEquivalents = listOf("BSS138 (geringere Gate-Schwelle, ideal für 3.3V Pegel)", "FDN337N"),
            testProcedure = "Widerstandsmessung Gate gegen Source: Muss absolut hochohmig (> 10 MΩ) sein. Zeigt das Multimeter einen Durchgang, ist der MOSFET zerstört."
        ),
        SmdChip(
            codeOrName = "IRFZ44N",
            fullPartNumber = "Infineon / International Rectifier IRFZ44NPBF",
            manufacturer = "Infineon Technologies",
            packageType = "TO-220AB (Durchsteck) / D2PAK (SMD)",
            functionCategory = "N-Kanal Leistungs-MOSFET (Power Switch)",
            description = "Arbeitspferd für DC-Motorsteuerungen, Heizbetten bei 3D-Druckern, KFZ-Schaltungen und Hochstrom-Schalter.",
            operatingRatings = "Vds: 55V, Id: 49A, Rds(on): 17.5 mΩ bei 10V Vgs, Verlustleistung: 94W",
            pinoutSummary = listOf(
                "Pin 1 (links): Gate (G) - benötigt 100Ω Vorwiderstand & 10kΩ Pulldown!",
                "Pin 2 & Kühlfahne: Drain (D) - Lastseite",
                "Pin 3 (rechts): Source (S) - Massebezug"
            ),
            typicalFailures = "Überhitzung, wenn das Gate direkt mit 3.3V ESP32 Pins angesteuert wird (IRFZ44N benötigt 10V Vgs für volles Durchschalten; bei 3.3V arbeitet er im linearen Bereich und verglüht!).",
            dropInEquivalents = listOf("IRLZ44N (Echter Logic-Level MOSFET, schaltet bereits bei 3.3V-5V voll durch!)", "STP55NF06"),
            testProcedure = "Diodentest von Source nach Drain: Interne Body-Diode muss ca. 0.52V anzeigen. Drain nach Source muss sperren."
        )
    )

    val materialNorms: List<MaterialNorm> = listOf(
        MaterialNorm(
            category = "Dichtungen & O-Ringe",
            title = "EPDM (Ethylen-Propylen-Dien-Kautschuk)",
            subTitle = "Lebensmittelechte Heißwasser- & Dampfdichtungen",
            temperatureRange = "-40°C bis +140°C (kurzzeitig +160°C)",
            materialSpecs = "Härte 70 Shore A, FDA / LFGB zertifiziert, schwarz oder rot.",
            applicationAreas = "Kaffeevollautomaten (Brüheinheit, Thermoblock-Druckschläuche), Waschmaschinen-Einlauf, Spülmaschinen.",
            criticalWarnings = "NIEMALS mit mineralölbasierten Fetten (WD-40, Vaseline, Motoröl) schmieren! EPDM quillt bei Kontakt mit Mineralöl auf und zersetzt sich innerhalb von Wochen! Ausschließlich reines Silikonfett (z.B. OKS 1110) verwenden!"
        ),
        MaterialNorm(
            category = "Dichtungen & O-Ringe",
            title = "NBR (Nitrilkautschuk / Perbunan)",
            subTitle = "Standard-Werkstoff für Schmieröle, Benzin & Mechanik",
            temperatureRange = "-30°C bis +100°C",
            materialSpecs = "Härte 70 Shore A, hohe Abriebfestigkeit, schwarz.",
            applicationAreas = "KFZ-Bereich, Rasenmäher, Stoßdämpfer, Hydraulik, Druckluft-Kupplungen.",
            criticalWarnings = "Ungeeignet für Heißwasser- und Dampfanwendungen über 100°C (versprödet und verliert Elastizität). Nicht beständig gegen Bremsflüssigkeit (DOT 4/5)!"
        ),
        MaterialNorm(
            category = "Wärmeleitmaterial",
            title = "Wärmeleitpads (Thermal Pads) - Dicken & Härte",
            subTitle = "Spaltüberbrückung für Grafikkarten (VRAM) & M.2 SSDs",
            temperatureRange = "-50°C bis +180°C",
            materialSpecs = "Dicken: 0.5 mm, 1.0 mm, 1.5 mm, 2.0 mm. Wärmeleitfähigkeit: 6.0 bis 14.0 W/mK.",
            applicationAreas = "Grafikkarten-Speicherchips (VRAM), Mainboard-VRM Spannungswandler, NVMe M.2 Passivkühler.",
            criticalWarnings = "Schutzfolie auf beiden Seiten vor Montage zwingend abziehen! Ein zu dickes Wärmeleitpad (z.B. 1.5 mm statt 0.5 mm) verhindert, dass der Haupt-Kühlkörper auf der GPU plan aufliegt -> Führt innerhalb von Sekunden zur Notabschaltung!"
        ),
        MaterialNorm(
            category = "18650 Akkuzellen",
            title = "High-Capacity vs. High-Drain Li-Ion Zellen",
            subTitle = "Zellchemie-Auswahl für E-Bikes, Werkzeuge & Roboter",
            temperatureRange = "Laden: 0°C bis 45°C | Entladen: -20°C bis 60°C",
            materialSpecs = "Format 18650 (18 mm Durchmesser, 65 mm Länge). Nennspannung 3.6V/3.7V, Ladeschluss 4.20V.",
            applicationAreas = "High-Capacity (Samsung 35E, LG MJ1, 3500mAh, max 8-10A) für E-Bikes & Powerbanks. High-Drain (Sony VTC6, Samsung 25R/30Q, Molicel P28A, 2500-3000mAh, max 25-35A) für Akkuschrauber, Sauger & RC-Modelle.",
            criticalWarnings = "Niemals High-Capacity-Zellen in Elektrowerkzeugen einsetzen! Der Anlaufstrom von Motoren überlastet die Zelle thermisch -> Gefahr von Thermal Runaway (Selbstentzündung)!"
        ),
        MaterialNorm(
            category = "Schrauben & Bit-Normen",
            title = "Feinmechanik-Bits & Platinenschrauben",
            subTitle = "Tri-Point, Pentalobe, Torx & Gehäusegewinde",
            temperatureRange = null,
            materialSpecs = "S2 Werkzeugstahl gehärtet (HRC 58-62) oder Chrom-Vanadium.",
            applicationAreas = "Pentalobe P2 (0.8mm): iPhone Gehäuseunterseite. Tri-Point Y000 (0.6mm): iPhone Display-Kabelklammern & Apple Watch. Torx T5/T6: Laptops, MacBooks, Konsolen. 6-32 UNC: PC-Gehäuse & Mainboard-Standoffs.",
            criticalWarnings = "Niemals Schlitz- oder falsche Phillips-Bits bei Y000 Tri-Point Schrauben ansetzen! Der Schraubenkopf dreht sofort rund ('Stripped Screw'), was das Lösen ohne Dremel oder Bohrer unmöglich macht."
        )
    )

    val ecoBenchmarks: List<EcoSavingsBenchmark> = listOf(
        EcoSavingsBenchmark("smartphone", "Smartphones", co2SavedKg = 65.0, eWasteSavedKg = 0.19, averageNewCostEuro = 580.0),
        EcoSavingsBenchmark("laptop", "Laptops & PCs", co2SavedKg = 260.0, eWasteSavedKg = 2.10, averageNewCostEuro = 920.0),
        EcoSavingsBenchmark("tablet", "Tablets", co2SavedKg = 110.0, eWasteSavedKg = 0.55, averageNewCostEuro = 480.0),
        EcoSavingsBenchmark("haushalt", "Haushaltsgeräte", co2SavedKg = 280.0, eWasteSavedKg = 35.00, averageNewCostEuro = 550.0),
        EcoSavingsBenchmark("gaming", "Gaming & Konsolen", co2SavedKg = 175.0, eWasteSavedKg = 4.20, averageNewCostEuro = 490.0),
        EcoSavingsBenchmark("audio", "Audio & Hi-Fi", co2SavedKg = 45.0, eWasteSavedKg = 0.85, averageNewCostEuro = 220.0),
        EcoSavingsBenchmark("fahrrad", "Fahrrad & E-Bike", co2SavedKg = 195.0, eWasteSavedKg = 21.00, averageNewCostEuro = 2100.0),
        EcoSavingsBenchmark("fahrzeug", "Fahrzeuge & KFZ", co2SavedKg = 420.0, eWasteSavedKg = 35.00, averageNewCostEuro = 1400.0),
        EcoSavingsBenchmark("pc_build", "PC-Bau & Hardware", co2SavedKg = 340.0, eWasteSavedKg = 11.50, averageNewCostEuro = 1150.0),
        EcoSavingsBenchmark("maker_robotics", "Robotik & Maker", co2SavedKg = 25.0, eWasteSavedKg = 0.45, averageNewCostEuro = 95.0)
    )

    fun getBenchmarkForCategory(categoryHint: String): EcoSavingsBenchmark {
        val lower = categoryHint.lowercase()
        if (lower.contains("kaffee") || lower.contains("coffee") || lower.contains("haushalt")) {
            return ecoBenchmarks.first { it.categoryId == "haushalt" }
        }
        return ecoBenchmarks.find { lower.contains(it.categoryId) || lower.contains(it.categoryName.lowercase()) }
            ?: EcoSavingsBenchmark("general", "Elektronik & Geräte", co2SavedKg = 120.0, eWasteSavedKg = 3.5, averageNewCostEuro = 400.0)
    }
}
