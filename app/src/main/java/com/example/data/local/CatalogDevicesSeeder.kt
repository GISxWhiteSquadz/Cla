package com.example.data.local

import org.json.JSONArray
import org.json.JSONObject

object CatalogDevicesSeeder {

    fun seedOtherDeviceProfiles(
        profiles: MutableList<DeviceProfileEntity>,
        components: MutableList<DeviceComponentEntity>
    ) {
        // -------------------------------------------------------------
        // SMARTPHONES
        // -------------------------------------------------------------
        val iphone14ProKey = "APPLE_IPHONE_14_PRO"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = iphone14ProKey,
                category = "Smartphones",
                subcategory = "Smartphones (iOS)",
                manufacturer = "Apple",
                modelName = "iPhone 14 Pro",
                modelNumber = "A2890 / A2650",
                releaseYear = "2022",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Display").put("label", "Panel-Typ").put("value", "6.1\" Super Retina XDR OLED (120Hz ProMotion)"))
                    put(JSONObject().put("category", "Akku").put("label", "Kapazität").put("value", "3200 mAh (12.38 Wh) Li-Ion"))
                    put(JSONObject().put("category", "Schrauben").put("label", "Gehäuse").put("value", "2x Pentalobe P2 (0.8 mm) & Tri-Point Y000"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Hard Reset: Lauter kurz drücken, Leiser kurz drücken, Power gedrückt halten bis Apple-Logo erscheint.")
                    put("DFU Mode: Gerät verbinden, Lauter kurz, Leiser kurz, Power 10s, dann Power + Leiser 5s, dann nur Leiser halten.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Error 4013").put("description", "iTunes Wiederherstellungsfehler").put("cause", "Kurzschluss am Annäherungssensor/Hörmuschel-Flexkabel").put("solution", "Hörmuschel-Flexkabel vorübergehend abstecken und Wiederherstellung erneut testen."))
                    put(JSONObject().put("code", "Unbekanntes Bauteil").put("description", "iOS meldet nicht verifiziertes Display/Akku").put("cause", "Ersatzteil ohne Seriennummern-Kopplung").put("solution", "Original BMS-Platine übernehmen oder Apple Diagnostics ausführen."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = iphone14ProKey,
                componentName = "Super Retina XDR OLED Display",
                componentCategory = "Display & Glas",
                partNumberOrSpec = "Apple OLED Display Assembly mit True Tone EEPROM",
                difficulty = "Mittel",
                estimatedTimeMinutes = 40,
                toolsJson = JSONArray().apply {
                    put("Pentalobe P2 (0.8mm)")
                    put("Tri-Point Y000 (0.6mm)")
                    put("Display-Heizkissen / Heißluft 75°C")
                    put("Saugnapf & dünnes Plektrum")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Akku vor dem Öffnen unter 25% entladen (Brandgefahr bei Stichbeschädigung).")
                    put("Display klappt nach RECHTS auf. Flexkabel nicht überdehnen!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Pentalobe Schrauben lösen").put("description", "Die 2 Pentalobe P2 Schrauben neben der Ladebuchse entfernen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Display erwärmen & öffnen").put("description", "Displayränder auf 75°C erwärmen, Saugnapf ansetzen und Kleberichtung mit Plektrum trennen.").put("caution", "Nicht tiefer als 3 mm einstechen."))
                    put(JSONObject().put("stepNumber", 3).put("title", "Akku trennen").put("description", "Abdeckblech der Akkukonnektoren mit Y000 lösen und Akkustecker mit Kunststoffspatel abhebeln.").put("caution", "Immer zuerst Akku trennen!"))
                    put(JSONObject().put("stepNumber", 4).put("title", "Displaykabel lösen").put("description", "Display- und Sensor-Flexkabel trennen. Neues Display mit frischer IP68-Dichtung montieren.").put("caution", null))
                }.toString(),
                notes = "True Tone EEPROM kann mit Display-Programmierer geklont werden."
            )
        )

        // Samsung Galaxy S23
        val s23Key = "SAMSUNG_GALAXY_S23"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = s23Key,
                category = "Smartphones",
                subcategory = "Smartphones (Android)",
                manufacturer = "Samsung",
                modelName = "Galaxy S23 (5G)",
                modelNumber = "SM-S911B/DS",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Display").put("label", "Panel").put("value", "6.1\" Dynamic AMOLED 2X (120Hz)"))
                    put(JSONObject().put("category", "Ladeanschluss").put("label", "USB").put("value", "USB-C 3.2 Gen 1 (25W Super Fast Charging)"))
                    put(JSONObject().put("category", "Akku").put("label", "Kapazität").put("value", "3900 mAh (Li-Ion)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Download-Modus: Gerät ausschalten, Lauter + Leiser gleichzeitig gedrückt halten und USB-C Kabel zum PC einstecken.")
                    put("Samsung Diagnostics: In der Telefon-App '*#0*#' eingeben für Hardware-Sensortests.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Feuchtigkeit im USB-Port").put("description", "Meldung verhindert kabelgebundenes Laden").put("cause", "Korrosion an den Pins der USB-C Buchse").put("solution", "Buchse mit Isopropanol reinigen oder Sub-Board tauschen."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = s23Key,
                componentName = "USB-C Ladebuchse & Sub-Board",
                componentCategory = "Ladeelektronik & Schnittstellen",
                partNumberOrSpec = "Samsung Sub-PBA Ladeplatine SM-S911B",
                difficulty = "Einfach",
                estimatedTimeMinutes = 25,
                toolsJson = JSONArray().apply {
                    put("Heißluft 80°C")
                    put("Phillips PH00")
                    put("Kunststoff-Hebelwerkzeug")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Glas-Rückseite gleichmäßig erwärmen, um Risse beim Aufhebeln zu verhindern.")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Backcover lösen").put("description", "Rückseite auf 80°C erwärmen und Kleberahmen mit dünner Plastikkarte vorsichtig durchtrennen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Lautsprecherabdeckung abschrauben").put("description", "Phillips-Schrauben des unteren Lautsprechermoduls entfernen und Modul herausnehmen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Sub-Board austauschen").put("description", "Flexkabel und Antennenkabel abklipsen, altes Sub-Board entnehmen und neues einsetzen.").put("caution", null))
                }.toString(),
                notes = "Schnell und kostengünstig tauschbar, löst 99% aller Ladeprobleme."
            )
        )

        // Google Pixel 8 Pro
        val pixel8Key = "GOOGLE_PIXEL_8_PRO"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = pixel8Key,
                category = "Smartphones",
                subcategory = "Smartphones (Android)",
                manufacturer = "Google",
                modelName = "Pixel 8 Pro",
                modelNumber = "GC3VE / G1MNW",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Display").put("label", "Panel").put("value", "6.7\" Super Actua LTPO OLED (1-120Hz)"))
                    put(JSONObject().put("category", "Sensor").put("label", "Temperatursensor").put("value", "Integrierter Melexis Infrarot-Thermometer"))
                    put(JSONObject().put("category", "Akku").put("label", "Kapazität").put("value", "5050 mAh Li-Ion mit Zuglaschen"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Google Repair Tool: Über pixelrepair.withgoogle.com im Chrome-Browser direkt Werkskalibrierung für Display und Fingerabdrucksensor durchführen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Fingerabdrucksensor nicht kalibriert").put("description", "Sensor reagiert nach Displaytausch nicht").put("cause", "Fehlende optische Kalibrierungsdaten").put("solution", "Google Pixel Fingerprint Calibration Tool im Browser ausführen."))
                }.toString()
            )
        )

        // -------------------------------------------------------------
        // LAPTOPS & NOTEBOOKS
        // -------------------------------------------------------------
        val macbookProKey = "APPLE_MACBOOK_PRO_14"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = macbookProKey,
                category = "Laptops & Notebooks",
                subcategory = "Apple Mac & MacBooks",
                manufacturer = "Apple",
                modelName = "MacBook Pro 14 (M-Series)",
                modelNumber = "A2442 / A2779 / A2992",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Display").put("label", "Panel").put("value", "14.2\" Liquid Retina XDR Mini-LED (120Hz)"))
                    put(JSONObject().put("category", "Kühlung").put("label", "Lüfter").put("value", "Duale asymmetrische Lüfter mit Heatpipe"))
                    put(JSONObject().put("category", "Schrauben").put("label", "Bodenplatte").put("value", "6x P5 Pentalobe (verschiedene Längen)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Apple Diagnostics: Beim Einschalten Power-Taste gedrückt halten bis Startoptionen erscheinen, dann 'Cmd + D' drücken.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "PPBUS_G3H Spannung fehlt").put("description", "MacBook schaltet nicht ein").put("cause", "Kurzschluss auf der primären 12V Versorgungsleitung durch Intersil ISL9240 PMIC").put("solution", "Kondensatoren rund um Ladecontroller auf Kurzschluss prüfen."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = macbookProKey,
                componentName = "Lüfter- & Heatsink-Reinigung",
                componentCategory = "Kühlung & Wartung",
                partNumberOrSpec = "Duale Apple Fan Assembly",
                difficulty = "Einfach",
                estimatedTimeMinutes = 20,
                toolsJson = JSONArray().apply {
                    put("P5 Pentalobe Schraubendreher")
                    put("Torx T5 Schraubendreher")
                    put("Pinsel & Druckluftdose")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("BATTERY DISCONNECT PFLICHT: Sofort nach Abnehmen des Bodens den kleinen Batterietrennschalter betätigen!")
                    put("Beim Ausblasen der Lüfter Flügel festhalten, um Induktionsspannung ins Board zu verhindern.")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Bodenplatte lösen").put("description", "Die 6 Pentalobe-Schrauben entfernen (Längen merken!), Deckel nach vorne ziehen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Akku trennen").put("description", "Schutzklappe über Akkustecker hochklappen und Trennfolie ziehen.").put("caution", "Pflichtschritt!"))
                    put(JSONObject().put("stepNumber", 3).put("title", "Lüfterflügel säubern").put("description", "Staubablagerungen an den Ansaugschlitzen und Lamellen mit Pinsel lösen und ausblasen.").put("caution", null))
                }.toString(),
                notes = "Regelmäßige Reinigung alle 12 Monate hält das System lautlos und verhindert Drosselung."
            )
        )

        // Lenovo ThinkPad T14
        val thinkpadKey = "LENOVO_THINKPAD_T14"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = thinkpadKey,
                category = "Laptops & Notebooks",
                subcategory = "Business & Windows Laptops",
                manufacturer = "Lenovo",
                modelName = "ThinkPad T14 Gen 3",
                modelNumber = "21AH / 21AJ",
                releaseYear = "2022",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Tastatur").put("label", "Typ").put("value", "Spritzwassergeschützte ThinkPad Präzisionstastatur mit TrackPoint"))
                    put(JSONObject().put("category", "Speicher").put("label", "Aufrüstbarkeit").put("value", "1x DDR4/DDR5 SO-DIMM Slot + 1x M.2 2280 NVMe SSD Slot"))
                    put(JSONObject().put("category", "Sicherheit").put("label", "Reset-Schalter").put("value", "Hardware Emergency Reset Hole auf Unterseite"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Internen Akku im BIOS deaktivieren: Vor jedem Öffnen unter 'Config -> Power -> Disable Built-in Battery' wählen. Reaktiviert sich beim Anstecken des Netzteils.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Black Screen / Kein Start").put("description", "ThinkPad reagiert nicht auf Einschaltknopf").put("cause", "Eingefrorener Embedded Controller (EC)").put("solution", "Büroklammer für 10 Sekunden in das kleine Notfall-Loch auf der Unterseite drücken."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = thinkpadKey,
                componentName = "Tastatur-Austausch & TrackPoint",
                componentCategory = "Eingabegeräte",
                partNumberOrSpec = "Lenovo FRU 5N20W68058 DE-Layout",
                difficulty = "Einfach",
                estimatedTimeMinutes = 15,
                toolsJson = JSONArray().apply {
                    put("Phillips PH0 Schraubendreher")
                    put("Flacher Hebelspatel")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Vor dem Ausbau im BIOS zwingend den internen Akku deaktivieren!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Tastaturrahmen nach hinten schieben").put("description", "Rahmen zwischen den Tasten vorsichtig mit Spatel um 2 mm nach oben schieben.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Befestigungsschrauben lösen").put("description", "Die 2 freigelegten Kreuzschlitzschrauben herausdrehen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Tastatur anheben & Flexkabel trennen").put("description", "Tastatur nach vorne klappen und beide Flachbandkabel entriegeln.").put("caution", null))
                }.toString(),
                notes = "Modulare Tastaturkonstruktion ermöglicht den Tausch in unter 15 Minuten."
            )
        )

        // Dell XPS 15
        val dellKey = "DELL_XPS_15"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = dellKey,
                category = "Laptops & Notebooks",
                subcategory = "Business & Windows Laptops",
                manufacturer = "Dell",
                modelName = "XPS 15 (9520/9530)",
                modelNumber = "XPS 9520",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Gehäuse").put("label", "Material").put("value", "CNC gefrästes Aluminium & Carbonfaser-Handauflage"))
                    put(JSONObject().put("category", "Akku").put("label", "Kapazität").put("value", "86 Wh 6-Zellen Li-Ion (Typ 4K1VM)"))
                    put(JSONObject().put("category", "Schrauben").put("label", "Boden").put("value", "8x Torx T5 & 2x Phillips PH00 unter Serviceklappe"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Dell ePSA Diagnose: Beim Einschalten wiederholt 'F12' drücken und 'Diagnostics' wählen. Testet Lüfter, Sensoren und RAM.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Trackpad lässt sich nicht mehr klicken").put("description", "Trackpad steht über Gehäuse hervor").put("cause", "Geblähter Lithium-Ionen Akku drückt von unten gegen das Trackpad").put("solution", "Brandgefahr! Akku unverzüglich ausbauen und fachgerecht entsorgen."))
                }.toString()
            )
        )

        // -------------------------------------------------------------
        // TABLETS
        // -------------------------------------------------------------
        val ipadKey = "APPLE_IPAD_PRO_11"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = ipadKey,
                category = "Tablets",
                subcategory = "Apple iPads",
                manufacturer = "Apple",
                modelName = "iPad Pro 11\" (M2)",
                modelNumber = "A2759 / A2761",
                releaseYear = "2022",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Display").put("label", "Panel").put("value", "11.0\" Liquid Retina IPS (120Hz ProMotion)"))
                    put(JSONObject().put("category", "Akku").put("label", "Kapazität").put("value", "7538 mAh (28.65 Wh)"))
                    put(JSONObject().put("category", "Anschluss").put("label", "Port").put("value", "Thunderbolt / USB 4"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("DFU-Wiederherstellung: Lauter kurz, Leiser kurz, Power halten bis Bildschirm schwarz wird, dann Power + Leiser 5s, dann nur Leiser halten.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Ghost Touching").put("description", "Display reagiert selbstständig auf Berührungen").put("cause", "Statisches Aufladen oder minderwertiges Ersatzdisplay").put("solution", "Massefedern am Gehäuserand prüfen und Displaykabel abschirmen."))
                }.toString()
            )
        )

        // -------------------------------------------------------------
        // HAUSHALTSGERÄTE (Inklusive Kaffeevollautomaten!)
        // -------------------------------------------------------------
        val delonghiKey = "DELONGHI_MAGNIFICA_ECAM_22_110"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = delonghiKey,
                category = "Haushaltsgeräte",
                subcategory = "Kaffeevollautomaten & Siebträger",
                manufacturer = "DeLonghi",
                modelName = "Magnifica S (ECAM 22.110.B)",
                modelNumber = "ECAM 22.110.B / SB",
                releaseYear = "2018",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Heizsystem").put("label", "Thermoblock").put("value", "1450 Watt Edelstahl-Durchlauferhitzer"))
                    put(JSONObject().put("category", "Pumpe").put("label", "Pumpendruck").put("value", "15 bar Ulka EP5 Vibrationspumpe"))
                    put(JSONObject().put("category", "Brühsystem").put("label", "Brühgruppe").put("value", "Entnehmbare DeLonghi Kompakt-Brüheinheit (rot)"))
                    put(JSONObject().put("category", "Dichtungen").put("label", "O-Ringe").put("value", "2x O-Ring 36.1x3.53 mm EPDM am Brühkolben"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Diagnosemodus: Tasten '1 Tasse Espresso' + 'Dampf' gedrückt halten und Hauptschalter hinten einschalten. LEDs blinken zur Bestätigung.")
                    put("Reset: Gerät ausschalten, '1 Tasse' + '2 Tassen' gedrückt halten und einschalten.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Allgemeiner Alarm (Warndreieck)").put("description", "Brühgruppe blockiert oder Endschalter nicht erreicht").put("cause", "Brühkolben schwergängig oder Antriebsspindel trocken").put("solution", "Brüheinheit entnehmen, Spindel mit Silikonfett schmieren, Mikroschalter prüfen."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = delonghiKey,
                componentName = "Brüheinheit & Kolben-Dichtungen",
                componentCategory = "Brühsystem & Mechanik",
                partNumberOrSpec = "DeLonghi 7313251451 / 2x O-Ring 36x3.5mm EPDM",
                difficulty = "Einfach",
                estimatedTimeMinutes = 20,
                toolsJson = JSONArray().apply {
                    put("Lebensmittelechtes Silikonfett (OKS 1110)")
                    put("Flachschraubendreher klein")
                    put("Warmes Wasser")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Keine Spülmaschine verwenden – das wäscht die Schmierstoffe der Führungsschienen aus!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Brüheinheit entnehmen").put("description", "Servicetür öffnen, rote Tasten zusammendrücken und herausziehen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "O-Ringe erneuern").put("description", "Alte EPDM-Ringe abhebeln und neue Ringe mit Silikonfett bestreichen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Spindel fetten").put("description", "Führungsschienen und Gewindespindel nachschmieren und einsetzen.").put("caution", null))
                }.toString(),
                notes = "Wartungsintervall: Alle 12 Monate oder 2000 Bezüge Dichtungen erneuern."
            )
        )

        // Bosch Serie 6 Waschmaschine
        val boschKey = "BOSCH_SERIE_6_WAT28400"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = boschKey,
                category = "Haushaltsgeräte",
                subcategory = "Großgeräte (Waschen & Spülen)",
                manufacturer = "Bosch",
                modelName = "Serie 6 VarioPerfect (WAT28400)",
                modelNumber = "WAT28400 / Serie 6",
                releaseYear = "2020",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Motor").put("label", "Motortyp").put("value", "EcoSilence Drive bürstenloser Inverter-Motor (BLDC)"))
                    put(JSONObject().put("category", "Heizung").put("label", "Heizelement").put("value", "2000 Watt Rohrheizkörper mit NTC-Temperatursensor"))
                    put(JSONObject().put("category", "Pumpe").put("label", "Laugenpumpe").put("value", "30 Watt Hanning / Askoll Magnettechnik-Pumpe"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Fehlerspeicher auslesen: Wahlschalter auf 6 Uhr. Taste 'Schleuderdrehzahl' gedrückt halten und Schalter auf 7 Uhr drehen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "E18 / F18").put("description", "Abpumpzeit überschritten / Pumpe blockiert").put("cause", "Flusensieb verstopft oder Fremdkörper im Flügelrad").put("solution", "Notentleerungsschlauch nutzen, Flusensieb herausschrauben und reinigen."))
                    put(JSONObject().put("code", "E23 / F23").put("description", "Aquastop / Wasser in Bodenwanne").put("cause", "Schwimmerschalter im Gehäuseboden aktiv durch Undichtigkeit").put("solution", "Wasser ablaufen lassen und Leckstelle orten."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = boschKey,
                componentName = "Laugenpumpe / Ablaufpumpe",
                componentCategory = "Ablauf & Hydraulik",
                partNumberOrSpec = "Bosch 00145787 / Askoll M231",
                difficulty = "Einfach",
                estimatedTimeMinutes = 35,
                toolsJson = JSONArray().apply {
                    put("Torx T20 Schraubendreher")
                    put("Kombizange für Schellen")
                    put("Flache Schale für Restwasser")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("LEBENSGEFAHR: Zwingend Netzstecker ziehen!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Notentleerung durchführen").put("description", "Kleinen Schlauch an der unteren Serviceklappe in Schale entleeren.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Flusensieb herausdrehen").put("description", "Fremdkörper entfernen und Flügelrad mit Finger anstoßen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Pumpe tauschen").put("description", "Sockelblende lösen, Schläuche mit Zange abziehen und neue Pumpe einsetzen.").put("caution", null))
                }.toString(),
                notes = "Kostengünstig und schnell zu reparieren."
            )
        )

        // Jura E8 Kaffeevollautomat
        val juraKey = "JURA_E8_COFFEEMAKER"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = juraKey,
                category = "Haushaltsgeräte",
                subcategory = "Kaffeevollautomaten & Siebträger",
                manufacturer = "Jura",
                modelName = "E8 Kaffeevollautomat",
                modelNumber = "EB / EC Generation",
                releaseYear = "2021",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Brühsystem").put("label", "P.E.P.").put("value", "Puls-Extraktionsprozess Brühgruppe (fest verbaut)"))
                    put(JSONObject().put("category", "Pumpe").put("label", "Druck").put("value", "15 bar Hochleistungspumpe"))
                    put(JSONObject().put("category", "Schrauben").put("label", "Gehäuseschrauben").put("value", "Jura Ovalsicherheitskopf-Schrauben (spezieller Ovalkopfschlüssel nötig)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Diagnosemenü: 'P' Taste gedrückt halten, Drehregler für Zählerstände (Kaffeebezüge, Entkalkungszyklen) nutzen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Error 8").put("description", "Brühgruppe klemmt in oberer/unterer Endposition").put("cause", "O-Ringe des Drainageventils aufgequollen oder Motorritzel blockiert").put("solution", "Drainageventil revidieren und Dichtsatz mit Silikonfett tauschen."))
                }.toString()
            )
        )

        // Miele Geschirrspüler G7000
        val mieleKey = "MIELE_G7000_DISHWASHER"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = mieleKey,
                category = "Haushaltsgeräte",
                subcategory = "Großgeräte (Waschen & Spülen)",
                manufacturer = "Miele",
                modelName = "G 7000 Geschirrspüler",
                modelNumber = "G 7100 / G 7300 SC",
                releaseYear = "2021",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Pumpe").put("label", "Umwälzpumpe").put("value", "Miele M-Power BLDC Inverter Umwälzpumpe"))
                    put(JSONObject().put("category", "Sicherheit").put("label", "Wasserschutz").put("value", "Waterproof-System (WPS) mit Doppelmagnetventil"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Servicemodus: Gerät ausschalten. Tasten 'Start' gedrückt halten und Hauptschalter einschalten. Innerhalb von 4 Sekunden 3x 'Start' drücken und beim 3. Mal halten.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Fehler F70").put("description", "Wasserschutzsystem / Bodenwanne voll").put("cause", "Schwimmerschalter aktiv, Laugenpumpe läuft dauerhaft zur Sicherheit").put("solution", "Bodenblech öffnen, Wasser abtupfen und Zulauf-/Ablaufdichtungen prüfen."))
                }.toString()
            )
        )

        // -------------------------------------------------------------
        // GAMING & KONSOLEN
        // -------------------------------------------------------------
        val ps5Key = "SONY_PLAYSTATION_5"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = ps5Key,
                category = "Gaming & Konsolen",
                subcategory = "Heimkonsolen (PS5/Xbox)",
                manufacturer = "Sony",
                modelName = "PlayStation 5 (CFI-1216A / CFI-1000)",
                modelNumber = "CFI-1016A / CFI-1216A",
                releaseYear = "2020",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Kühlung").put("label", "Kühlmedium").put("value", "Flüssigmetall (Gallium-Indium Legierung)"))
                    put(JSONObject().put("category", "Schnittstellen").put("label", "HDMI").put("value", "HDMI 2.1 (4K 120Hz / 8K 60Hz)"))
                    put(JSONObject().put("category", "Schrauben").put("label", "Gehäuse").put("value", "Torx T8 Security mit Mittelstift"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Sicherheitsmodus: Konsole ausschalten, Power gedrückt halten bis nach 7 Sekunden der ZWEITE Piepton ertönt. Controller per USB verbinden.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Überhitzung / Notabschaltung").put("description", "PS5 schaltet bei Grafiklast nach 15 Min ab").put("cause", "Flüssigmetall oxidiert oder zur Seite verlaufen ('Dry Spot')").put("solution", "APU polieren und Flüssigmetall neu verstreichen."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = ps5Key,
                componentName = "Flüssigmetall & APU-Kühler",
                componentCategory = "Kühlung & Thermomanagement",
                partNumberOrSpec = "Thermal Grizzly Conductonaut / Sony Liquid Metal",
                difficulty = "Experte",
                estimatedTimeMinutes = 60,
                toolsJson = JSONArray().apply {
                    put("Torx T8 Security")
                    put("Flüssigmetall (Gallium-Legierung)")
                    put("Isopropanol 99.9% & Q-Tips")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Flüssigmetall ist STROMLEITEND! Ein Tropfen auf SMDs erzeugt sofortigen Kurzschluss.")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Lüfter & Platten abnehmen").put("description", "Weiße Faceplates abziehen und Lüftergitter mit T8 Security lösen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Kühler vorsichtig trennen").put("description", "Spannklammer lösen und Kühler leicht drehen vor dem Anheben.").put("caution", "Nicht mit Gewalt reißen."))
                    put(JSONObject().put("stepNumber", 3).put("title", "Oxidationen polieren").put("description", "Trockene Stellen auf dem Die mit Q-Tip und Isopropanol polieren bis es spiegelt.").put("caution", null))
                }.toString(),
                notes = "Löst das verbreitete PS5-Abschaltproblem dauerhaft."
            )
        )

        // Nintendo Switch OLED
        val switchKey = "NINTENDO_SWITCH_OLED"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = switchKey,
                category = "Gaming & Konsolen",
                subcategory = "Handhelds & Portable (Switch/Steam Deck)",
                manufacturer = "Nintendo",
                modelName = "Switch (OLED / Standard)",
                modelNumber = "HEG-001 / HAC-001",
                releaseYear = "2021",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Display").put("label", "Panel").put("value", "7.0\" OLED Panel (720p)"))
                    put(JSONObject().put("category", "Controller").put("label", "Sticks").put("value", "Potentiometer-Joysticks (anfällig für Abrieb/Drift)"))
                    put(JSONObject().put("category", "Schrauben").put("label", "Gehäuse").put("value", "Tri-Wing Y00 (1.5mm) & Phillips #00"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Stick-Kalibrierung: Systemeinstellungen -> Controller und Sensoren -> Control Sticks kalibrieren.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Joy-Con Stick Drift").put("description", "Spielfigur bewegt sich selbstständig").put("cause", "Graphitbahn-Abrieb im Potentiometer").put("solution", "Stick gegen magnetischen Hall-Effekt Stick austauschen."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = switchKey,
                componentName = "Joy-Con Hall-Effekt Analog-Stick Upgrade",
                componentCategory = "Eingabe & Steuerung",
                partNumberOrSpec = "Hall-Sensor 3D Joystick (Magnetisch)",
                difficulty = "Einfach",
                estimatedTimeMinutes = 20,
                toolsJson = JSONArray().apply {
                    put("Tri-Wing Y00")
                    put("Phillips #00")
                    put("Pinzette")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("ZIF-Klemmen der Flachbandkabel nur sanft mit Spatel hochklappen!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Joy-Con öffnen").put("description", "4 Tri-Wing Schrauben lösen und wie ein Buch aufklappen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Akku entnehmen").put("description", "Akku heraushebeln und Schrauben des Mittelrahmens lösen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Neuen Hall-Stick montieren").put("description", "Alten Stick entnehmen, Hall-Stick einsetzen und kalibrieren.").put("caution", null))
                }.toString(),
                notes = "Magnetische Hall-Sticks können konstruktionsbedingt nie wieder driften."
            )
        )

        // Microsoft Xbox Series X
        val xboxKey = "MICROSOFT_XBOX_SERIES_X"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = xboxKey,
                category = "Gaming & Konsolen",
                subcategory = "Heimkonsolen (PS5/Xbox)",
                manufacturer = "Microsoft",
                modelName = "Xbox Series X",
                modelNumber = "Model 1882",
                releaseYear = "2020",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Kühlung").put("label", "Lüfter").put("value", "130 mm Flüster-Axiallüfter & geteilte Hauptplatine"))
                    put(JSONObject().put("category", "Kühler").put("label", "Vapor Chamber").put("value", "Kupfer-Dampfkammer mit Lamellenblock"))
                    put(JSONObject().put("category", "Schrauben").put("label", "Rückwand").put("value", "Torx T8 Security unter Aufklebern"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Problembehandlung / Reset: Auswerfen-Taste + Koppeln-Taste gedrückt halten und Xbox-Taste drücken bis zweiter Startton ertönt.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Fehler E100 / E101").put("description", "Betriebssystem-Update fehlgeschlagen").put("cause", "Beschädigte Systempartition auf der internen Western Digital NVMe SSD").put("solution", "Offline-Systemupdate (OSU1) via FAT32/NTFS USB-Stick einspielen."))
                }.toString()
            )
        )

        // -------------------------------------------------------------
        // AUDIO & HI-FI
        // -------------------------------------------------------------
        val sonyAudioKey = "SONY_WH1000XM5"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = sonyAudioKey,
                category = "Audio & Hi-Fi",
                subcategory = "Over-Ear Kopfhörer (ANC)",
                manufacturer = "Sony",
                modelName = "WH-1000XM5 ANC Kopfhörer",
                modelNumber = "YY2954",
                releaseYear = "2022",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Akku").put("label", "Kapazität").put("value", "1200 mAh (3.8V Li-Ion)"))
                    put(JSONObject().put("category", "Treiber").put("label", "Wandler").put("value", "30 mm Kohlefaser-Verbundmembran"))
                    put(JSONObject().put("category", "ANC").put("label", "Prozessoren").put("value", "Sony V1 + QN1 Dual-Chip mit 8 Mikrofonen"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Initialisieren / Werksreset: Kopfhörer ausschalten. Power-Taste und NC/AMB-Taste gleichzeitig für ca. 7 Sekunden gedrückt halten bis blaue LED 4x blinkt.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Pfeifen / Rückkopplung in einer Muschel").put("description", "Hoher Pfeifton bei aktiver Geräuschunterdrückung").put("cause", "Feuchtigkeit oder Schmutz im internen Feedback-Mikrofon").put("solution", "Ohrpolster abnehmen, Akustikvlies trocknen und Mikrofonöffnung mit Isopropanol säubern."))
                }.toString()
            )
        )

        // Apple AirPods Pro 2
        val airpodsKey = "APPLE_AIRPODS_PRO_2"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = airpodsKey,
                category = "Audio & Hi-Fi",
                subcategory = "In-Ear Kopfhörer & True Wireless",
                manufacturer = "Apple",
                modelName = "AirPods Pro 2 (USB-C / Lightning)",
                modelNumber = "A2968 / A3048",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Chip").put("label", "Audio").put("value", "Apple H2 Kopfhörer-Chip mit Bluetooth 5.3"))
                    put(JSONObject().put("category", "Ladecase").put("label", "Akku").put("value", "523 mAh Li-Ion mit Lautsprecher & U1 Chip"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Zurücksetzen: Beide AirPods ins Case stecken, Deckel öffnen. Taste auf der Rückseite 15 Sekunden gedrückt halten bis LED bernsteinfarben und dann weiß blinkt.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Ein Ohrhörer lädt nicht").put("description", "AirPod wird im Widget mit 0% angezeigt").put("cause", "Oxidierte Ladekontakte am Schachtboden des Cases").put("solution", "Wattestäbchen mit Isopropanol befeuchten und die vergoldeten Pins am Boden vorsichtig säubern."))
                }.toString()
            )
        )

        // -------------------------------------------------------------
        // FAHRRAD & E-BIKE
        // -------------------------------------------------------------
        val boschEbikeKey = "BOSCH_EBIKE_PERFORMANCE_CX"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = boschEbikeKey,
                category = "Fahrrad & E-Bike",
                subcategory = "E-Bikes (Motoren, Akkus & Sensorik)",
                manufacturer = "Bosch E-Bike",
                modelName = "Performance Line CX Gen 4 (Smart System)",
                modelNumber = "BDU3740",
                releaseYear = "2022",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Motor").put("label", "Drehmoment").put("value", "85 Nm max. Drehmoment / 250 Watt Nennleistung"))
                    put(JSONObject().put("category", "Akku").put("label", "Batterie").put("value", "PowerTube 750 Wh (36V Li-Ion)"))
                    put(JSONObject().put("category", "Sensorik").put("label", "Speichensensor").put("value", "Reed-Sensor an Kettenstrebe mit Speichenmagnet (Abstand: 5-17 mm)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Bosch Flow App: Updates für Motor, Display und Bedieneinheit drahtlos per Bluetooth über das Smartphone einspielen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Fehler 500").put("description", "Interner Elektronikfehler im Motor").put("cause", "Feuchtigkeit in den Steckkontakten oder Motorsteuerungs-Reset nötig").put("solution", "Akku entnehmen, Kontakte reinigen und Power-Taste am Akku für 20 Sekunden gedrückt halten."))
                    put(JSONObject().put("code", "Fehler 503").put("description", "Geschwindigkeitssensor-Signal fehlerhaft").put("cause", "Speichenmagnet verdreht oder verrutscht").put("solution", "Magnet so drehen, dass er genau an der Markierung des Sensors im Abstand von 5 bis 15 mm vorbeiläuft."))
                }.toString()
            )
        )

        // Shimano Deore XT
        val shimanoKey = "SHIMANO_DEORE_XT"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = shimanoKey,
                category = "Fahrrad & E-Bike",
                subcategory = "Klassische Fahrräder (Schaltung & Bremsen)",
                manufacturer = "Shimano",
                modelName = "Deore XT M8100 12-Fach",
                modelNumber = "RD-M8100-SGS",
                releaseYear = "2021",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Antrieb").put("label", "Gänge").put("value", "1x12-Fach Hyperglide+ Kassetten (10-51T)"))
                    put(JSONObject().put("category", "Kupplung").put("label", "Shadow RD+").put("value", "Einstellbarer Reibungsdämpfer gegen Kettenschlagen"))
                    put(JSONObject().put("category", "Bremse").put("label", "Bremsflüssigkeit").put("value", "Shimano Original Mineralöl (KEINE DOT-Bremsflüssigkeit verwenden!)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("B-Screw Justierung: Auf das größte 51-Zähne Ritzel schalten. Markierungslinie auf der Rückseite des Schaltwerkskäfigs exakt mit den Zahnspitzen des Ritzels abgleichen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Kette springt bei Gangwechsel").put("description", "Gänge rasseln oder schalten verzögert").put("cause", "Zugspannung gelängt oder Schaltauge verbogen").put("solution", "Zugspannschraube am Trigger gegen den Uhrzeigersinn drehen oder Schaltauge mit Richtwerkzeug ausrichten."))
                }.toString()
            )
        )

        // -------------------------------------------------------------
        // FAHRZEUGE & KFZ
        // -------------------------------------------------------------
        val vwKey = "VW_GOLF_VII_TDI"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = vwKey,
                category = "Fahrzeuge & KFZ",
                subcategory = "PKW Wartung & Elektronik",
                manufacturer = "Volkswagen",
                modelName = "Golf VII 2.0 TDI (EA288)",
                modelNumber = "Golf 7 TDI / 150 PS",
                releaseYear = "2017",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Diagnose").put("label", "OBD-2 Port").put("value", "16-Pin OBD-2 Buchse links unter dem Lenkrad / Sicherungskasten"))
                    put(JSONObject().put("category", "Batterie").put("label", "Start-Stopp").put("value", "70 Ah AGM Starterbatterie (erfordert BMS-Anlernen)"))
                    put(JSONObject().put("category", "Glühkerzen").put("label", "Glühanlage").put("value", "Keramik-Glühkerzen mit 4.4V Betriebsspannung (Anzugsdrehmoment 15-18 Nm)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("VCDS / OBDeleven Codierung: Nach Tausch der Starterbatterie im Steuergerät 19 (Diagnoseinterface) die neue Kapazität, Seriennummer und Batteriehersteller eintragen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "P0300 / P0301-P0304").put("description", "Verbrennungsaussetzer Zylinder erkannt").put("cause", "Defekte Zündspule / Glühkerze oder Injektor verkokt").put("solution", "Bauteil mit Nachbarzylinder quer tauschen und prüfen, ob der Fehler mitwandert."))
                    put(JSONObject().put("code", "P0401").put("description", "AGR-Durchsatz zu gering").put("cause", "Abgasrückführungsventil (AGR) mit Ruß zugesetzt").put("solution", "AGR-Ventil ausbauen und mit Drosselklappenreiniger säubern oder ersetzen."))
                }.toString()
            )
        )

        // BMW F30
        val bmwKey = "BMW_3ER_F30"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = bmwKey,
                category = "Fahrzeuge & KFZ",
                subcategory = "PKW Wartung & Elektronik",
                manufacturer = "BMW",
                modelName = "3er / 5er (F30/G30)",
                modelNumber = "F30 320d / 330i",
                releaseYear = "2018",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Batterie").put("label", "Typ").put("value", "80 Ah oder 90 Ah AGM Batterie im Kofferraum rechts"))
                    put(JSONObject().put("category", "BMS").put("label", "Sensor").put("value", "Intelligenter Batteriesensor (IBS) am Minuspol"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Batteriewechsel registrieren: Mit BimmerLink oder ISTA+ neuen Batteriewechsel im DME/DDE registrieren. Unterbleibt dies, überlädt die Lichtmaschine den neuen Akku!")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Drivetrain Error / Antrieb gestört").put("description", "Leistungsverlust bei Beschleunigung").put("cause", "Ladedruck-Regelventil (Wastegate/VTG) klemmt oder Zündaussetzer").put("solution", "Fehlerspeicher mit ENET-Kabel auslesen und Zündspulen auf Risse prüfen."))
                }.toString()
            )
        )
    }
}
