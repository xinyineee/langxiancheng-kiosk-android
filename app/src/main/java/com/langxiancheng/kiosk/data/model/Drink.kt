package com.langxiancheng.kiosk.data.model

/**
 * Represents a special drink in the LangXianCheng menu.
 *
 * @property id Unique drink identifier (e.g., "D1")
 * @property name Chinese name (e.g., "启程·梨想云")
 * @property englishName English name (e.g., "Departure · Pearfect Cloud")
 * @property tagline Short motto/slogan for the drink
 * @property heartCopy Emotional/inspirational copy for the result card
 * @property colorHex Brand color hex for this drink's accent
 * @property emoji Decorative emoji for display
 */
data class Drink(
    val id: String,
    val name: String,
    val englishName: String,
    val tagline: String,
    val heartCopy: String,
    val colorHex: String = "#FF6B1A",
    val emoji: String = "☕"
)
