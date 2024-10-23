package org.oreo.customCrafting.data.itemMeta

import org.oreo.customCrafting.data.itemMeta.itemSpecific.*

/**
 * Item meta also stored in a data class
 */
data class ItemMetaData(
    val displayName: String? = null,
    val lore: List<String>? = null,
    val enchantments: Map<String, Int>? = null, // String as key (enchantment name), Int as level
    val itemFlags: List<String>? = null,
    val unbreakable: Boolean = false,
    val customModelData: Int? = null,
    val attributeModifiers: List<AttributeModifierData>? = null, // List of attribute modifiers
    val damage: Int? = null, // For Damageable items
    val persistentData: Map<String, String>? = null, // Simplified to a map of key-value pairs
    val potionEffects: List<PotionEffectData>? = null, // For PotionMeta
    val skullOwner: String? = null, // For SkullMeta (player head owner)
    val bookMeta: BookMetaData? = null, // For BookMeta
    val bannerPatterns: List<BannerPatternData>? = null, // For BannerMeta
    val fireworkEffects: List<FireworkEffectData>? = null, // For FireworkMeta
    val leatherArmorColor: String? = null, // For LeatherArmorMeta (color as hex code)
    val mapViewId: Int? = null, // For MapMeta (simplified as an int reference to the map view)
    val repairCost: Int? = null // For Repairable items
)
