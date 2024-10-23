package org.oreo.customCrafting.data.itemMeta.itemSpecific

data class AttributeModifierData(
    val attribute: String, // Attribute name (e.g., "GENERIC_ATTACK_DAMAGE")
    val amount: Double,
    val operation: String, // Operation (e.g., "ADD_NUMBER", "MULTIPLY_SCALAR_1")
    val uuid: String // UUID of the modifier
)
