package com.example.data.rag

import com.example.data.local.KnowledgeChunkEntity
import com.example.model.DeviceFingerprint
import com.example.model.RAGChunk
import kotlin.math.sqrt

object VectorRAGEngine {

    /**
     * Tokenizes text and extracts normalized terms and 3-grams for technical models and error codes.
     */
    fun extractVectorTokens(text: String): List<String> {
        val clean = text.lowercase().replace(Regex("[^a-z0-9äöüß\\s-_]"), " ")
        val rawTokens = clean.split(Regex("\\s+")).filter { it.length >= 2 }
        val tokens = mutableSetOf<String>()
        tokens.addAll(rawTokens)

        // Also add char n-grams for technical codes like "e18", "p0300", "qwen2", "ecam22110"
        for (token in rawTokens) {
            if (token.length in 3..6) {
                tokens.add(token)
            }
        }
        return tokens.toList()
    }

    /**
     * Computes cosine similarity between query tokens and chunk tokens.
     */
    fun computeSimilarity(queryTokens: List<String>, chunkTokens: List<String>): Float {
        if (queryTokens.isEmpty() || chunkTokens.isEmpty()) return 0f

        val querySet = queryTokens.toSet()
        val chunkSet = chunkTokens.toSet()

        val intersectionCount = querySet.intersect(chunkSet).size
        val dotProduct = intersectionCount.toDouble()
        val magnitudeQuery = sqrt(querySet.size.toDouble())
        val magnitudeChunk = sqrt(chunkSet.size.toDouble())

        if (magnitudeQuery == 0.0 || magnitudeChunk == 0.0) return 0f
        return (dotProduct / (magnitudeQuery * magnitudeChunk)).toFloat()
    }

    /**
     * Searches knowledge base chunks with vector similarity and fingerprint weighting.
     */
    fun search(
        query: String,
        deviceFingerprint: DeviceFingerprint?,
        chunks: List<KnowledgeChunkEntity>,
        limit: Int = 4
    ): List<RAGChunk> {
        val queryTokens = extractVectorTokens(query + " " + (deviceFingerprint?.displayTitle ?: ""))
        val targetFingerprintKey = deviceFingerprint?.normalizedKey ?: ""

        val scored = chunks.map { entity ->
            val chunkTokens = entity.vectorTokensCsv.split(",") + extractVectorTokens(entity.title + " " + entity.content)
            var score = computeSimilarity(queryTokens, chunkTokens)

            // Fingerprint match boost
            if (targetFingerprintKey.isNotBlank()) {
                if (entity.deviceFingerprintKey.equals(targetFingerprintKey, ignoreCase = true)) {
                    score += 0.45f
                } else if (entity.deviceFingerprintKey.contains(targetFingerprintKey) || targetFingerprintKey.contains(entity.deviceFingerprintKey)) {
                    score += 0.25f
                }
            }

            // Category match boost
            if (query.contains(entity.category, ignoreCase = true)) {
                score += 0.15f
            }

            val finalConfidence = (score.coerceIn(0.1f, 0.99f))
            RAGChunk(
                title = entity.title,
                content = entity.content,
                source = entity.source,
                confidence = finalConfidence,
                manufacturer = entity.manufacturer,
                model = entity.model
            ) to score
        }

        return scored
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    /**
     * Chunks a raw document into smaller, indexable paragraphs.
     */
    fun chunkDocument(
        text: String,
        deviceFingerprint: DeviceFingerprint,
        category: String,
        source: String
    ): List<KnowledgeChunkEntity> {
        val paragraphs = text.split("\n\n").filter { it.trim().length > 30 }
        return paragraphs.mapIndexed { index, paragraph ->
            val tokens = extractVectorTokens(paragraph)
            KnowledgeChunkEntity(
                deviceFingerprintKey = deviceFingerprint.normalizedKey,
                manufacturer = deviceFingerprint.manufacturer,
                model = deviceFingerprint.model,
                category = category,
                title = "Abschnitt ${index + 1}: ${paragraph.take(40).trim()}...",
                content = paragraph.trim(),
                vectorTokensCsv = tokens.joinToString(","),
                source = source,
                confidence = 0.92f
            )
        }
    }
}
