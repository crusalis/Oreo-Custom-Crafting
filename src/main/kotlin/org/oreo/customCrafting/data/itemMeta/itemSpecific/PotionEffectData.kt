package org.oreo.customCrafting.data.itemMeta.itemSpecific

data class PotionEffectData(
    val type: String, // Potion effect type (e.g., "HEAL")
    val duration: Int,
    val amplifier: Int,
    val ambient: Boolean,
    val particles: Boolean,
    val icon: Boolean
)
