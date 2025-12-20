package com.example.notecast.presentation.ui.settingsscreen

data class PermissionState(
    val isMicrophoneEnabled: Boolean = true,
    val isThemeEnabled: Boolean = true,
    val isSummaryEnabled: Boolean = true,
    val isMindMapEnabled: Boolean = true,
    val isBackgroundEnabled: Boolean = true,
    val isAutoSyncEnabled: Boolean = false,
    val isLocationEnabled: Boolean = false
)