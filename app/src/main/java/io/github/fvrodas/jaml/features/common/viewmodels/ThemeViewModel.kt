package io.github.fvrodas.jaml.features.common.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.features.common.themes.JamlColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThemeViewModel : ViewModel() {

    private val _currentTheme: MutableStateFlow<JamlColors> = MutableStateFlow(JamlColors.Default)

    val currentTheme: StateFlow<JamlColors> get() = _currentTheme

    fun setLauncherTheme(theme: JamlColors = JamlColors.Default) {
        viewModelScope.launch {
            _currentTheme.update { theme }
        }
    }

}
