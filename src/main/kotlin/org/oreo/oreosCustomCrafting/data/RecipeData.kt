package org.oreo.oreosCustomCrafting.data

import org.bukkit.Material

/**
 * Abstract class that ShapedRecipeData and ShapeLessRecipeData inherit from
 */
abstract class RecipeData(
) {
    abstract val name: String
    abstract val fileResult: String?
    abstract val materialResult: Material?
    abstract val amount: Int
    abstract var group : String?
}
