package com.example.model

data class OfficeLineItem(
    val description: String,
    val partNumber: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val isLabor: Boolean = false
) {
    val total: Double get() = quantity * unitPrice
}

object OfficeTemplates {
    fun getTemplateFor(docType: String, customer: String = "", device: String = ""): String {
        return when (docType) {
            "Angebot" -> """
                # Kostenvoranschlag / Reparaturangebot
                **Datum:** ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMANY).format(java.util.Date())}
                **Kunde:** ${customer.ifBlank { "Max Mustermann" }}
                **Gerät / Baugruppe:** ${device.ifBlank { "Gerät gem. Eingangsbefund" }}
                
                ### Befund & Schadensanalyse
                - Fehlerbild: Gerät startet nicht / Display defekt / Fehlercode
                - Vorprüfung: Sichtprüfung und elektrische Messung durchgeführt
                
                ### Reparaturbedingungen
                - Ersatzteile in Erstausrüster-Qualität (OEM / Original Refurbished)
                - ESD-gesicherter Arbeitsplatz nach DIN EN 61340-5-1
                - Gültigkeit des Angebots: 14 Kalendertage
            """.trimIndent()

            "Rechnung" -> """
                # Rechnung
                **Rechnungsdatum:** ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMANY).format(java.util.Date())}
                **Leistungszeitpunkt:** Entspricht Rechnungsdatum
                **Rechnungsempfänger:** ${customer.ifBlank { "Kunde" }}
                **Auftragsgegenstand:** ${device.ifBlank { "Reparatur / PC-Montage" }}
                
                ### Durchgeführte Arbeiten
                - Vollständige Funktionsdiagnose und Baugruppentest
                - Fachgerechter Austausch der defekten Komponenten
                - 100% Endprüfung und Sicherheitsprüfung
                
                ### Zahlungskonditionen
                - Zahlbar innerhalb von 14 Tagen ohne Abzug
                - Banküberweisung auf angegebenes Geschäftskonto
            """.trimIndent()

            "Auftrag" -> """
                # Werkstattauftrag (Reparatur / PC-Bau)
                **Auftragsannahme:** ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMANY).format(java.util.Date())}
                **Auftraggeber:** ${customer.ifBlank { "Kunde" }}
                **Zielsystem:** ${device.ifBlank { "Hardwarekomponente / PC-System" }}
                
                ### Kundenauftrag & Wünsche
                - Detaillierte Fehlerbehebung oder Neuaufbau nach Spezifikation
                - Datenrettung / Datenerhalt erwünscht: JA [x]  NEIN [ ]
                - Maximales Kostenlimit freigegeben: Siehe Kostenvoranschlag
                
                ### Status-Checkliste
                - [x] Annahme & Typenschild registriert
                - [ ] Fehlerdiagnose & Messwerte erfasst
                - [ ] Ersatzteile disponiert
                - [ ] Montage & Funktionskontrolle
                - [ ] Abholbenachrichtigung an Kunden
            """.trimIndent()

            "Reparaturbericht", "Prüfprotokoll" -> """
                # Technischer Reparatur- & Prüfbericht
                **Prüfdatum:** ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMANY).format(java.util.Date())}
                **Prüfer:** CoreSystems Techniker
                **Prüfling:** ${device.ifBlank { "Elektrisches Gerät" }}
                
                ### 1. Eingangsprüfung & Sichtkontrolle
                - Gehäusezustand: Keine äußerlichen Sicherheitsmängel
                - Schutzleiter / Isolierung: Vorprüfung unauffällig
                
                ### 2. Fehlerursache & Reparaturverlauf
                - Auslöser: Defektes Bauteil / Überlastung / Verschleiß
                - Ersatz: Austausch gegen neues Bauteil durchgeführt
                
                ### 3. Messtechnische Endabnahme
                - Isolationswiderstand: R_iso > 10 MΩ (Bestanden)
                - Schutzleiterwiderstand: R_pe < 0.3 Ω (Bestanden)
                - Betriebsstrom unter Nennlast: Im Toleranzbereich
                
                **Gesamtergebnis: FREIGEGEBEN ZUM BETRIEB**
            """.trimIndent()

            "PC-Bau Plan" -> """
                # PC-Konfigurations- & Bauplan
                **Erstellt am:** ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMANY).format(java.util.Date())}
                **System-Name:** ${device.ifBlank { "Custom Gaming & Creator Rig" }}
                
                ### Architektur & Kompatibilität
                - CPU & Sockel: Pins & Kühlerhalterung geprüft
                - RAM: XMP / EXPO Profil Kompatibilität verifiziert
                - GPU: Maße (Länge / Slots) & 12VHPWR Anschluss geprüft
                - Kühlung & Airflow: Positiver Überdruck (Überdruckbetrieb)
                
                ### Montage-Reihenfolge
                1. CPU, M.2 SSD und RAM außerhalb auf Mainboard vorinstallieren
                2. I/O-Shield & Mainboard-Abstandshalter im Gehäuse montieren
                3. Netzteil mit modularen Kabeln einsetzen & vorverlegen
                4. CPU-Kühler / AIO Radiator montieren (Schutzfolie abziehen!)
                5. Grafikkarte im obersten PCIe x16 Slot einrasten & abstützen
                6. BIOS Flash & 24h Stabilitäts-Stresstest
            """.trimIndent()

            "Freies Dokument", "Freie Gestaltung" -> """
                # Freies Dokument / Werkstatt-Dokumentation
                **Datum:** ${java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.GERMANY).format(java.util.Date())}
                **Verfasser / Kunde:** ${customer.ifBlank { "Werkstattleitung" }}
                **Betreff / Projekt:** ${device.ifBlank { "Freies Projekt & Dokumentation" }}
                
                ### Freie Dokumentation & Arbeitsbericht
                Hier besteht die vollständige Freiheit für individuelle Arbeitsberichte, Messprotokolle, Notizen oder Kundenabsprachen.
                
                ### Individuelle Gliederungspunkte
                - 1. Eingangsbefund & Ausgangslage
                - 2. Manuelle Analyse & Sonderprüfung
                - 3. Vereinbarungen, Beschaffungsnotizen & Kundenfreigabe
                - 4. Fazit & Technische Zusammenfassung
            """.trimIndent()

            else -> """
                # Dokumenten-Notiz
                **Erstellt:** ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.GERMANY).format(java.util.Date())}
                **Betreff:** ${device.ifBlank { "Allgemeine Werkstattnotiz" }}
                
                Text hier eingeben...
            """.trimIndent()
        }
    }
}
