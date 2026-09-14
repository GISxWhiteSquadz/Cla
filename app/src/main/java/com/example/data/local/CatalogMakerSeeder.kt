package com.example.data.local

import org.json.JSONArray
import org.json.JSONObject

object CatalogMakerSeeder {

    fun seedMakerProfiles(
        profiles: MutableList<DeviceProfileEntity>,
        components: MutableList<DeviceComponentEntity>
    ) {
        val category = "Robotik & Maker"

        // 1. ESPRESSIF: ESP32 DevKit V1
        val esp32Key = "ESPRESSIF_ESP32_DEVKIT"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = esp32Key,
                category = category,
                subcategory = "Mikrocontroller-Boards (ESP32/Arduino)",
                manufacturer = "Espressif",
                modelName = "ESP32 DevKit V1 (30-Pin NodeMCU)",
                modelNumber = "ESP-WROOM-32",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Mikrocontroller").put("label", "Prozessor").put("value", "Xtensa Dual-Core 32-Bit LX6 bis 240 MHz (520 KB SRAM)"))
                    put(JSONObject().put("category", "Logikpegel").put("label", "GPIO Spannung").put("value", "3.3V Logik (ACHTUNG: GPIOs sind NICHT 5V tolerant!)"))
                    put(JSONObject().put("category", "Konnektivität").put("label", "Funk-Module").put("value", "Wi-Fi 802.11 b/g/n (2.4 GHz) & Bluetooth 4.2 BLE"))
                    put(JSONObject().put("category", "Hardware-Busse").put("label", "Schnittstellen").put("value", "I2C (SDA=GPIO21, SCL=GPIO22), SPI, 3x UART, 16x PWM Kanäle"))
                    put(JSONObject().put("category", "Stromaufnahme").put("label", "Strombedarf").put("value", "80 mA Normalbetrieb / Bis zu 260 mA bei Wi-Fi Übertragung / 10 µA Deep Sleep"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Arduino IDE Board-URL: https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json eintragen.")
                    put("Bootloader Flash-Trick: Bei Fehlermeldung 'Failed to connect to ESP32: Timed out waiting for packet header' während des Uploads die physische 'BOOT'-Taste (GPIO 0) gedrückt halten.")
                    put("Baudrate Serieller Monitor: Auf 115200 Baud stellen für lesbare Boot- und Panic-Meldungen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Brownout detector was triggered").put("description", "Endloser Reboot beim Starten von Wi-Fi oder Motoren").put("cause", "Spannungseinbruch unter 2.7V durch fehlenden Stützkondensator oder schwache USB-Buchse").put("solution", "470µF-1000µF Elektrolytkondensator parallel zwischen 5V und GND schalten."))
                    put(JSONObject().put("code", "Guru Meditation Error: Core 1 panic").put("description", "ESP32 Absturz / FreeRTOS Exception").put("cause", "Nullpointer-Zugriff oder Stack-Overflow im Task").put("solution", "Stack-Größe des Tasks von 2048 auf 4096 Bytes erhöhen."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = esp32Key,
                componentName = "Pinout-Planung & Common GND",
                componentCategory = "Hardware & Verdrahtung",
                partNumberOrSpec = "30-Pin NodeMCU Pinout",
                difficulty = "Einfach",
                estimatedTimeMinutes = 10,
                toolsJson = JSONArray().apply {
                    put("Breadboard / Steckbrett")
                    put("Dupont-Jumperkabel")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("TABU-PINS: GPIO 6 bis 11 NIEMALS belegen – sie sind intern direkt mit dem SPI-Flash verlötet! Eine Beschaltung verhindert das Booten.")
                    put("Pins GPIO 34, 35, 36 und 39 sind reine Eingänge (Input-only) ohne interne Pull-Up Widerstände.")
                    put("COMMON GND PFLICHT: Alle Masseleitungen von ESP32, Motoren und Netzteilen MÜSSEN verbunden sein!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "ESP32 auf Breadboard platzieren").put("description", "Modul so einstecken, dass auf einer Seite eine Reihe Stecklöcher für Kabel frei bleibt.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Gemeinsame Masse legen").put("description", "GND des ESP32 mit der blauen Minusleiste des Breadboards verbinden.").put("caution", "Common GND ist zwingend."))
                    put(JSONObject().put("stepNumber", 3).put("title", "Stützkondensator einsetzen").put("description", "470µF Elko an 5V und GND setzen (Minuspol ist weißer Streifen).").put("caution", "Polung beachten."))
                }.toString(),
                notes = "Saubere Masseverbindungen verhindern 90% aller sporadischen Maker-Fehler."
            )
        )

        // 2. ARDUINO: Arduino Uno R4 WiFi
        val arduinoKey = "ARDUINO_UNO_R4"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = arduinoKey,
                category = category,
                subcategory = "Mikrocontroller-Boards (ESP32/Arduino)",
                manufacturer = "Arduino",
                modelName = "Arduino Uno R4 WiFi",
                modelNumber = "ABX00087",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Mikrocontroller").put("label", "Hauptprozessor").put("value", "Renesas RA4M1 (32-Bit ARM Cortex-M4 bis 48 MHz)"))
                    put(JSONObject().put("category", "Konnektivität").put("label", "Wi-Fi & Bluetooth").put("value", "ESP32-S3 Coprozessor für WLAN & BLE"))
                    put(JSONObject().put("category", "Logikpegel").put("label", "Betriebsspannung").put("value", "5V Logikpegel (voll 5V shields-kompatibel!)"))
                    put(JSONObject().put("category", "Eingangsspannung").put("label", "VIN Buchse").put("value", "6V bis 24V DC Weitbereichseingang"))
                    put(JSONObject().put("category", "Matrix").put("label", "LED-Matrix").put("value", "Integrierte 12x8 rote LED-Matrix (96 Pixel) für Symbole & Animationen"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Arduino IDE 2.x Support: In Boardverwalter nach 'Arduino Renesas RA4M1 Boards' suchen und installieren.")
                    put("LED Matrix Library: Mit '#include <Arduino_LED_Matrix.h>' lassen sich Grafiken direkt auf das Board zaubern.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Board wird nicht als COM-Port erkannt").put("description", "DFU Bootloader Modus aktiv").put("cause", "USB-C Kabel ist nur reines Ladekabel ohne Datenadern").put("solution", "Vollwertiges USB-C Datenkabel anschließen oder Reset-Knopf doppelt drücken für Bootloader-Mode."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = arduinoKey,
                componentName = "L298N Motortreiber & 5V Logiksteuerung",
                componentCategory = "Aktorik & Motoren",
                partNumberOrSpec = "L298N Dual H-Bridge Motor Driver Module",
                difficulty = "Einfach",
                estimatedTimeMinutes = 20,
                toolsJson = JSONArray().apply {
                    put("Schlitzschraubendreher 2.5 mm")
                    put("Dupont-Kabel Male-to-Female")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Motoren NIEMALS direkt über die 5V Pins des Arduino betreiben – der Spannungsregler brennt bei Blockierstrom sofort durch!")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Motorstrom verbinden").put("description", "Externe 7-12V Batterie an die 12V-Klemme des L298N und Minus an die GND-Klemme anschließen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Common GND zum Arduino").put("description", "Von der GND-Klemme des L298N ein Kabel zum GND-Pin des Arduino Uno R4 legen.").put("caution", "Pflicht!"))
                    put(JSONObject().put("stepNumber", 3).put("title", "Steuerpins anschließen").put("description", "IN1 bis IN4 an Arduino Digitalpins D8, D9, D10, D11 anschließen.").put("caution", null))
                }.toString(),
                notes = "Der Uno R4 arbeitet nativ mit 5V Signalen, sodass keine Pegelwandler zu Standard-Modulen nötig sind."
            )
        )

        // 3. RASPBERRY PI: Raspberry Pi 5
        val rpiKey = "RASPBERRY_PI_5"
        profiles.add(
            DeviceProfileEntity(
                fingerprintKey = rpiKey,
                category = category,
                subcategory = "Single-Board-Computer (Raspberry Pi)",
                manufacturer = "Raspberry Pi",
                modelName = "Raspberry Pi 5 Model B",
                modelNumber = "Raspberry Pi 5 4GB/8GB",
                releaseYear = "2023",
                specificationsJson = JSONArray().apply {
                    put(JSONObject().put("category", "Prozessor").put("label", "SoC").put("value", "Broadcom BCM2712 Quad-Core Arm Cortex-A76 bei 2.4 GHz"))
                    put(JSONObject().put("category", "Schnittstellen").put("label", "PCIe").put("value", "PCIe 2.0 x1 FPC-Steckplatz für M.2 NVMe HATs"))
                    put(JSONObject().put("category", "Stromversorgung").put("label", "Netzteil").put("value", "USB-C PD 5V / 5A (27 Watt) Netzteil erforderlich"))
                    put(JSONObject().put("category", "Kühlung").put("label", "Kühler-Header").put("value", "Dedizierter 4-Pin JST PWM-Lüfteranschluss mit Drehzahlregelung"))
                }.toString(),
                softwareInfoJson = JSONArray().apply {
                    put("Raspberry Pi Imager: Betriebssystem (Raspberry Pi OS 64-Bit Bookworm) direkt per Imager auf SD-Karte oder NVMe-SSD schreiben.")
                    put("NVMe Boot im EEPROM aktivieren: In '/boot/firmware/config.txt' den Eintrag 'dtparam=pciex1' und 'dtparam=pciex1_gen=3' eintragen.")
                }.toString(),
                knownErrorCodesJson = JSONArray().apply {
                    put(JSONObject().put("code", "Gelber Blitz / Warnsymbol oben rechts").put("description", "Under-Voltage Warning / Spannungseinbruch").put("cause", "Netzteil liefert weniger als 5V unter Last oder zu dünnes USB-Kabel").put("solution", "Offizielles Raspberry Pi 27W USB-C PD Netzteil verwenden."))
                }.toString()
            )
        )
        components.add(
            DeviceComponentEntity(
                deviceFingerprintKey = rpiKey,
                componentName = "Active Cooler Montage & PCIe NVMe HAT",
                componentCategory = "Kühlung & Speicher",
                partNumberOrSpec = "Offizieller Raspberry Pi 5 Active Cooler & M.2 HAT+",
                difficulty = "Einfach",
                estimatedTimeMinutes = 15,
                toolsJson = JSONArray().apply {
                    put("Pinzette für Wärmeleitpads")
                    put("Kreuzschlitzschraubendreher PH0")
                }.toString(),
                warningsJson = JSONArray().apply {
                    put("Der Pi 5 taktet ohne aktive Kühlung bei CPU-Last innerhalb von 45 Sekunden auf 85°C hoch und drosselt stark.")
                }.toString(),
                stepsJson = JSONArray().apply {
                    put(JSONObject().put("stepNumber", 1).put("title", "Schutzfolien abziehen").put("description", "Folie von den drei Wärmeleitpads an der Unterseite des Active Coolers entfernen.").put("caution", null))
                    put(JSONObject().put("stepNumber", 2).put("title", "Kühler einrasten").put("description", "Kühler aufsetzen und die beiden Kunststoff-Spreizstifte durch die Platine drücken bis sie klicken.").put("caution", null))
                    put(JSONObject().put("stepNumber", 3).put("title", "Lüfterkabel einstecken").put("description", "4-Pin JST Stecker in den Lüfteranschluss neben den USB-Ports stecken.").put("caution", null))
                }.toString(),
                notes = "Der Active Cooler regelt die Drehzahl automatisch anhand der SoC-Temperatur."
            )
        )
    }
}
