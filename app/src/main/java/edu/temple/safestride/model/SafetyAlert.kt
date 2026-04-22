package edu.temple.safestride.model

enum class SafetyAlertType {
    HIGH_SPEED,
    FALL_OR_IMPACT,
    EXTREME_ROTATION
}

data class SafetyAlert(
    val type: SafetyAlertType,
    val title: String,
    val message: String
)