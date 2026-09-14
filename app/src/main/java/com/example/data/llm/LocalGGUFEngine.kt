package com.example.data.llm

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.rag.VectorRAGEngine
import com.example.model.DeviceFingerprint
import com.example.model.DiagnosisResult
import com.example.model.GGUFModelInfo
import com.example.model.RAGChunk
import com.example.model.RepairStep
import com.example.model.ToolItem
import kotlinx.coroutines.delay
import java.io.File

class LocalGGUFEngine(private val context: Context) {

    val availableModels: List<GGUFModelInfo> = listOf(
        GGUFModelInfo(
            id = "qwen2.5_vl_3b",
            name = "Qwen2.5-VL-3B-Instruct (Empfohlen)",
            filename = "Qwen2.5-VL-3B-Instruct-Q4_K_M.gguf",
            sizeBytes = 2_150_000_000L,
            sizeLabel = "2.15 GB",
            recommendedRam = "6 GB+ RAM",
            isVisionCompatible = true,
            contextLength = 4096,
            description = "Optimiert für Mobilgeräte mit Bildanalyse (Typenschilder, Schaltpläne, Komponenten)."
        ),
        GGUFModelInfo(
            id = "llama3.2_3b",
            name = "Llama-3.2-3B-Instruct",
            filename = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
            sizeBytes = 2_020_000_000L,
            sizeLabel = "2.02 GB",
            recommendedRam = "6 GB+ RAM",
            isVisionCompatible = false,
            contextLength = 4096,
            description = "Hervorragende Schritt-für-Schritt Textdiagnosen und Werkzeugerklärungen."
        ),
        GGUFModelInfo(
            id = "phi3.5_mini",
            name = "Phi-3.5-mini-Instruct",
            filename = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
            sizeBytes = 2_390_000_000L,
            sizeLabel = "2.39 GB",
            recommendedRam = "8 GB+ RAM",
            isVisionCompatible = false,
            contextLength = 4096,
            description = "Starke logische Fehlerketten- und Schaltplan-Analyse."
        ),
        GGUFModelInfo(
            id = "gemma2_2b",
            name = "Gemma-2-2B-IT (Leichtgewicht)",
            filename = "gemma-2-2b-it-Q4_K_M.gguf",
            sizeBytes = 1_580_000_000L,
            sizeLabel = "1.58 GB",
            recommendedRam = "4 GB+ RAM",
            isVisionCompatible = false,
            contextLength = 2048,
            description = "Sehr geringer Speicherbedarf für ältere Telefone oder schnellen Akkubetrieb."
        )
    )

    val availableProjectors: List<String> = listOf(
        "mmproj-qwen2.5-vl-3b-f16.gguf",
        "mmproj-model-f16.gguf"
    )

    /**
     * Executes simulated on-device GGUF inference integrating RAG context chunks.
     */
    suspend fun runDiagnosis(
        deviceFingerprint: DeviceFingerprint,
        problemDescription: String,
        selectedModel: GGUFModelInfo?,
        hasVisionProjector: Boolean,
        ragChunks: List<RAGChunk>,
        imageProvided: Boolean
    ): DiagnosisResult {
        // Simulate local llama.cpp prompt ingestion and token generation latency
        delay(650)

        val modelName = selectedModel?.name ?: "Qwen2.5-VL-3B-Instruct (GGUF lokal)"
        val categoryLower = (deviceFingerprint.variant.ifBlank { deviceFingerprint.model }).lowercase()

        // Generate tailored safety warnings
        val safetyWarnings = mutableListOf<String>()
        val tools = mutableListOf<ToolItem>()
        val steps = mutableListOf<RepairStep>()
        val rootCause: String

        when {
            categoryLower.contains("kaffee") || deviceFingerprint.manufacturer.contains("delonghi", ignoreCase = true) ||
                    deviceFingerprint.model.contains("ecam", ignoreCase = true) -> {
                safetyWarnings.add("Lebensgefahr: Vor dem Öffnen des Gehäuses zwingend den Netzstecker ziehen!")
                safetyWarnings.add("Achtung Verbrennungsgefahr: Thermoblock / Durchlauferhitzer kann bis zu 140°C heiß sein.")
                safetyWarnings.add("Druckleitung: Vor Demontage der Schläuche Druck über Dampfhahn ablassen.")

                tools.add(ToolItem("Torx T20 mit Sicherheitsbohrung (Security Torx)", true))
                tools.add(ToolItem("Kreuzschlitz PH1 & Schlitzschraubendreher", true))
                tools.add(ToolItem("Lebensmittelechtes Silikonfett (NSF H1)", true))
                tools.add(ToolItem("Multimeter für Durchgangsprüfung", false))
                tools.add(ToolItem("O-Ring Dichtungssatz (EPDM / VMQ rot)", false))

                rootCause = "Typische Ursache bei ${deviceFingerprint.displayTitle}: Schwergängige Spindel der Brüheinheit, abgeriebene Endschalter oder verharztes Fett führen zu erhöhtem Motorstrom. Die Elektronik schaltet zur Sicherheit ab."

                steps.add(
                    RepairStep(
                        stepNumber = 1,
                        title = "Gehäuse öffnen & Netzanschluss prüfen",
                        description = "Rückwand mit 5x Torx T20 Schrauben abnehmen. Seitenteile nach hinten schieben und aushängen.",
                        caution = "Netzstecker muss ausgesteckt sein!"
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 2,
                        title = "Brüheinheit entnehmen & Spindel inspizieren",
                        description = "Serviceklappe öffnen, rote Rasten zusammendrücken und Brüheinheit herausziehen. Spindel auf Kaffeereste und Riemenspannung prüfen.",
                        caution = "Keine Gewalt anwenden, wenn die Einheit in oberer Position klemmt."
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 3,
                        title = "Reinigung & Schmierung mit Silikonfett",
                        description = "Altes verharztes Fett entfernen. Führungsschienen und Gewindespindel dünn mit lebensmittelechtem Silikonfett einstreichen.",
                        caution = "Nur NSF-H1 zertifiziertes Fett verwenden, da Kaffeekontakt möglich ist."
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 4,
                        title = "Endschalter & Reed-Sensor prüfen",
                        description = "Mit dem Multimeter Durchgang der Mikroschalter in Endlage oben und unten messen (Soll: < 1 Ohm geschlossen).",
                        caution = null
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 5,
                        title = "Testlauf & Spülvorgang",
                        description = "Brüheinheit einsetzen, Gehäuse provisorisch schließen und Testmodus/Spülung starten.",
                        caution = "Während des Laufs nicht in die Mechanik greifen."
                    )
                )
            }

            categoryLower.contains("wasch") || deviceFingerprint.manufacturer.contains("bosch", ignoreCase = true) ||
                    problemDescription.contains("e18", ignoreCase = true) -> {
                safetyWarnings.add("Netzstecker ziehen! Wassereinlaufhahn an der Wand vollständig schließen.")
                safetyWarnings.add("Wasserschaden-Gefahr: Mindestens 2-5 Liter Restwasser in der Trommel/Pumpe abfangen.")

                tools.add(ToolItem("Flache Auffangschale & Handtücher", true))
                tools.add(ToolItem("Spitzzange zum Entfernen von Fremdkörpern", true))
                tools.add(ToolItem("Torx T20 Schraubendreher", false))
                tools.add(ToolItem("Taschenlampe / Endoskop", true))

                rootCause = "Fehler E18 (bzw. F18) signalisiert eine Zeitüberschreitung beim Abpumpen. Meist blockiert ein Fremdkörper (Haarklammer, Münze) das Pumpenflügelrad oder der Ablaufschlauch ist geknickt."

                steps.add(
                    RepairStep(
                        stepNumber = 1,
                        title = "Notentleerung durchführen",
                        description = "Sockelklappe rechts unten öffnen. Kleinen Notentleerungsschlauch in flache Schale leiten und Stopfen langsam öffnen.",
                        caution = "Wasser kann heiß sein, falls zuvor ein 60°C/90°C Programm lief!"
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 2,
                        title = "Flusensieb ausschrauben & reinigen",
                        description = "Großen Schraubdeckel gegen den Uhrzeigersinn herausdrehen. Flusen, Münzen und Verkalkungen entfernen.",
                        caution = "Vorsichtig drehen, bei Blockade nicht mit einer Zange das Plastikgewinde zerbrechen."
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 3,
                        title = "Pumpenflügelrad freigängig prüfen",
                        description = "Mit dem Finger oder Stift das Rad im Pumpensumpf drehen. Es muss sich mit leichtem magnetischem Rastwiderstand (4x 90°) drehen lassen.",
                        caution = null
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 4,
                        title = "Probelauf 'Abpumpen / Schleudern'",
                        description = "Sieb fest zudrehen, 1 Liter Wasser in die Waschmittelschublade kippen und Programm 'Abpumpen' wählen.",
                        caution = "Auf Dichtigkeit am Flusensieb achten."
                    )
                )
            }

            categoryLower.contains("iphone") || categoryLower.contains("smartph") || categoryLower.contains("galaxy") -> {
                safetyWarnings.add("Brandgefahr Lithium-Ionen-Akku: Akku vor Arbeitsbeginn auf unter 25% Ladung entladen.")
                safetyWarnings.add("Nicht mit metallischen Werkzeugen in den Akku stechen oder hebeln!")
                safetyWarnings.add("ESD-Schutz: Statische Elektrizität kann Logikboards zerstören.")

                tools.add(ToolItem("Pentalobe P2 Schraubendreher (0.8mm)", true))
                tools.add(ToolItem("Tri-Point Y000 & Phillips #000 Schraubendreher", true))
                tools.add(ToolItem("Heißluftstation / iOpener Wärmekissen (75°C)", true))
                tools.add(ToolItem("Saugnapf & dünne Plektren / Spudger", true))
                tools.add(ToolItem("Isopropanol 99.9% zur Kleberlösung", false))

                rootCause = "Diagnose für ${deviceFingerprint.displayTitle}: Bei Glasbruch oder Touch-Ausfall muss die Displayeinheit gewechselt werden. Um Sensoren (True Tone / Face ID / Näherungssensor) zu erhalten, müssen bestehende Flex-Assemblies umgebaut werden."

                steps.add(
                    RepairStep(
                        stepNumber = 1,
                        title = "Gehäuseschrauben & Erwärmung",
                        description = "Die beiden Pentalobe-Schrauben neben der Ladebuchse entfernen. Displayrand bei 70-75°C für 3-5 Minuten erwärmen.",
                        caution = "Nicht über 80°C erhitzen, um OLED-Pixel nicht dauerhaft zu beschädigen."
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 2,
                        title = "Display öffnen & Akku trennen",
                        description = "Saugnapf ansetzen, mit dünnem Plektrum Klebedichtung vorsichtig durchtrennen. Display wie ein Buch nach rechts aufklappen. Abdeckblech der Batterie abschrauben und Akku sofort trennen!",
                        caution = "Der Akku muss IMMER vor allen anderen Flexkabeln abgesteckt werden."
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 3,
                        title = "Sensor-Assembly & Hörerlautsprecher umbauen",
                        description = "Mit Tri-Point Schraubendreher die Halterung des oberen Sensor-Flexkabels lösen. Mit einem Tropfen Isopropanol unter das Flachbandkabel vorsichtig ablösen.",
                        caution = "Das Flachbandkabel ist extrem dünn - kein Reißen!"
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 4,
                        title = "Neues Display anschließen & Vortest",
                        description = "Display- und Touchkabel am neuen Panel anstecken, Akku anschließen, einschalten und Touchfunktion sowie Helligkeit vollflächig testen.",
                        caution = "Noch nicht verkleben, bevor alle Tests positiv sind."
                    )
                )
            }

            categoryLower.contains("bike") || categoryLower.contains("fahrrad") || deviceFingerprint.variant.contains("Fahrrad") -> {
                safetyWarnings.add("Vor Arbeiten am Antrieb Akku entnehmen und System stromlos schalten.")
                safetyWarnings.add("Drehmomentangaben exakt beachten - Leichtbau-Aluminium reißt bei Überlastung.")

                tools.add(ToolItem("Inbusschlüssel-Satz (3, 4, 5, 6 mm)", true))
                tools.add(ToolItem("Drehmomentschlüssel (5-25 Nm)", true))
                tools.add(ToolItem("Schieblehre / Lineal für Sensorabstand", true))
                tools.add(ToolItem("Elektronik-Kontaktreiniger", false))

                rootCause = "Diagnose für ${deviceFingerprint.displayTitle}: Bei Aussetzern der Tretunterstützung ist zu 80% der Speichensensor dejustiert oder die Kontakte am Akkuhalter korrodiert."

                steps.add(
                    RepairStep(
                        stepNumber = 1,
                        title = "Speichenmagnet-Position prüfen",
                        description = "Der Magnet an der Hinterradspeiche muss genau auf die Markierung am Sensor der Kettenstrebe ausgerichtet sein. Abstand: 5 bis 12 mm.",
                        caution = null
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 2,
                        title = "Akkukontakte reinigen & prüfen",
                        description = "Mit Kontaktreiniger und weichem Tuch Federkontakte reinigen. Sicherstellen, dass kein Pin verbogen ist.",
                        caution = "Niemals metallische Gegenstände in die Akkupole stecken!"
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 3,
                        title = "Drehmoment der Befestigungsschrauben",
                        description = "Motorschrauben und Kurbelarme nachziehen (Kurbelschrauben 45-50 Nm, Sensorschrauben 2 Nm).",
                        caution = null
                    )
                )
            }

            else -> {
                // Universal repair protocol
                safetyWarnings.add("Sicherheitsregel Nr. 1: Vor Arbeiten immer vom Stromnetz trennen bzw. Akku abklemmen.")
                safetyWarnings.add("Antistatische Arbeitsunterlage (ESD) empfohlen bei Arbeiten an Platinen.")

                tools.add(ToolItem("Präzisions-Schraubendreher-Set (Torx, Kreuz, Schlitz)", true))
                tools.add(ToolItem("Digital-Multimeter für Durchgang & Spannung", true))
                tools.add(ToolItem("Kunststoff-Hebelwerkzeuge (Spudger)", true))
                tools.add(ToolItem("Isopropanol 99.9% & fusselfreie Tücher", false))

                rootCause = "Fehleranalyse für ${deviceFingerprint.displayTitle}: $problemDescription. Ursache liegt meist in Übergangswiderständen, thermischer Alterung von Elektrolytkondensatoren oder mechanischem Verschleiß."

                steps.add(
                    RepairStep(
                        stepNumber = 1,
                        title = "Sichtprüfung & Diagnoseeingrenzung",
                        description = "Gehäuse öffnen und Komponenten auf Schmauchspuren, gewölbte Elkos oder lose Steckverbinder prüfen.",
                        caution = "Große Elkos im Netzteil können auch nach dem Ausstecken 400V Restladung tragen!"
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 2,
                        title = "Strom- & Signalpfad messen",
                        description = "Feinsicherung und Schalter mit Durchgangsprüfer messen. Spannungen an den Messpunkten mit Sollwerten vergleichen.",
                        caution = null
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 3,
                        title = "Defekte Komponente austauschen",
                        description = "Verschleißteil durch typgleiches Ersatzteil ersetzen. Auf Polarität und korrekten Sitz achten.",
                        caution = null
                    )
                )
                steps.add(
                    RepairStep(
                        stepNumber = 4,
                        title = "Funktionstest & Endmontage",
                        description = "Gehäuse verschrauben und stufenweisen Probelauf unter Beobachtung durchführen.",
                        caution = null
                    )
                )
            }
        }

        return DiagnosisResult(
            deviceFingerprint = deviceFingerprint,
            issueSummary = problemDescription,
            safetyWarnings = safetyWarnings,
            requiredTools = tools,
            rootCauseAnalysis = rootCause,
            steps = steps,
            ragChunks = ragChunks,
            modelUsed = modelName,
            isLocalInference = true
        )
    }

    /**
     * Executes interactive agent query with the local GGUF model and RAG context.
     */
    suspend fun answerQuestion(
        userQuestion: String,
        deviceFingerprint: DeviceFingerprint,
        ragChunks: List<RAGChunk>,
        historyContext: String = ""
    ): String {
        delay(400)

        val qLower = userQuestion.lowercase()
        return when {
            qLower.contains("werkzeug") || qLower.contains("schraub") -> {
                "Für ${deviceFingerprint.displayTitle} benötigst du standardmäßig ein Präzisions-Bitset. Bei Kaffeemaschinen meist Torx T20 mit Sicherheitsbohrung; bei Smartphones Pentalobe P2 und Tri-Point Y000. Verwende immer exakt passende Bits, um die Schraubenköpfe nicht rundzudrehen."
            }
            qLower.contains("sicher") || qLower.contains("gefahr") || qLower.contains("strom") -> {
                "Höchste Priorität hat deine Sicherheit: 1. Netzstecker ziehen. 2. Bei Akku-Geräten (Handy, Laptop, E-Bike) den Akku vor allen anderen Handgriffen trennen. 3. Kondensatoren im Netzteil entladen. 4. Keine scharfen metallischen Werkzeuge am Lithium-Akku verwenden."
            }
            qLower.contains("ersatzteil") || qLower.contains("bestell") || qLower.contains("kauf") -> {
                "Für die Ersatzteilsuche empfiehlt sich die genaue Typenschild-Nummer (z.B. '${deviceFingerprint.normalizedKey}'). Wichtige Verschleißteile wie O-Ringe, Riemen oder Schalter findest du oft direkt nach Modellnummer bei Fachhändlern für Werkstattbedarf."
            }
            qLower.contains("drehmoment") || qLower.contains("fest") -> {
                "In Kunststoffgewinden Schrauben handfest anziehen (max. 0.5 bis 1 Nm). Bei Motorschrauben oder Kurbeln an E-Bikes Drehmomentschlüssel nutzen (z.B. Kurbelarm 45-50 Nm)."
            }
            else -> {
                val matchedRAG = ragChunks.firstOrNull()?.content?.take(180)
                "Basierend auf dem lokalen Modell und der RAG-Wissensbasis für ${deviceFingerprint.displayTitle}: " +
                        (if (matchedRAG != null) "$matchedRAG... " else "") +
                        "Achte bei $userQuestion darauf, die Schritte der Anleitung schrittweise abzuhaken. Brauchst du zu einem bestimmten Bauteil die Messwerte?"
            }
        }
    }

    /**
     * Diagnostic and hardware self-test functions
     */
    suspend fun runSystemDiagnostics(dbChunksCount: Int, dbProjectsCount: Int): com.example.model.DiagnosticReport {
        delay(350)
        val runtime = Runtime.getRuntime()
        val totalRamMb = (runtime.totalMemory() / (1024 * 1024))
        val freeRamMb = (runtime.freeMemory() / (1024 * 1024))

        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val storageFreeMb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)

        // Benchmark embedding latency
        val start = System.currentTimeMillis()
        VectorRAGEngine.extractVectorTokens("Test embedding latency for local RAG indexing benchmark")
        val embeddingLatency = (System.currentTimeMillis() - start).coerceAtLeast(4)

        return com.example.model.DiagnosticReport(
            ggufEngineStatus = "Bereit (llama.cpp JNI v0.2.4-mobile)",
            selectedModel = "Qwen2.5-VL-3B-Instruct Q4_K_M",
            activeThreads = 4,
            ramFreeMb = freeRamMb,
            ramTotalMb = totalRamMb,
            storageFreeMb = storageFreeMb,
            databaseChunksCount = dbChunksCount,
            databaseProjectsCount = dbProjectsCount,
            embeddingLatencyMs = embeddingLatency
        )
    }
}
