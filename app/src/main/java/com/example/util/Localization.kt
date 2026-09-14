package com.example.util

object Localization {
    fun t(key: String, language: String = "DE"): String {
        val isEn = language.uppercase() == "EN"
        return when (key) {
            // Navigation
            "nav_start" -> if (isEn) "Home" else "Start"
            "nav_catalog" -> if (isEn) "Devices" else "Katalog"
            "nav_projects" -> if (isEn) "Projects" else "Projekte"
            "nav_history" -> if (isEn) "History" else "Historie"
            "nav_profile" -> if (isEn) "Settings" else "Einstellungen"

            // Start Screen
            "start_title" -> if (isEn) "CoreRepair Assistant" else "Reparatur-Agent"
            "start_subtitle" -> if (isEn) "AI Diagnostics & Professional Repair Hub" else "Intelligente Diagnose & Reparaturwerkbank"
            "start_search_placeholder" -> if (isEn) "Describe defect or device (e.g. 'Coffee maker leaks water', 'PC won't turn on')..." else "Defekt oder Gerät beschreiben (z.B. 'Kaffeemaschine tropft', 'PC bootet nicht')..."
            "start_action_diagnose" -> if (isEn) "Diagnose Now" else "Diagnose starten"
            "start_workbench_card_title" -> if (isEn) "Interactive Workbench & Meters" else "Interaktive Werkbank & Mess-Tools"
            "start_workbench_card_sub" -> if (isEn) "Multimeter Targets • SMD Decoder • Eco Passport" else "Multimeter-Sollwerte • SMD-Decoder • Öko-Pass"
            "start_glossary_card_title" -> if (isEn) "Beginner Guide & Tech Glossary" else "Anfänger-Leitfaden & Fachbegriffe"
            "start_glossary_card_sub" -> if (isEn) "Diode test, OL, Standoffs, ESD & 230V safety explained simply" else "Diodentest, OL, Standoffs, ESD & 230V-Sicherheit einfach erklärt"

            // Workbench
            "wb_title" -> if (isEn) "Interactive Workbench" else "Interaktive Werkbank"
            "wb_subtitle" -> if (isEn) "Verified Expected Values & Pro Utilities" else "Geprüfte Sollwerte & Fach-Utilities"
            "wb_tab_multimeter" -> if (isEn) "Multimeter Check" else "Multimeter-Prüfung"
            "wb_tab_smd" -> if (isEn) "SMD & IC Decoder" else "SMD & IC-Decoder"
            "wb_tab_eco" -> if (isEn) "Eco & Repair Passport" else "Öko & Werkstatt-Pass"
            "wb_tab_norms" -> if (isEn) "Materials & Standards" else "Material & Normen"

            // Safety Disclaimer
            "disclaimer_title" -> if (isEn) "Safety Warning & Liability Disclaimer" else "Sicherheitshinweis & Haftungsausschluss"
            "disclaimer_subtitle" -> if (isEn) "Important safety rules before performing any repair" else "Wichtige Sicherheitsregeln vor Beginn von Reparaturarbeiten"
            "disclaimer_mains_title" -> if (isEn) "230V Mains Voltage & Capacitors" else "230V Netzspannung & Kondensatoren"
            "disclaimer_mains_text" -> if (isEn) "Always unplug the power cord before opening! Primary capacitors store lethal DC voltages (>300V) even hours after disconnection. Discharge properly before touching." else "Vor dem Öffnen stets den Netzstecker ziehen! Primärkondensatoren in Netzteilen speichern lebensgefährliche Spannungen (>300V) auch Stunden nach dem Trennen. Vor Berührung fachgerecht entladen."
            "disclaimer_battery_title" -> if (isEn) "Lithium-Ion Battery Safety" else "Lithium-Ionen-Akkus & Brandgefahr"
            "disclaimer_battery_text" -> if (isEn) "Never bend, puncture, or short lithium batteries. Fire and toxic chemical fumes risk. Use only plastic pry tools." else "Akkus niemals knicken, durchstechen oder kurzschließen. Akute Brand- und Verpuffungsgefahr. Niemals spitze Metallwerkzeuge zum Aushebeln verwenden."
            "disclaimer_self_responsibility" -> if (isEn) "All instructions and measurement targets are provided for DIY & right-to-repair reference. You perform repairs entirely at your own risk. Consult a certified technician if unsure." else "Alle Diagnosen, Messwerte und Richtlinien dienen als Hilfestellung zur Selbstreparatur ('Right to Repair'). Die Durchführung erfolgt vollständig auf eigene Verantwortung. Bei Unsicherheit einen Fachbetrieb hinzuziehen."
            "disclaimer_checkbox" -> if (isEn) "I have read, understood, and accept the safety rules and terms of use." else "Ich habe die Sicherheitsregeln und den Haftungsausschluss gelesen und akzeptiere diese."
            "disclaimer_accept_btn" -> if (isEn) "Accept & Continue to Workbench" else "Zustimmen & Fortfahren"

            // General
            "close" -> if (isEn) "Close" else "Schließen"
            "back" -> if (isEn) "Back" else "Zurück"
            "search" -> if (isEn) "Search" else "Suchen"
            "all" -> if (isEn) "All" else "Alle"
            else -> key
        }
    }
}
