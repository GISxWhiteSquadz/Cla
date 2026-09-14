package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("core_repair_settings", Context.MODE_PRIVATE)

    private val _selectedModelId = MutableStateFlow(
        prefs.getString(KEY_MODEL_ID, "qwen2.5_vl_3b") ?: "qwen2.5_vl_3b"
    )
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    private val _selectedProjector = MutableStateFlow(
        prefs.getString(KEY_PROJECTOR, "") ?: ""
    )
    val selectedProjector: StateFlow<String> = _selectedProjector.asStateFlow()

    private val _webResearchAllowed = MutableStateFlow(
        prefs.getBoolean(KEY_WEB_RESEARCH, true)
    )
    val webResearchAllowed: StateFlow<Boolean> = _webResearchAllowed.asStateFlow()

    private val _contextSize = MutableStateFlow(
        prefs.getInt(KEY_CONTEXT_SIZE, 4096)
    )
    val contextSize: StateFlow<Int> = _contextSize.asStateFlow()

    private val _threads = MutableStateFlow(
        prefs.getInt(KEY_THREADS, 4)
    )
    val threads: StateFlow<Int> = _threads.asStateFlow()

    private val _disclaimerAccepted = MutableStateFlow(
        prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
    )
    val disclaimerAccepted: StateFlow<Boolean> = _disclaimerAccepted.asStateFlow()

    private val _language = MutableStateFlow(
        prefs.getString(KEY_LANGUAGE, "DE") ?: "DE"
    )
    val language: StateFlow<String> = _language.asStateFlow()

    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
        _disclaimerAccepted.value = accepted
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setSelectedModelId(id: String) {
        prefs.edit().putString(KEY_MODEL_ID, id).apply()
        _selectedModelId.value = id
    }

    fun setSelectedProjector(filename: String) {
        prefs.edit().putString(KEY_PROJECTOR, filename).apply()
        _selectedProjector.value = filename
    }

    fun setWebResearchAllowed(allowed: Boolean) {
        prefs.edit().putBoolean(KEY_WEB_RESEARCH, allowed).apply()
        _webResearchAllowed.value = allowed
    }

    fun setContextSize(size: Int) {
        prefs.edit().putInt(KEY_CONTEXT_SIZE, size).apply()
        _contextSize.value = size
    }

    fun setThreads(count: Int) {
        prefs.edit().putInt(KEY_THREADS, count).apply()
        _threads.value = count
    }

    companion object {
        private const val KEY_MODEL_ID = "pref_model_id"
        private const val KEY_PROJECTOR = "pref_projector"
        private const val KEY_WEB_RESEARCH = "pref_web_research"
        private const val KEY_CONTEXT_SIZE = "pref_context_size"
        private const val KEY_THREADS = "pref_threads"
        private const val KEY_DISCLAIMER_ACCEPTED = "pref_disclaimer_accepted"
        private const val KEY_LANGUAGE = "pref_language"
    }
}
