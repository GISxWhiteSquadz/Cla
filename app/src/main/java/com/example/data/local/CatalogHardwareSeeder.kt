package com.example.data.local

import org.json.JSONArray
import org.json.JSONObject

object CatalogHardwareSeeder {

    fun seedHardwareProfiles(
        profiles: MutableList<DeviceProfileEntity>,
        components: MutableList<DeviceComponentEntity>
    ) {
        val category = "PC-Bau & Hardware"

        // 1. AMD: Ryzen 7 7800X3D (AM5)
        val amdKey = "AMD_RYZEN_7_7800X3D"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = amdKey,
                category = category,
                subcategory = "CPU / Prozessor",
                manufacturer = "AMD",
                modelName = "Ryzen 7 7800X3D (AM5)",
                modelNumber = "100-100000910WOF",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Sockel & Architektur").put("label", "Sockel").put("value", "AMD AM5 (LGA1718)"))
                    put(JSONObject().put("category", "Kerne & Takt").put("label", "Kerne / Threads").put("value", "8 Kerne / 16 Threads bis 5.0 GHz Boost"))
                    put(JSONObject().put("category", "Cache").put("label", "3D V-Cache").put("value", "96 MB L3 3D V-Cache (104 MB Gesamt-Cache)"))
                    put(JSONObject().put("category", "Verlustleistung").put("label", "TDP & TJMax").put("value", "120 Watt TDP / Max. Temperatur 89 °C"))
                    put(JSONObject().put("category", "Speicher").put("label", "RAM Support").put("value", "DDR5-5200 (JEDEC) / Sweet-Spot: DDR5-6000 CL30 (EXPO)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("EXPO aktivieren: Nach dem Zusammenbau ins BIOS (Entf/F2) und 'AMD EXPO I' laden für DDR5-6000 CL30 Stabilität.")
                    put("First Boot & Memory Training: Beim allerersten Start kann der Bildschirm 2-3 Minuten schwarz bleiben. Die DRAM Debug-LED leuchtet Orange/Gelb – Netzteil NICHT ausschalten!")
                    put("Curve Optimizer: Für niedrigere Temperaturen im BIOS Curve Optimizer auf 'All Cores Negative 15 bis 20' einstellen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Debug-LED ROT (CPU)").put("description", "CPU wird nicht erkannt oder erhält keine Spannungsversorgung").put("cause", "8-Pin EPS CPU-Stromkabel am Mainboard vergessen oder Sockelpins beschädigt").put("solution", "Prüfen, ob das 8-Pin CPU-Kabel (nicht PCIe!) oben links am Board fest eingerastet ist."))
                    put(JSONObject().put("code", "Debug-LED GELB / ORANGE (DRAM)").put("description", "DDR5 Speichertraining oder Kontaktproblem").put("cause", "Memory Training beim ersten Start oder RAM sitzt nicht in Slot A2/B2").put("solution", "Mindestens 3 Minuten Geduld haben. RAM in Slot 2 und 4 von links einrasten."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = amdKey,
                componentName = "AM5 CPU Sockelmontage (LGA1718)",
                componentCategory = "Prozessor & Sockel",
                partNumberOrSpec = "LGA1718 Land Grid Array Sockel",
                difficulty = "Mittel",
                estimatedTimeMinutes = 10,
                toolsJson = JSONArray().apply {
                    put("Flache, saubere Arbeitsunterlage (Mainboard-Karton)")
                    put("Erdungsband / ESD-Schutz")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("ACHTUNG: Die LGA-Pins im AM5-Sockel sind extrem empfindlich. Niemals berühren! Ein verbogener Pin zerstört das Board.")
                    put("Schwarze Plastik-Schutzkappe noch auf dem Sockel lassen – sie springt beim Schließen des Hebels von selbst ab.")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Sockelhebel entriegeln").put("description", "Metallhebel leicht nach außen drücken und komplett nach oben klappen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "CPU an den Kanten fassen").put("description", "Ryzen 7 7800X3D nur an den Außenkanten greifen. Dreiecksmarkierung an der CPU und am Sockel abgleichen.").put("caution", "Nicht auf Kontakte fassen."))
                    put(JSONObject().put("stepNumber", 3).put("title", "CPU drucklos einlegen").put("description", "Prozessor exakt waagerecht in den Sockel gleiten lassen – NIEMALS drücken!").put("caution", "Muss plan aufliegen."))
                    put(JSONObject().put("stepNumber", 4).put("title", "Hebel arretieren").put("description", "Metallrahmen absenken und Hebel mit sanftem Druck unter die Rastnase führen. Die Plastikkappe springt ab.").put("caution", null))
                }.toString(),
                notes = "Plastik-Schutzkappe unbedingt im Mainboard-Karton für eventuelle Garantiefälle aufbewahren."
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = amdKey,
                componentName = "Wärmeleitpaste & Kühler-Montage",
                componentCategory = "Kühlung & Paste",
                partNumberOrSpec = "Nichtleitende Wärmeleitpaste (z.B. Arctic MX-6 / Noctua NT-H2)",
                difficulty = "Einfach",
                estimatedTimeMinutes = 15,
                toolsJson = JSONArray().apply {
                    put("Kreuzschlitz PH2 Schraubendreher")
                    put("Isopropanol 99.9% & fusselfreies Tuch")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Zwingend prüfen, ob die durchsichtige Schutzfolie von der Kupferbodenplatte des Kühlers abgezogen wurde!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Heatspreader säubern").put("description", "Die Oberseite des Ryzen 7800X3D mit Isopropanol reinigen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Paste auftragen").put("description", "Erbsengroßen Punkt (ca. 4-5 mm) mittig auf den Heatspreader setzen oder X-Muster wählen.").put("caution", "Nicht zu viel Paste nehmen."))
                    put(JSONObject().put("stepNumber", 3).put("title", "Kühler aufsetzen & über Kreuz anziehen").put("description", "Kühler plan aufsetzen und Schrauben abwechselnd über Kreuz je eine halbe Umdrehung festziehen.").put("caution", "Gleichmäßiger Anpressdruck."))
                    put(JSONObject().put("stepNumber", 4).put("title", "Lüfter an CPU_FAN Header anschließen").put("description", "4-Pin PWM Lüfterkabel am oberen Mainboard-Header 'CPU_FAN' einstecken.").put("caution", null))
                }.toString(),
                notes = "AM5 Heatspreader hat Aussparungen an den Seiten – überquellende Paste ist elektrisch unkritisch, sieht aber unschön aus."
            )
        )

        // 2. INTEL: Core i7-14700K (LGA1700)
        val intelKey = "INTEL_CORE_I7_14700K"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = intelKey,
                category = category,
                subcategory = "CPU / Prozessor",
                manufacturer = "Intel",
                modelName = "Core i7-14700K (LGA1700)",
                modelNumber = "BX8071514700K",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Sockel & Architektur").put("label", "Sockel").put("value", "Intel LGA1700 (Raptor Lake Refresh)"))
                    put(JSONObject().put("category", "Kerne & Takt").put("label", "Kerne").put("value", "20 Kerne (8 P-Cores + 12 E-Cores), 28 Threads bis 5.6 GHz"))
                    put(JSONObject().put("category", "Verlustleistung").put("label", "Base / Turbo Power").put("value", "125W Base / 253W Max Turbo Power (PL2)"))
                    put(JSONObject().put("category", "Speicher").put("label", "RAM Support").put("value", "DDR5 bis 5600 MHz (JEDEC) / XMP 3.0 bis 7200+ MHz"))
                    put(JSONObject().put("category", "Zubehör-Tipp").put("label", "Contact Frame").put("value", "Thermalright LGA1700 Contact Frame zur Vorbeugung von IHS-Verbiegen"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Intel Baseline Profile: Im Mainboard-UEFI 'Intel Default Settings / Baseline Profile' auswählen, um Spannungsaussetzer und Instabilitäten dauerhaft zu verhindern.")
                    put("PL1/PL2 Power Limits: PL1 und PL2 im BIOS fest auf 253 Watt begrenzen, ICCMax auf 307 Ampere setzen.")
                    put("Intel XMP 3.0: DDR5-XMP Profil mit einem Klick im BIOS aktivieren.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "BSOD 'UNEXPECTED_KERNEL_MODE_TRAP'").put("description", "Instabilität bei Lastspitzen im Browser oder Unreal Engine").put("cause", "Mainboard liefert zu hohe unlimitierte Spannungen (Multi-Core Enhancement aktiv)").put("solution", "BIOS auf neuste Version mit Intel 0x129/0x12B Microcode updaten und Intel Default Settings laden."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = intelKey,
                componentName = "LGA1700 Montage & CPU Contact Frame",
                componentCategory = "Prozessor & Sockel",
                partNumberOrSpec = "Thermalright LGA1700 Contact Frame / ILM Ersatz",
                difficulty = "Mittel",
                estimatedTimeMinutes = 20,
                toolsJson = JSONArray().apply {
                    put("Torx T20 Schlüssel (liegt Contact Frame bei)")
                    put("Mainboard-Karton als Unterlage")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Schrauben des Contact Frames nur gleichmäßig über Kreuz mit zwei Fingern handfest anziehen (ca. 0.4 Nm). Zu viel Druck führt zu RAM-Kanal-Ausfall!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Standard-ILM demontieren").put("description", "Die 4 Torx-Schrauben des originalen Mainboard-Haltebügels vorsichtig lösen und Original-Bügel abnehmen.").put("caution", "Backplate auf der Rückseite mit Hand festhalten."))
                    put(JSONObject().put("stepNumber", 2).put("title", "CPU einlegen").put("description", "Core i7-14700K mit Einkerbungen am LGA1700 Sockel ausrichten und spannungsfrei einsetzen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Contact Frame aufsetzen").put("description", "Thermalright Frame plan auflegen und die 4 Schrauben über Kreuz abwechselnd vorsichtig anziehen.").put("caution", "Keine Gewalt."))
                }.toString(),
                notes = "Der Contact Frame senkt die CPU-Temperaturen um 5 bis 9 °C, da der Heatspreader nicht mehr konkav durchbiegt."
            )
        )

        // 3. ASUS: ROG Strix B650E-F Gaming WiFi
        val asusKey = "ASUS_ROG_STRIX_B650E_F"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = asusKey,
                category = category,
                subcategory = "Mainboard",
                manufacturer = "ASUS",
                modelName = "ROG Strix B650E-F Gaming WiFi Mainboard",
                modelNumber = "90MB1BQ0-M0EAY0",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Formfaktor").put("label", "Format").put("value", "Standard ATX (305 × 244 mm)"))
                    put(JSONObject().put("category", "Chipsatz").put("label", "Chipsatz").put("value", "AMD B650E (Extreme PCIe 5.0 Support)"))
                    put(JSONObject().put("category", "PCIe Slots").put("label", "Grafikkartenslot").put("value", "1x PCIe 5.0 x16 SafeSlot mit Q-Release Taste"))
                    put(JSONObject().put("category", "M.2 NVMe").put("label", "M.2 Steckplätze").put("value", "3x M.2 (1x PCIe 5.0 x4 + 2x PCIe 4.0 x4) mit M.2 Q-Latch schraubenlos"))
                    put(JSONObject().put("category", "Diagnose").put("label", "Q-LED").put("value", "4-Farb Status-LEDs (Rot, Gelb, Weiß, Grün) & BIOS FlashBack"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("BIOS FlashBack ohne CPU: BIOS-Datei von ASUS Website laden, per 'BIOSRenamer.exe' in SB650EF.CAP umbenennen, auf FAT32-Stick kopieren. Stick in FlashBack-Port stecken, FlashBack-Taste 3 Sekunden drücken bis LED blinkt.")
                    put("Q-Release Taste: Praktischer Druckknopf am rechten Rand des RAM-Bereichs zum bequemen Entriegeln riesiger Grafikkarten ohne Schraubendreher-Gefahr.")
                    put("Armoury Crate im BIOS deaktivieren: Verhindert automatische Software-Installation unter Windows.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Q-LED ROT").put("description", "CPU-Fehler").put("cause", "Kein Strom am 8-Pin EPS CPU Header oder CPU sitzt nicht korrekt").put("solution", "Stromkabel oben links prüfen."))
                    put(JSONObject().put("code", "Q-LED GELB / ORANGE").put("description", "DRAM-Fehler").put("cause", "RAM in falschen Slots (muss in Slot 2 und 4 von links) oder Speichertraining aktiv").put("solution", "DDR5 Memory Training abwarten oder RAM-Riegel neu fest eindrücken."))
                    put(JSONObject().put("code", "Q-LED WEISS").put("description", "VGA / Grafikfehler").put("cause", "Monitor nicht eingeschaltet, HDMI an Mainboard statt GPU oder PCIe-Stromkabel nicht arretiert").put("solution", "Monitorkabel direkt an die Grafikkarte stecken."))
                    put(JSONObject().put("code", "Q-LED GRÜN").put("description", "BOOT / Kein Betriebssystem").put("cause", "Normalzustand vor OS-Installation").put("solution", "Bootfähigen Windows USB-Stick einstecken."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = asusKey,
                componentName = "Abstandshalter (Standoffs) & Gehäusemontage",
                componentCategory = "Gehäuse & Mainboard",
                partNumberOrSpec = "Messing 6-32 UNC Standoffs (ATX 9-Punkt Belegung)",
                difficulty = "Einfach",
                estimatedTimeMinutes = 15,
                toolsJson = JSONArray().apply {
                    put("Phillips PH2 Schraubendreher")
                    put("Standoff-Stecknuss (liegt PC-Gehäuse bei)")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("LEBENSGEFÄHRLICHER KURZSCHLUSS: Nur dort Abstandshalter ins Gehäuse schrauben, wo das Mainboard tatsächlich Montagelöcher besitzt! Ein überzähliger Abstandshalter berührt Leiterbahnen auf der Board-Rückseite und brennt das Mainboard beim ersten Start ab!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Standoff-Positionen abgleichen").put("description", "Die 9 Befestigungslöcher des ATX-Mainboards mit den Gewindebohrungen des Gehäuses vergleichen.").put("caution", "Überflüssige Standoffs zwingend herausschrauben!"))
                    put(JSONObject().put("stepNumber", 2).put("title", "I/O-Shield prüfen").put("description", "Das ROG Strix besitzt eine fest integrierte I/O-Blende – kein loses Blech notwendig.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Mainboard einsetzen").put("description", "Board schräg zur I/O-Blende einführen, auf den Zentrierstift setzen und mit 9 Schrauben gefühlvoll handfest anziehen.").put("caution", null))
                }.toString(),
                notes = "Niemals Akkuschrauber für Mainboardschrauben verwenden – Platinenrisse drohen."
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = asusKey,
                componentName = "Front-Panel Header JFP1 & USB 3.2 Verdrahtung",
                componentCategory = "Verkabelung & Signale",
                partNumberOrSpec = "JFP1 9-Pin Header (Power SW, Reset SW, HDD LED)",
                difficulty = "Einfach",
                estimatedTimeMinutes = 10,
                toolsJson = JSONArray().apply {
                    put("Taschenlampe / Smartphone-Licht")
                    put("Spitzzange oder Pinzette bei engen Gehäusen")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("USB 3.2 Gen 1 (19-Pin) Stecker ist extrem fehleranfällig: Beim Einstecken exakt senkrecht führen, da die feinen Pins im Pfostenstecker extrem leicht verbiegen!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "JFP1 Belegung identifizieren").put("description", "Unten rechts am Board: Pin 6+8 (obere Reihe, 3. und 4. Pin von links) ist 'POWER SW'. Polarität (+/-) ist beim Taster egal!").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "LED-Polarität beachten").put("description", "Bei 'POWER LED' und 'HDD LED' ist Plus (+) immer links, Minus (-) rechts.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Front-Audio (AAFP) & USB-C verbinden").put("description", "HD-Audio Stecker ganz unten links einstecken (fehlender Pin dient als Verpolschutz). USB-C Frontkabel bündig einrasten.").put("caution", null))
                }.toString(),
                notes = "Sollte der PC nach dem Bau nicht angehen, 'POWER SW' Pin 6 und 8 kurz mit Schraubendreher brücken, um defekten Gehäuseschalter auszuschließen."
            )
        )

        // 4. MSI: GeForce RTX 4080 Super Gaming X Trio
        val msiKey = "MSI_RTX_4080_SUPER"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = msiKey,
                category = category,
                subcategory = "Grafikkarte (GPU)",
                manufacturer = "MSI",
                modelName = "GeForce RTX 4080 Super Gaming X Trio",
                modelNumber = "RTX 4080 SUPER 16G",
                releaseYear = "2024",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Grafikchip").put("label", "GPU").put("value", "AD103-400 (10240 CUDA Kerne, 2610 MHz Boost)"))
                    put(JSONObject().put("category", "VRAM").put("label", "Speicher").put("value", "16 GB GDDR6X (256-Bit Bus / 736 GB/s Bandbreite)"))
                    put(JSONObject().put("category", "Stromversorgung").put("label", "Stromanschluss").put("value", "1x 16-Pin 12V-2x6 / 12VHPWR (TGP 320 Watt)"))
                    put(JSONObject().put("category", "Abmessungen").put("label", "Länge & Slots").put("value", "337 × 140 × 67 mm (3.5 Slots / Gewicht ca. 1.87 kg)"))
                    put(JSONObject().put("category", "Kühlung").put("label", "Kühler").put("value", "Tri-Frozr 3 mit Torx Fan 5.0 & Kupfer-Bodenplatte"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("NVIDIA GeForce Treiber: Immer 'Game Ready Treiber' direkt von nvidia.com oder via GeForce Experience / NVIDIA App laden.")
                    put("Dual BIOS Schalter: Physischer Schalter an der Oberkante der Grafikkarte ('Silent' für flüsterleisen Betrieb oder 'Gaming' für maximale Lüfterdrehzahl).")
                    put("Resizable BAR im UEFI aktivieren: Erlaubt der CPU vollen Zugriff auf den gesamten 16 GB VRAM für bis zu 12% Mehrleistung in Cyberpunk 2077 etc.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Black Screen & Lüfter auf 100% Notlauf").put("description", "Grafikkarte verliert Signal bei Last, Lüfter drehen auf Maximaldrehzahl").put("cause", "Sense-Pins des 12VHPWR-Kabels haben Wackelkontakt oder Stecker nicht tief genug eingesteckt").put("solution", "12VHPWR Stromkabel mit Nachdruck fest eindrücken, bis die Rasthaken hörbar und spaltfrei schließen."))
                    put(JSONObject().put("code", "Grafikfehler / Artefakte / Checkerboard").put("description", "Grüne Punkte oder Dreiecke auf dem Bildschirm").put("cause", "PCIe-Slot nicht ganz eingerastet oder defekter VRAM").put("solution", "GPU ausbauen, PCIe-Kontakte mit Isopropanol reinigen und fest in den obersten Slot drücken."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = msiKey,
                componentName = "12VHPWR 16-Pin Stromkabel (Brandgefahr vermeiden)",
                componentCategory = "Stromversorgung & Sicherheit",
                partNumberOrSpec = "12V-2x6 / 12VHPWR 600W Kabel (16-Pin)",
                difficulty = "Mittel",
                estimatedTimeMinutes = 10,
                toolsJson = JSONArray().apply {
                    put("Gute Beleuchtung / Taschenlampe")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("BRANDGEFAHR (CRITICAL): Der 12VHPWR-Stecker MUSS vollständig und bündig ohne jeden Spalt in die Grafikkarte geschoben werden! Die Verriegelungsnase muss hörbar 'KLICK' machen. Ein unvollständig sitzender Stecker erzeugt Übergangswiderstände und schmilzt bei 320W Last!")
                    put("Biegeradius: Das Kabel erst mindestens 35 mm hinter dem Stecker biegen. Niemals direkt am Stecker knicken!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Stecker bündig ansetzen").put("description", "16-Pin Stecker gerade vor die Buchse der RTX 4080 Super halten.").put("caution", "Nicht verkanten."))
                    put(JSONObject().put("stepNumber", 2).put("title", "Mit festem Daumendruck einschieben").put("description", "Gleichmäßig kräftig drücken, bis die seitliche Rastklammer hörbar einrastet.").put("caution", "Kein Lichtspalt darf zwischen Stecker und Buchse sichtbar sein."))
                    put(JSONObject().put("stepNumber", 3).put("title", "Sichtprüfung mit Taschenlampe").put("description", "Aus allen 4 Winkeln prüfen, ob der Stecker parallel und lückenlos abschließt.").put("caution", null))
                }.toString(),
                notes = "Nutze bevorzugt das native 12VHPWR-Kabel des ATX 3.0 Netzteils anstelle wackeliger 3x 8-Pin Adapterpeitschen."
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = msiKey,
                componentName = "Anti-Sag Stütze (Grafikkartenhalter)",
                componentCategory = "Mechanik & Entlastung",
                partNumberOrSpec = "MSI Gaming Graphics Card Bracket (Liegt GPU bei)",
                difficulty = "Einfach",
                estimatedTimeMinutes = 10,
                toolsJson = JSONArray().apply {
                    put("Phillips PH2 Schraubendreher")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Mit 1.87 kg Eigengewicht biegt die GPU ohne Halterung nach unten durch. Dies führt langfristig zu Mikrorissen im PCIe-Slot oder auf dem GPU-Platinenlayer!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Slotblenden-Schrauben lösen").put("description", "Die 2 Slotblendenschrauben unterhalb der GPU lösen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Stützbügel anschrauben").put("description", "Den beiliegenden massiven MSI Metallbügel anschrauben.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Gummiauflage justieren").put("description", "Die gummierte Auflage so nach oben schieben, dass sie die rechte Ecke der Grafikkarte um ca. 2 mm anhebt und waagerecht hält.").put("caution", "Lüfterblätter dürfen nicht an der Stütze schleifen!"))
                }.toString(),
                notes = "Alternativ kann eine höhenverstellbare magnetische Säulenstütze auf dem Gehäuseboden verwendet werden."
            )
        )

        // 5. CORSAIR: Vengeance DDR5-6000 CL30 RAM Kit
        val corsairRamKey = "CORSAIR_VENGEANCE_DDR5"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = corsairRamKey,
                category = category,
                subcategory = "Arbeitsspeicher (RAM)",
                manufacturer = "Corsair",
                modelName = "Vengeance DDR5-6000 CL30 (2x16GB Kit)",
                modelNumber = "CMK32GX5M2B6000Z30",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Kapazität").put("label", "Speichervolumen").put("value", "32 GB (2x 16 GB Dual-Channel Kit)"))
                    put(JSONObject().put("category", "Taktfrequenz").put("label", "Geschwindigkeit").put("value", "DDR5-6000 MHz (PC5-48000)"))
                    put(JSONObject().put("category", "Latenzen").put("label", "Timings").put("value", "CL30-36-36-76 bei 1.40 Volt (Niedrige Latenz)"))
                    put(JSONObject().put("category", "Profile").put("label", "Overclocking-Profile").put("value", "AMD EXPO & Intel XMP 3.0 zertifiziert"))
                    put(JSONObject().put("category", "Bauhöhe").put("label", "Low-Profile").put("value", "35 mm Bauhöhe (Perfekte Kompatibilität mit großen Luftkühlern)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("DDR5 Dual-Channel Slots: Moderne Mainboards verlangen die Bestückung in Slot 2 und 4 von links (A2 und B2). Werden Slot 1 und 3 genutzt, bricht die Signalintegrität zusammen und das XMP/EXPO Profil wird instabil!")
                    put("MemTest86 Diagnosetest: Bei Bluescreens bootfähigen MemTest86 USB-Stick erstellen und 4 Durchläufe starten. 0 Errors erforderlich.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "PC bootet nicht / Lüfter drehen / Kein Bild").put("description", "Endlosschleife beim Speicherinitialisieren").put("cause", "RAM sitzt nicht bis zum Klick im Slot oder Memory Training noch im Gange").put("solution", "Riegel mit beiden Daumen kräftig eindrücken, bis der Klickverschluss zuschnappt."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = corsairRamKey,
                componentName = "DDR5 Dual-Channel Bestückung (Slots A2 & B2)",
                componentCategory = "Arbeitsspeicher & RAM",
                partNumberOrSpec = "288-Pin DDR5 DIMM",
                difficulty = "Einfach",
                estimatedTimeMinutes = 5,
                toolsJson = JSONArray().apply {
                    put("Keine Werkzeuge erforderlich (reine Handmontage)")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Kerbe in der Kontaktleiste ist asymmetrisch. Niemals versuchen, den RAM mit Gewalt falsch herum in den Slot zu pressen!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Slots A2 und B2 öffnen").put("description", "Die Halteklammern an Slot 2 und Slot 4 (vom CPU-Sockel aus nach rechts gezählt) nach außen klappen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Kerbe ausrichten").put("description", "Riegel über den Slot halten und Kerbe mit dem Steg im Sockel abgleichen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Mit beiden Daumen eindrücken").put("description", "Gleichmäßig an beiden Enden kräftig drücken, bis die Klammer von alleine hörbar zuschnappt.").put("caution", "Keine Schräglage."))
                }.toString(),
                notes = "Immer zuerst Slot 2 und 4 bestücken – niemals Slot 1 und 3 bei 2 Riegeln!"
            )
        )

        // 6. CORSAIR: RM850x Shift ATX 3.0 Netzteil
        val corsairPsuKey = "CORSAIR_RM850X_SHIFT"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = corsairPsuKey,
                category = category,
                subcategory = "Netzteil (PSU)",
                manufacturer = "Corsair",
                modelName = "RM850x Shift ATX 3.0 Netzteil",
                modelNumber = "CP-9020252-EU",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Leistung").put("label", "Gesamtleistung").put("value", "850 Watt Dauerleistung (100% japanische 105°C Kondensatoren)"))
                    put(JSONObject().put("category", "Effizienz").put("label", "Zertifikat").put("value", "80 PLUS Gold & Cybenetics Gold"))
                    put(JSONObject().put("category", "Standard").put("label", "Norm").put("value", "ATX 3.0 & PCIe 5.0 konform mit nativem 12VHPWR Kabel"))
                    put(JSONObject().put("category", "Besonderheit").put("label", "Shift-Kabelanschlüsse").put("value", "Seitliche modulare Anschlüsse für bequemstes Kabelmanagement"))
                    put(JSONObject().put("category", "Lüfter").put("label", "Lüftermodus").put("value", "Zero-RPM Lüftermodus bei Teillast bis 425 Watt lautlos"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Kabel-Kompatibilität (CRITICAL): Nur die mitgelieferten Corsair Type-5 Micro-Fit Kabel verwenden! Modulare Kabel anderer Hersteller (z. B. be quiet!, Seasonic) haben andere Pinbelegungen und zerstören angeschlossene Hardware sofort!")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Klickendes Relais-Geräusch beim Einschalten").put("description", "Netzteil klickt und schaltet sofort wieder ab").put("cause", "Kurzschlussschutz (SCP) oder Überlastschutz (OCP) löst aus").put("solution", "Verkabelung prüfen: Ist versehentlich ein PCIe 8-Pin Kabel in den CPU 8-Pin EPS Sockel gesteckt worden?"))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = corsairPsuKey,
                componentName = "Modulare Kabelbelegung (CPU EPS vs. PCIe)",
                componentCategory = "Stromversorgung & Schutz",
                partNumberOrSpec = "Corsair Type-5 Micro-Fit Modular Cables",
                difficulty = "Mittel",
                estimatedTimeMinutes = 20,
                toolsJson = JSONArray().apply {
                    put("Phillips PH2 Schraubendreher")
                    put("Klettkabelbinder")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("VERWECHSLUNGSGEFAHR: 8-Pin CPU EPS (oben links am Board) und 8-Pin PCIe (Grafikkarte) haben gegensätzliche Polaritäten! Die Stecker sind beschriftet mit 'CPU' und 'PCIe'. Niemals verwechseln!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Benötigte Kabel vorab anstecken").put("description", "24-Pin ATX, 2x 8-Pin CPU EPS und 1x 12VHPWR Kabel ins Netzteil einstecken.").put("caution", "Vor dem Einbau ins Gehäuse anstecken."))
                    put(JSONObject().put("stepNumber", 2).put("title", "Netzteil einsetzen").put("description", "Lüfter nach UNTEN ausrichten (sofern das Gehäuse Boden-Lüftungsschlitze mit Staubfilter besitzt).").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "4 Gehäuseschrauben fixieren").put("description", "Netzteil von der Rückseite mit den 4 Grobgewindeschrauben festziehen.").put("caution", null))
                }.toString(),
                notes = "Modulare Anschlüsse an der Gehäuseseite erleichtern nachträgliche Erweiterungen enorm."
            )
        )

        // 7. SAMSUNG: 990 PRO NVMe M.2 2280 SSD (2TB)
        val samsungKey = "SAMSUNG_990_PRO_2TB"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = samsungKey,
                category = category,
                subcategory = "SSD & Speicher",
                manufacturer = "Samsung",
                modelName = "990 PRO NVMe M.2 2280 SSD (2TB)",
                modelNumber = "MZ-V9P2T0BW",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Schnittstelle").put("label", "Interface").put("value", "PCIe Gen 4.0 x4, NVMe 2.0 (M.2 2280 M-Key)"))
                    put(JSONObject().put("category", "Leistung").put("label", "Lesen / Schreiben").put("value", "Bis zu 7450 MB/s Lesen / 6900 MB/s Schreiben"))
                    put(JSONObject().put("category", "Speicher").put("label", "Flash-Typ").put("value", "Samsung V-NAND TLC mit 2 GB LPDDR4 DRAM-Cache"))
                    put(JSONObject().put("category", "Lebensdauer").put("label", "TBW").put("value", "1200 Terabytes Written (5 Jahre Herstellergarantie)"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Samsung Magician Software: Nach Windows-Installation installieren, um Firmware auf den neusten Stand zu bringen und SMART-Werte zu überwachen.")
                    put("TRIM Befehl: Unter Windows mit 'fsutil behavior query DisableDeleteNotify' prüfen (Ergebnis 0 = TRIM aktiv für dauerhaft maximale Schreibraten).")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "SSD drosselt auf unter 500 MB/s").put("description", "Extremer Performance-Einbruch bei längerer Last").put("cause", "Thermal Throttling (Überhitzung über 80 °C) weil Schutzfolie auf dem Wärmeleitpad vergessen wurde").put("solution", "Heatsink abschrauben, blaue Folie abziehen und Heatsink neu montieren."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = samsungKey,
                componentName = "M.2 Slot Montage & Heatsink-Schutzfolie",
                componentCategory = "Massenspeicher & NVMe",
                partNumberOrSpec = "M.2 2280 NVMe SSD & Mainboard-Heatsink",
                difficulty = "Einfach",
                estimatedTimeMinutes = 10,
                toolsJson = JSONArray().apply {
                    put("Phillips PH1 Schraubendreher")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("HÄUFIGSTER ANFÄNGERFEHLER: Die durchsichtige oder blaue Schutzfolie auf dem Wärmeleitpad der Mainboard-Kühlkörper MUSS zwingend vor der Montage abgezogen werden! Bleibt sie drauf, kocht die SSD im eigenen Saft.")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Heatsink des obersten Slots abschrauben").put("description", "Immer den obersten M.2 Slot direkt unter der CPU wählen (direkte PCIe-Lanes zur CPU).").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "M.2 SSD im 30° Winkel einstecken").put("description", "Samsung 990 PRO schräg in den Slot schieben und sanft nach unten drücken.").put("caution", "M-Key Kerbe beachten."))
                    put(JSONObject().put("stepNumber", 3).put("title", "Mit Q-Latch arretieren").put("description", "Kunststoff-Riegel um 90° drehen (keine Schraube nötig).").put("caution", null))
                    put(JSONObject().put("stepNumber", 4).put("title", "FOLIE VOM PADS ABZIEHEN").put("description", "Blaue Schutzfolie vom Heatsink abziehen und Kühlkörper handfest verschrauben.").put("caution", "Folie nicht vergessen!"))
                }.toString(),
                notes = "Der primäre M.2 Slot ist direkt mit der CPU verdrahtet und liefert die geringste Latenz."
            )
        )

        // 8. BE QUIET!: Dark Rock Pro 5 CPU-Kühler
        val beQuietKey = "BE_QUIET_DARK_ROCK_PRO_5"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = beQuietKey,
                category = category,
                subcategory = "CPU-Kühlung",
                manufacturer = "be quiet!",
                modelName = "Dark Rock Pro 5 CPU-Kühler",
                modelNumber = "BK036",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Kühlleistung").put("label", "TDP").put("value", "270 Watt TDP Kühlleistung (Dual-Tower)"))
                    put(JSONObject().put("category", "Heatpipes").put("label", "Kupferrohre").put("value", "7 Hochleistungs-Kupfer-Heatpipes (6 mm)"))
                    put(JSONObject().put("category", "Lüfter").put("label", "Lüfterbestückung").put("value", "1x Silent Wings 135mm (Mitte) + 1x Silent Wings 4 120mm (Front)"))
                    put(JSONObject().put("category", "Schalter").put("label", "Speed Switch").put("value", "Integrierter Speed-Switch (Quiet-Modus bis 1500 RPM / Performance bis 2000 RPM)"))
                    put(JSONObject().put("category", "Lautstärke").put("label", "Geräuschpegel").put("value", "Max. 23.3 dB(A) bei 100% PWM Drehzahl"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Lüfterkurve im UEFI: CPU-Lüfterkurve auf 'PWM' und 'Silent' stellen: Bis 60°C 40% Drehzahl, ab 75°C 70%, ab 85°C 100%. Verhindert störendes Lüfteraufheulen bei kurzen Lastspitzen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "CPU erreicht sofort 100 °C beim Start").put("description", "Sofortige Notdrosselung nach Zusammenbau").put("cause", "Transparente Schutzfolie auf dem Kupferboden des Kühlers wurde nicht abgezogen").put("solution", "Kühler abnehmen, Plastikfolie abziehen, neue Wärmeleitpaste auftragen und montieren."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = beQuietKey,
                componentName = "Kühlkörper Backplate & Montagebrücke",
                componentCategory = "Kühlung & Montage",
                partNumberOrSpec = "Dark Rock Pro 5 Montage-Kit AM5 / LGA1700",
                difficulty = "Mittel",
                estimatedTimeMinutes = 20,
                toolsJson = JSONArray().apply {
                    put("Langer Phillips PH2 Schraubendreher (liegt Kühler bei)")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Die Schutzfolie auf der spiegelnden Kupferbodenplatte MUSS abgezogen werden!")
                    put("Mittleren 135mm Lüfter erst nach dem Verschrauben des Kühlers von oben in den Kühlschacht einschieben.")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Montagebrücken anschrauben").put("description", "Die zwei Metallbrücken links und rechts des Sockels mit Rändelschrauben fixieren.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "SCHUTZFOLIE VOM BODEN ABZIEHEN").put("description", "Die Schutzfolie an der Unterseite des Kühlers abziehen.").put("caution", "Absolut Pflicht!"))
                    put(JSONObject().put("stepNumber", 3).put("title", "Kühler über Kreuz verschrauben").put("description", "Mit dem langen Schraubendreher durch die Aussparungen im Kühler die beiden Hauptschrauben abwechselnd anziehen.").put("caution", "Gleichmäßiger Zug."))
                    put(JSONObject().put("stepNumber", 4).put("title", "Mittenlüfter einsetzen").put("description", "Den 135mm Lüfter von oben einschieben bis er einrastet. PWM-Kabel an CPU_FAN anschließen.").put("caution", null))
                }.toString(),
                notes = "Der Dark Rock Pro 5 kühlt selbst High-End CPUs flüsterleise ohne Ausfallrisiken einer AiO-Wasserkühlung."
            )
        )

        // 9. FRACTAL DESIGN: North ATX PC-Gehäuse
        val fractalKey = "FRACTAL_DESIGN_NORTH_ATX"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = fractalKey,
                category = category,
                subcategory = "PC-Gehäuse & Airflow",
                manufacturer = "Fractal Design",
                modelName = "North Charcoal Black ATX Gehäuse",
                modelNumber = "FD-C-NOR1C-01",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Gehäuse-Format").put("label", "Mainboard-Formfaktor").put("value", "ATX / Micro-ATX / Mini-ITX"))
                    put(JSONObject().put("category", "Material & Design").put("label", "Frontblende").put("value", "Echtholz-Lamellen (FSC-zertifizierte Walnuss/Eiche) mit Mesh"))
                    put(JSONObject().put("category", "Kompatibilität").put("label", "Max. Grafikkartenlänge").put("value", "Bis zu 355 mm (ausreichend für RTX 4080 Super)"))
                    put(JSONObject().put("category", "CPU-Kühlerhöhe").put("label", "Max. Kühlerhöhe").put("value", "Bis zu 170 mm (Dark Rock Pro 5 passt perfekt)"))
                    put(JSONObject().put("category", "Kühlung").put("label", "Vorinstallierte Lüfter").put("value", "2x 140 mm Aspect 14 PWM Lüfter in der Front"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Front-Panel Header JFP1: Den 9-Pin Frontpanel-Stecker (Power SW, Reset SW, Power LED) exakt nach Mainboard-Handbuch aufstecken. Bei ASUS/MSI: Pin 6 & 8 für Power Switch.")
                    put("Airflow-Prinzip: Vorne kühle Frischluft einsaugen (Intake), hinten und oben warme Abluft ausblasen (Exhaust) für leisen und staubfreien Betrieb.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "PC reagiert nicht auf den Power-Taster").put("description", "Kein Lüfter dreht, kein Lebenszeichen nach Gehäusebau").put("cause", "Power-SW Stecker sitzt um 1 Pin versetzt oder Gehäuse-Abstandshalter kurzschließen das Board").put("solution", "Frontpanel-Kabel am Mainboard prüfen oder mit Schraubendreher die beiden Power-Pins kurz überbrücken."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = fractalKey,
                componentName = "Mainboard-Abstandshalter & Frontpanel-Verkabelung",
                componentCategory = "Gehäusebau & Verkabelung",
                partNumberOrSpec = "Fractal North ATX Standoffs & I/O Shield",
                difficulty = "Einfach",
                estimatedTimeMinutes = 15,
                toolsJson = JSONArray().apply {
                    put("Phillips PH2 Schraubendreher")
                    put("Sechskant-Aufstecknuss für Standoffs")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("ABSTANDSHALTER-PFLICHT (CRITICAL): Nur dort Abstandshalter ins Gehäuse schrauben, wo das Mainboard tatsächlich Schraubenlöcher besitzt! Ein überzähliger Standoff unter dem Board verursacht Kurzschlüsse und zerstört das Mainboard beim ersten Einschalten!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Standoffs abgleichen").put("description", "9 ATX-Abstandshalter im Gehäuse genau passend zu den Löchern des ATX-Mainboards eindrehen.").put("caution", "Keine überzähligen Standoffs!"))
                    put(JSONObject().put("stepNumber", 2).put("title", "I/O Blende & Mainboard einsetzen").put("description", "Mainboard vorsichtig über die hinteren Anschlüsse schieben und auf den Zentrierpin absenken.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Schrauben handfest fixieren").put("description", "Mit den beiliegenden 6-32 Feingewindeschrauben das Board fixieren.").put("caution", "Nicht überdrehen."))
                    put(JSONObject().put("stepNumber", 4).put("title", "Power-SW & USB-C Frontpanel anstecken").put("description", "USB 3.2 Gen2x2 Typ-C Kabel und JFP1 Pfostenstecker unten rechts am Board fest einstecken.").put("caution", null))
                }.toString(),
                notes = "Das Fractal North bietet exzellentes Kabelmanagement hinter dem Mainboard-Tray mit vorverlegten Klettbändern."
            )
        )
    }
}
