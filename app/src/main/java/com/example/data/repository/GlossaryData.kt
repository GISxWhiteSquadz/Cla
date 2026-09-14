package com.example.data.repository

data class GlossaryItem(
    val id: String,
    val term: String,
    val termEn: String,
    val category: String,
    val shortDefinition: String,
    val shortDefinitionEn: String,
    val detailedExplanation: String,
    val detailedExplanationEn: String,
    val practicalTip: String,
    val practicalTipEn: String,
    val dangerLevel: String = "INFO" // "INFO", "WARNING", "DANGER"
)

object GlossaryData {
    val items = listOf(
        GlossaryItem(
            id = "diode_test",
            term = "Diodentest (Multimeter)",
            termEn = "Diode Test Mode",
            category = "Messgeräte & Elektronik",
            shortDefinition = "Misst die Vorwärts-Schwellenspannung eines Halbleiters in Volt (V).",
            shortDefinitionEn = "Measures the forward voltage drop of a semiconductor in Volts (V).",
            detailedExplanation = "Das Multimeter schickt einen winzigen Prüfstrom (ca. 1 mA) durch das Bauteil und misst den Spannungsabfall. Silizium-PN-Übergänge (z.B. Schutzdioden, Transistoren, ESD-Dioden in ICs) zeigen typischerweise 0,40 V bis 0,70 V. Zeigt das Gerät 0,00 V an, liegt ein interner Kurzschluss vor. Zeigt es OL (Open Loop) in beide Richtungen, ist die Leitung unterbrochen.",
            detailedExplanationEn = "The multimeter sends a tiny test current (~1 mA) through the component and measures voltage drop. Silicon PN junctions typically show 0.40 V to 0.70 V. A reading of 0.00 V indicates a short circuit, while OL in both directions indicates a severed trace or blown component.",
            practicalTip = "Wichtig: Immer nur im stromlosen Zustand messen! Rote Prüfspitze an Masse (GND) und schwarze Prüfspitze an die Datenleitung (Reverse-Diode-Mode), um Schutzdioden auf Chips zu prüfen.",
            practicalTipEn = "Always measure with device powered off! Put red probe on GND and black probe on signal pin to check protection diodes.",
            dangerLevel = "INFO"
        ),
        GlossaryItem(
            id = "ol_open_loop",
            term = "OL (Open Loop / Überlauf)",
            termEn = "OL (Open Loop / Overload)",
            category = "Messgeräte & Elektronik",
            shortDefinition = "Bedeutet 'Unendlicher Widerstand' oder Messbereichsüberschreitung.",
            shortDefinitionEn = "Means infinite resistance, open circuit, or measurement out of range.",
            detailedExplanation = "Auf dem Multimeter-Display steht 'OL' oder '1.' nicht für einen Fehler des Messgeräts, sondern für 'Open Loop'. Es fließt kein Strom zwischen den Prüfspitzen. Im Widerstandsbereich (> MΩ) bedeutet dies eine saubere Trennung; im Diodentest in Sperrrichtung ist dies das normale Verhalten.",
            detailedExplanationEn = "On a multimeter, 'OL' or '1.' stands for Open Loop. No current flows between the test probes. In resistance mode, it means total isolation; in reverse diode mode, it is the expected healthy reading.",
            practicalTip = "Wenn du ein Kabel oder eine Sicherung prüfst und 'OL' siehst, ist der Draht bzw. die Sicherung durchgebrannt!",
            practicalTipEn = "If testing a fuse or wire and you see 'OL', the fuse or wire is blown/cut!",
            dangerLevel = "INFO"
        ),
        GlossaryItem(
            id = "short_circuit",
            term = "Kurzschluss (Short Circuit)",
            termEn = "Short Circuit",
            category = "Messgeräte & Elektronik",
            shortDefinition = "Ungewollte niederohmige Verbindung zwischen zwei Potenzialen (z.B. Spannungsschiene zu Masse).",
            shortDefinitionEn = "Unintended low-resistance connection between two potentials (e.g. power rail to GND).",
            detailedExplanation = "Ein Kurzschluss führt dazu, dass bei anliegender Spannung unkontrolliert hoher Strom fließt. Schutzschaltungen schalten das Netzteil sofort ab oder Bauteile (Keramikkondensatoren MLCC, ICs) brennen durch. Mit dem Durchgangsprüfer (Piepser) oder Widerstandstest (< 1 Ohm) lässt sich ein Kurzschluss leicht aufspüren.",
            detailedExplanationEn = "A short circuit causes excessive current flow. Protection circuits trigger shutdown or components (MLCC caps, MOSFETs) burn out. Detected via continuity beeper or resistance mode (< 1 Ohm).",
            practicalTip = "Verdächtige MLCC-Kondensatoren wärmen sich bei leichter Stromeinspeisung oft als Erste auf (Isopropanol-Alkohol-Verdunstungstest hilft beim Finden).",
            practicalTipEn = "Faulty MLCC capacitors heat up first when injecting low voltage (use 99.9% isopropanol evaporation to spot the culprit).",
            dangerLevel = "WARNING"
        ),
        GlossaryItem(
            id = "standoffs_pc",
            term = "Standoffs (Mainboard-Abstandshalter)",
            termEn = "Motherboard Standoffs",
            category = "PC-Bau & Hardware",
            shortDefinition = "Messing- oder Metallbolzen, die das Mainboard in sicherem Abstand zum Gehäuseblech halten.",
            shortDefinitionEn = "Brass/metal threaded spacers keeping the motherboard elevated away from metal chassis.",
            detailedExplanation = "Auf der Rückseite jedes Mainboards ragen hunderte gelötete Pin-Enden und Leiterbahnen heraus. Ohne Standoffs berühren diese das geerdete Gehäuseblech. Dies führt zu sofortigen Masseschlüssen und kann das Board oder Netzteil dauerhaft zerstören.",
            detailedExplanationEn = "Hundreds of solder pins poke through the back of a motherboard. Without standoffs, these touch bare metal causing catastrophic short circuits.",
            practicalTip = "Schraube nur dort Standoffs ins Gehäuse ein, wo das Mainboard auch tatsächlich ein Schraubloch mit Schutzrand hat. Überzählige Standoffs unter dem Board wirken wie eine Kurzschlussbrücke!",
            practicalTipEn = "Install standoffs ONLY where matching mounting holes exist on the motherboard. Extra standoffs will bridge and short rear traces!",
            dangerLevel = "WARNING"
        ),
        GlossaryItem(
            id = "esd_protection",
            term = "ESD (Elektrostatische Entladung)",
            termEn = "ESD (Electrostatic Discharge)",
            category = "Sicherheit & Werkstatt",
            shortDefinition = "Entladung statischer Elektrizität des menschlichen Körpers in empfindliche Mikrochips.",
            shortDefinitionEn = "Sudden flow of static electricity from human body into sensitive microchips.",
            detailedExplanation = "Der Mensch spürt statische Funken erst ab ca. 3.000 Volt. Halbleiterstrukturen in modernen CPUs, Grafikkarten und RAM-Riegeln werden jedoch bereits ab 30 bis 100 Volt unbemerkt zerstört oder vorgeschädigt (Gate-Oxid-Durchbruch).",
            detailedExplanationEn = "Humans feel static shocks at >3000 V, but modern silicon chips (CPUs, GPUs, RAM) suffer permanent gate oxide damage from as little as 30 to 100 V.",
            practicalTip = "Vor dem Anfassen von PC-Komponenten oder Platinen kurz einen unlackierten metallischen Heizkörper berühren oder ein geerdetes ESD-Armband anlegen.",
            practicalTipEn = "Touch an unpainted metal radiator or wear a grounded antistatic wrist strap before handling bare PCBs.",
            dangerLevel = "INFO"
        ),
        GlossaryItem(
            id = "ldo_regulator",
            term = "LDO (Spannungsregler / Low-Dropout)",
            termEn = "LDO Voltage Regulator",
            category = "Elektronik & Halbleiter",
            shortDefinition = "Linearer Spannungsregler, der z.B. 5V sauber auf 3,3V für Mikrocontroller herunterregelt.",
            shortDefinitionEn = "Linear regulator dropping e.g. 5V smoothly to 3.3V for microcontrollers.",
            detailedExplanation = "LDOs (wie der AMS1117 oder AP2112) versorgen sensible ICs (ESP32, STM32, Audio-Chips) mit stabiler Gleichspannung. Da sie die Differenzspannung linear in Wärme umwandeln, werden sie bei Überlast oder Kurzschluss am Ausgang extrem heiß und riechen verbrannt.",
            detailedExplanationEn = "LDOs (like AMS1117, AP2112) feed microcontrollers clean 3.3V. Excess voltage is dissipated as heat; a short on the output rail causes them to heat up severely or fail.",
            practicalTip = "Wenn dein ESP32 oder Arduino nicht mehr leuchtet und der kleine Dreibeiner-Chip brennend heiß wird: Ausgangs-Pin auf Kurzschluss zu Masse prüfen!",
            practicalTipEn = "If your dev board won't turn on and the 3-pin chip burns your finger, measure continuity from 3.3V output to GND.",
            dangerLevel = "INFO"
        ),
        GlossaryItem(
            id = "mains_230v",
            term = "230V Netzspannung & Kondensatoren",
            termEn = "Mains Voltage (230V) & Primary Caps",
            category = "Lebenswichtige Sicherheit",
            shortDefinition = "Haushaltsstrom aus der Steckdose. Lebensgefahr bei Berührung!",
            shortDefinitionEn = "Wall outlet electricity. Severe hazard / risk of fatal electric shock!",
            detailedExplanation = "In Netzteilen, Waschmaschinen und Kaffeevollautomaten liegt lebensgefährliche Wechselspannung an. Selbst nachdem der Netzstecker gezogen wurde, speichern große Primär-Elektrolytkondensatoren (z.B. 450V 100µF) Gleichspannungen von über 325 Volt oft über Stunden hinweg!",
            detailedExplanationEn = "Power supplies, washers, and coffee machines carry lethal AC mains. Even after unplugging, bulk primary capacitors store lethal DC voltages (>325V) for hours.",
            practicalTip = "VOR jedem Öffnen: Netzstecker physisch ziehen! Große Kondensatoren niemals mit dem Schraubenzieher kurzschließen (Lichtbogengefahr), sondern über einen 1kΩ / 10W Leistungswiderstand entladen.",
            practicalTipEn = "ALWAYS unplug from wall first! Never short big capacitors with a screwdriver (arc flash risk); discharge safely with a 1kΩ / 10W resistor.",
            dangerLevel = "DANGER"
        ),
        GlossaryItem(
            id = "li_ion_safety",
            term = "Lithium-Ionen Akku Sicherheit",
            termEn = "Lithium-Ion Battery Safety",
            category = "Lebenswichtige Sicherheit",
            shortDefinition = "Hochenergetische Akkuzellen (Smartphones, E-Bikes, Laptops).",
            shortDefinitionEn = "High-energy storage cells (smartphones, e-bikes, laptops).",
            detailedExplanation = "Lithium-Polymer- und 18650-Akkus enthalten hochentzündliche Elektrolyte. Mechanische Beschädigung (z.B. Einstechen mit spitzem Werkzeug beim Akkutausch), Überladung oder Unterschreitung der Tiefentladeschwelle (< 2,5V) führt zum 'Thermal Runaway' – einem heftigen, selbstunterhaltenden Metallbrand mit giftigen Gasen.",
            detailedExplanationEn = "Puncturing or bending Li-Po/18650 packs causes thermal runaway — violent, self-sustaining chemical fires releasing toxic fluorine gases.",
            practicalTip = "Niemals metallische Werkzeuge zum Heraushebeln von verklebten Smartphone-Akkus verwenden! Nur Kunststoff-Spatel (Spudger) und bei Bedarf etwas Isopropanol zum Lösen des Klebers nutzen.",
            practicalTipEn = "Never use metal tools or sharp tweezers to pry glued batteries! Use plastic spudgers and a few drops of isopropanol to dissolve adhesive.",
            dangerLevel = "DANGER"
        ),
        GlossaryItem(
            id = "specialty_screws",
            term = "Spezialschrauben (Tri-Point, Pentalobe, Torx TR)",
            termEn = "Specialty Screws (Tri-Point, Pentalobe, Security Torx)",
            category = "Werkzeugkunde",
            shortDefinition = "Herstellerspezifische Schraubenköpfe zum Schutz vor unbefugtem Öffnen.",
            shortDefinitionEn = "Proprietary screw heads designed by OEMs to prevent unauthorized disassembly.",
            detailedExplanation = "Hersteller wie Apple (Pentalobe P2/P5, Tri-Point Y000), Nintendo (Tri-Wing) oder Dyson (Torx mit Sicherheitsstift / Tamper-Resistant) setzen auf Spezialprofile. Ein unpassender Kreuzschlitz dreht den weichen Schraubenkopf sofort rund ('Stripped Screw').",
            detailedExplanationEn = "OEMs use proprietary heads (Pentalobe, Tri-Point Y000, Security Torx with center pin). Using an incorrect Phillips bit will instantly strip the soft metal screw head.",
            practicalTip = "Immer den exakten Bit mit leichtem Anpressdruck gerade aufsetzen. Ist die Schraube bereits rundgedreht: Ein breites Gummiband zwischen Bit und Schraubenkopf legen für zusätzlichen Grip.",
            practicalTipEn = "Always use the exact precision bit. If a screw is starting to strip, place a flat rubber band between bit and screw head for extra grip.",
            dangerLevel = "INFO"
        ),
        GlossaryItem(
            id = "thermal_paste_pads",
            term = "Wärmeleitpaste & Pad-Stärken",
            termEn = "Thermal Paste & Thermal Pad Thickness",
            category = "Kühlung & Wartung",
            shortDefinition = "Materialien zur Schließung mikroskopischer Lufteinschlüsse zwischen Chip und Kühlkörper.",
            shortDefinitionEn = "Interface materials displacing microscopic air pockets between die and heatsink.",
            detailedExplanation = "Luft ist ein extrem schlechter Wärmeleiter (0,026 W/mK). Wärmeleitpaste (ca. 8–14 W/mK) verdrängt Luft. Bei Grafikkarten und Konsolen kühlen Wärmeleitpads zusätzlich VRMs und VRAM. Ein zu dickes Pad (z.B. 1,5 mm statt 0,5 mm) drückt den Kühlkörper hoch, sodass die Haupt-GPU keinen Kontakt mehr hat und binnen Sekunden überhitzt!",
            detailedExplanationEn = "Air is a severe insulator. Thermal pads cool VRAM/VRMs; using a pad that is too thick (1.5mm instead of 0.5mm) lifts the heatsink off the GPU die causing instant thermal throttling.",
            practicalTip = "Alte Paste stets rückstandslos mit 99,9% Isopropanol entfernen. Paste hauchdünn bzw. als erbsengroßen Punkt in der Mitte auftragen; der Anpressdruck verteilt sie automatisch.",
            practicalTipEn = "Clean old crusty paste with 99.9% isopropanol. Apply a pea-sized dot in the center; cooler mounting pressure spreads it evenly.",
            dangerLevel = "INFO"
        )
    )
}
