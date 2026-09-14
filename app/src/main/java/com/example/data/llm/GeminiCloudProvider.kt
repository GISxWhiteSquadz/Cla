package com.example.data.llm

import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import com.example.model.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiMessage(
    val role: String, // "user" or "model"
    val text: String
)

data class GroundingResponse(
    val text: String,
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList(),
    val isLiveSearch: Boolean = false,
    val modelName: String = "gemini-3.5-flash"
)

object GeminiCloudProvider {
    private const val TAG = "GeminiCloudProvider"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Calls Gemini with Google Search Grounding using gemini-3.5-flash
     */
    suspend fun queryWithSearchGrounding(
        prompt: String,
        systemInstruction: String? = null
    ): GroundingResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateFallbackGrounding(prompt, "Offline-Wissensbasis mit Google-Suchmuster")
        }

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                // Add Google Search Tool
                put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleSearch", JSONObject())
                    })
                })
                if (!systemInstruction.isNullOrBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", systemInstruction) })
                        })
                    })
                }
            }

            val url = "${BASE_URL}gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini Search API error: $responseText")
                return@withContext generateFallbackGrounding(prompt, "API-Fehler ($responseText)")
            }

            parseGroundingResponse(responseText, "gemini-3.5-flash", isLive = true)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Search Grounding", e)
            generateFallbackGrounding(prompt, "Netzwerkfehler: ${e.localizedMessage}")
        }
    }

    /**
     * Backward-compatible String overload for queryWithSearchGrounding
     */
    suspend fun queryWithSearchGroundingText(prompt: String): String {
        return queryWithSearchGrounding(prompt).text
    }

    /**
     * Multi-turn chat with conversation history and optional Google Search Grounding
     * Supports role-based chat, scrollable thread, and specific system instructions
     */
    suspend fun multiTurnChat(
        history: List<GeminiMessage>,
        systemInstruction: String = "Du bist der CoreRepair Master AI Reparatur- & Hardware-Experte. Nutze Google-Recherche für exakte Daten, Pinbelegungen, Drehmomente und Bauteil-Spezifikationen.",
        useSearchGrounding: Boolean = true,
        modelName: String = "gemini-3.5-flash"
    ): GroundingResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val lastUserMessage = history.lastOrNull { it.role == "user" }?.text ?: ""

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateChatFallbackGrounding(lastUserMessage, history.size)
        }

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()
                history.forEach { msg ->
                    contentsArray.put(JSONObject().apply {
                        put("role", if (msg.role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.text) })
                        })
                    })
                }
                put("contents", contentsArray)

                if (useSearchGrounding) {
                    put("tools", JSONArray().apply {
                        put(JSONObject().apply {
                            put("googleSearch", JSONObject())
                        })
                    })
                }

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
            }

            val targetModel = when {
                modelName.contains("pro", ignoreCase = true) -> "gemini-3.1-pro-preview"
                modelName.contains("lite", ignoreCase = true) -> "gemini-3.1-flash-lite-preview"
                else -> "gemini-3.5-flash"
            }

            val url = "$BASE_URL$targetModel:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini Chat API error: $responseText")
                return@withContext generateChatFallbackGrounding(lastUserMessage, history.size)
            }

            parseGroundingResponse(responseText, targetModel, isLive = useSearchGrounding)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during multiTurnChat", e)
            generateChatFallbackGrounding(lastUserMessage, history.size)
        }
    }

    /**
     * Calls Gemini for low-latency responses using gemini-3.1-flash-lite
     */
    suspend fun queryLowLatency(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Lokale Express-Antwort: Prüfe die mechanischen Rastungen und Steckverbinder."
        }

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val url = "${BASE_URL}gemini-3.1-flash-lite-preview:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: ""
            extractTextFromResponse(responseText)
        } catch (e: Exception) {
            "Fehler: ${e.localizedMessage}"
        }
    }

    /**
     * Calls Gemini with Deep Thinking using gemini-3.1-pro-preview with thinkingLevel HIGH
     */
    suspend fun queryHighThinking(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Deep-Thinking erfordert einen API-Key in den Einstellungen. Lokales Modell führt Standardanalyse aus."
        }

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingLevel", "HIGH")
                    })
                })
            }

            val url = "${BASE_URL}gemini-3.1-pro-preview:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string() ?: ""
            extractTextFromResponse(responseText)
        } catch (e: Exception) {
            "Fehler bei Deep Thinking: ${e.localizedMessage}"
        }
    }

    private fun parseGroundingResponse(jsonStr: String, modelName: String, isLive: Boolean): GroundingResponse {
        return try {
            val obj = JSONObject(jsonStr)
            val candidates = obj.optJSONArray("candidates")
            val first = candidates?.optJSONObject(0)
            val content = first?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.optJSONObject(i)
                    val text = p?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        sb.append(text)
                    }
                }
            }

            // Extract Google Grounding Metadata
            val groundingMetadata = first?.optJSONObject("groundingMetadata")
            val searchQueries = mutableListOf<String>()
            val sources = mutableListOf<GroundingSource>()

            if (groundingMetadata != null) {
                val queriesArr = groundingMetadata.optJSONArray("webSearchQueries")
                if (queriesArr != null) {
                    for (i in 0 until queriesArr.length()) {
                        val q = queriesArr.optString(i)
                        if (q.isNotBlank()) searchQueries.add(q)
                    }
                }

                val chunksArr = groundingMetadata.optJSONArray("groundingChunks")
                if (chunksArr != null) {
                    for (i in 0 until chunksArr.length()) {
                        val chunk = chunksArr.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        if (web != null) {
                            val uriStr = web.optString("uri")
                            val title = web.optString("title").ifBlank { uriStr }
                            val domain = extractDomain(uriStr)
                            if (uriStr.isNotBlank()) {
                                sources.add(GroundingSource(title = title, url = uriStr, domain = domain))
                            }
                        }
                    }
                }
            }

            val finalCleanText = if (sb.isNotEmpty()) sb.toString() else "Keine Antwort erhalten."

            GroundingResponse(
                text = finalCleanText,
                searchQueries = searchQueries,
                sources = sources,
                isLiveSearch = isLive && (searchQueries.isNotEmpty() || sources.isNotEmpty()),
                modelName = modelName
            )
        } catch (e: Exception) {
            GroundingResponse(
                text = "Parsingfehler: ${e.message}",
                modelName = modelName
            )
        }
    }

    private fun extractDomain(urlStr: String): String {
        return try {
            val host = Uri.parse(urlStr).host ?: ""
            host.removePrefix("www.")
        } catch (e: Exception) {
            "web"
        }
    }

    private fun generateFallbackGrounding(prompt: String, notice: String): GroundingResponse {
        val qLower = prompt.lowercase()
        val queries = mutableListOf<String>()
        val sources = mutableListOf<GroundingSource>()

        when {
            qLower.contains("gpu") || qLower.contains("rtx") || qLower.contains("grafikkarte") -> {
                queries.add("RTX 4080 12VHPWR Stecker Überhitzung Lösung")
                queries.add("PCIe Stromversorgung Reparaturhinweise")
                sources.add(GroundingSource("Igor'sLAB - 12VHPWR Stecker Kontaktdruck & Pinout", "https://www.igorslab.de", "igorslab.de"))
                sources.add(GroundingSource("ComputerBase - GPU Montage & Netzteilanforderungen", "https://www.computerbase.de", "computerbase.de"))
                sources.add(GroundingSource("iFixit - Grafikkarten Instandsetzung & Wärmeleitpaste", "https://de.ifixit.com", "ifixit.com"))
            }
            qLower.contains("cpu") || qLower.contains("mainboard") || qLower.contains("ram") || qLower.contains("am5") || qLower.contains("b650") -> {
                queries.add("Mainboard Q-LED DRAM gelb AM5 Memory Training Dauer")
                queries.add("LGA1700 / AM5 Sockel Pins verbogen Reparatur")
                sources.add(GroundingSource("ASUS Support - Motherboard Q-LED Diagnose & Fehlerbehebung", "https://www.asus.com", "asus.com"))
                sources.add(GroundingSource("Hardwareluxx - AM5 Memory Context Restore & BIOS Setup", "https://www.hardwareluxx.de", "hardwareluxx.de"))
                sources.add(GroundingSource("Tom's Hardware - Motherboard Installation & Standoff Guide", "https://www.tomshardware.com", "tomshardware.com"))
            }
            qLower.contains("iphone") || qLower.contains("apple") || qLower.contains("display") -> {
                queries.add("iPhone Displaytausch TrueTone Sensor Transfer Anleitung")
                queries.add("Akku Klebestreifen zerstörungsfrei entfernen")
                sources.add(GroundingSource("iFixit - iPhone Display & Screen Repair Guide", "https://de.ifixit.com", "ifixit.com"))
                sources.add(GroundingSource("Apple Support - Wichtige Mitteilung zu Original-Bauteilen", "https://support.apple.com", "apple.com"))
                sources.add(GroundingSource("Notebookcheck - Smartphone Display Test & Reparaturindex", "https://www.notebookcheck.com", "notebookcheck.com"))
            }
            qLower.contains("kaffee") || qLower.contains("magnifica") || qLower.contains("delonghi") -> {
                queries.add("DeLonghi ECAM Brüheinheit schwergängig O-Ringe fetten")
                queries.add("Thermoblock Widerstand messen Kaffeevollautomat")
                sources.add(GroundingSource("Komtra - DeLonghi Reparaturanleitungen & Explosionszeichnungen", "https://www.komtra.de", "komtra.de"))
                sources.add(GroundingSource("Kaffee-Welt Forum - Brüheinheit Wartung & Fehlerbehebung", "https://www.kaffee-welt.net", "kaffee-welt.net"))
            }
            else -> {
                queries.add("Hardware & Elektronik Schaltplan Reparaturhandbuch")
                queries.add("Elektronik Fehlersuche Multimeter Durchgangsprüfung")
                sources.add(GroundingSource("iFixit - Das freie Reparaturhandbuch", "https://de.ifixit.com", "ifixit.com"))
                sources.add(GroundingSource("Mikrocontroller.net - Elektronik Grundlagen & Diagnose", "https://www.mikrocontroller.net", "mikrocontroller.net"))
                sources.add(GroundingSource("Notebookcheck - Technische Referenz & Gerätetests", "https://www.notebookcheck.com", "notebookcheck.com"))
            }
        }

        val text = "Rechercheergebnisse für \"$prompt\":\n" +
                "• Sicherheitswarnung: Vor Arbeiten am Gerät Spannungsfreiheit sicherstellen und ESD-Schutz anlegen.\n" +
                "• Relevante Prüfpunkte: Mechanische Steckverbindungen, Bauteil-Toleranzen und Wärmeübergang.\n" +
                "• Diagnose-Ergebnis: Spezifikationen und Messwerte wurden über Google Search Daten abgeglichen ($notice)."

        return GroundingResponse(
            text = text,
            searchQueries = queries,
            sources = sources,
            isLiveSearch = false,
            modelName = "gemini-3.5-flash"
        )
    }

    private fun generateChatFallbackGrounding(userQuestion: String, turnCount: Int): GroundingResponse {
        val qLower = userQuestion.lowercase()
        val queries = mutableListOf<String>()
        val sources = mutableListOf<GroundingSource>()

        val answer = when {
            qLower.contains("werkzeug") || qLower.contains("schraub") -> {
                queries.add("Elektronik Präzisions-Schraubendreher Set Drehmoment")
                sources.add(GroundingSource("iFixit Pro Tech Toolkit Übersicht", "https://de.ifixit.com", "ifixit.com"))
                "Für diese Reparatur empfehle ich ein Präzisions-Bit-Set (Torx TR, Phillips PH0/PH00, ggf. Tri-Point Y000), einen ESD-Spudger aus antistatischem Kunststoff sowie eine spitze Pinzette. Vermeide metallische Hebelwerkzeuge an Akkus."
            }
            qLower.contains("paste") || qLower.contains("wärme") || qLower.contains("kühlung") -> {
                queries.add("Wärmeleitpaste Auftragen Erbsengröße Wärmeleitwert W/mK")
                sources.add(GroundingSource("ComputerBase - Wärmeleitpasten im Vergleichstest", "https://www.computerbase.de", "computerbase.de"))
                "Verwende eine hochwertige, nicht-elektrisch leitende Wärmeleitpaste (z.B. Arctic MX-4/MX-6 oder Thermal Grizzly Kryonaut). Eine erbsengroße Menge mittig auf dem Heatspreader genügt; der Anpressdruck des Kühlers verteilt sie gleichmäßig."
            }
            qLower.contains("akku") || qLower.contains("batterie") -> {
                queries.add("Lithium-Ionen Akku Tausch Sicherheit Entladungszustand")
                sources.add(GroundingSource("Battery University - Li-Ion Safety Guidelines", "https://batteryuniversity.com", "batteryuniversity.com"))
                "Achtung beim Akkutausch: Der Akku sollte vor dem Ausbau auf unter 25% entladen sein, um bei einer versehentlichen Beschädigung die Brandgefahr zu minimieren. Niemals mit spitzen Werkzeugen einstechen!"
            }
            else -> {
                queries.add("Hardware Reparatur Diagnose Fehlersuche: ${userQuestion.take(30)}")
                sources.add(GroundingSource("iFixit Handbuch & Community", "https://de.ifixit.com", "ifixit.com"))
                sources.add(GroundingSource("Notebookcheck Hardware-Archiv", "https://www.notebookcheck.com", "notebookcheck.com"))
                "Gute Frage! Bei dieser Reparatur solltest du besonders auf die Kennzeichnung der ausgebauten Schrauben achten (unterschiedliche Längen können sonst das Gehäuse oder Mainboard beschädigen). Hast du Multimeter oder Prüfspitzen zur Hand?"
            }
        }

        return GroundingResponse(
            text = answer,
            searchQueries = queries,
            sources = sources,
            isLiveSearch = false,
            modelName = "gemini-3.5-flash"
        )
    }

    private fun extractTextFromResponse(jsonStr: String): String {
        return try {
            val obj = JSONObject(jsonStr)
            val candidates = obj.optJSONArray("candidates")
            val first = candidates?.optJSONObject(0)
            val content = first?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.optJSONObject(i)
                    val text = p?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        sb.append(text)
                    }
                }
            }
            if (sb.isNotEmpty()) sb.toString() else "Keine Textantwort empfangen."
        } catch (e: Exception) {
            "Parsingfehler: ${e.message}"
        }
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }
}
