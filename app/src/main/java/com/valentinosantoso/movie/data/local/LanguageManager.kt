package com.valentinosantoso.movie.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LanguageManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("movie_lang_prefs", Context.MODE_PRIVATE)

    private val _languageFlow = MutableStateFlow(getSavedLanguage())
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    private fun getSavedLanguage(): String {
        return prefs.getString("selected_language", "id") ?: "id"
    }

    fun setLanguage(lang: String) {
        val validLang = if (lang.equals("en", ignoreCase = true)) "en" else "id"
        prefs.edit().putString("selected_language", validLang).apply()
        _languageFlow.value = validLang
    }

    fun toggleLanguage() {
        val nextLang = if (_languageFlow.value == "id") "en" else "id"
        setLanguage(nextLang)
    }

    val tmdbLanguage: String
        get() = if (_languageFlow.value == "en") "en-US" else "id-ID"

    val currentLocale: Locale
        get() = if (_languageFlow.value == "en") Locale.ENGLISH else Locale("id", "ID")
}
