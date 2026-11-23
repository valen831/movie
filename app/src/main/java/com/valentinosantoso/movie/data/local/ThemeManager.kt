package com.valentinosantoso.movie.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    DARK,
    LIGHT
}

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("movie_theme_prefs", Context.MODE_PRIVATE)

    private val _themeModeFlow = MutableStateFlow(getSavedThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    private fun getSavedThemeMode(): ThemeMode {
        val name = prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeModeFlow.value = mode
    }

    fun toggleTheme() {
        val next = if (_themeModeFlow.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(next)
    }

    fun isDark(): Boolean {
        return _themeModeFlow.value == ThemeMode.DARK
    }
}
