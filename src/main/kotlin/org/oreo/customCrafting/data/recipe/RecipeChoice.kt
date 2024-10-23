package org.oreo.customCrafting.data.recipe

import org.oreo.customCrafting.data.itemMeta.ItemMetaData

/**
 * This allows us to use custom items as ingredients
 */
sealed class RecipeChoiceData {
    data class MaterialChoiceData(val materials: List<String>) : RecipeChoiceData() // List of material names
    data class ExactChoiceData(val items: List<ItemData>) : RecipeChoiceData() // List of item data (includes meta)
}

/**
 * The items data
 * If it only has material it is of type MaterialChoiceData
 * If it has any extra medata it is of type ExactChoiceData
 */
data class ItemData(
    val material: String, // Material name
    val amount: Int = 1, // Amount of the item
    val itemMetaData: ItemMetaData? = null // Optional item metadata
)

