package org.oreo.oreosCustomCrafting.data

import org.bukkit.inventory.CraftingRecipe
import java.io.Serializable

/**
 * A data class used to get any kind all data of a custom recipe
 */
data class CustomRecipeData(
    val recipe: CraftingRecipe,
    val recipeData: RecipeData
) : Serializable



