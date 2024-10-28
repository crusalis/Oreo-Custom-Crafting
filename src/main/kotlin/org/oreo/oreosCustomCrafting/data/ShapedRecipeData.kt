package org.oreo.oreosCustomCrafting.data

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.NamespacedKey

data class ShapedRecipeData(
    val rows: List<String>,
    val name: String,
    val ingredients: Map<Char, Material>, // Remain as Map<Char, Material>
    val result: Material // Changed to Material
)

/**
 * Converts data back into a ShapedRecipe.
 */
fun dataToShapedRecipe(data: ShapedRecipeData): ShapedRecipe {
    // Create the result ItemStack from the Material
    val value = ItemStack(data.result)

    val recipe = ShapedRecipe(NamespacedKey.minecraft(data.name), value)

    // Set the shape
    recipe.shape(*data.rows.toTypedArray())

    // Map ingredients back to the recipe
    data.ingredients.forEach { (char, material) ->
        recipe.setIngredient(char, material)
    }

    return recipe
}

/**
 * Converts a ShapedRecipe into ShapedRecipeData.
 */
fun shapedRecipeToData(recipe: ShapedRecipe): ShapedRecipeData {
    val rows = recipe.shape.toList()

    // Map ingredients to Material
    val ingredients = mutableMapOf<Char, Material>()

    recipe.ingredientMap.forEach { (key, itemStack) ->
        itemStack?.let {
            // Directly use the Material type for ingredients
            ingredients[key] = itemStack.type
        }
    }

    // Get the result material directly
    val result: Material = recipe.result.type

    return ShapedRecipeData(
        rows = rows,
        ingredients = ingredients,
        name = recipe.key.key,
        result = result
    )
}
