package org.oreo.customCrafting.data.itemMeta.itemSpecific

data class FireworkEffectData(
    val colors: List<String>, // List of colors (as hex code strings)
    val fadeColors: List<String>?, // Colors the firework fades to (if any)
    val flicker: Boolean,
    val trail: Boolean,
    val type: String // Firework effect type (e.g., "BALL", "STAR")
)
