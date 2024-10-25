package org.oreo.oreosCustomCrafting.data

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe


data class ShapedRecipeData(
    val rows : List<String>,
    val ingredients : Map<Char, RecipeIngredient>,
    val name : String
)

data class RecipeIngredient(
    val type: IngredientType, // Either "Material" or "Exact"
    val materials: List<String>? = null, // List of Material names for MaterialChoice
    val items: List<Material>? = null // List of serialized ItemStack data for ExactChoice
)

enum class IngredientType {
    EXACT,
    MATERIAL,
}

/**
 * Converts data back into a Shaped recipe
 */
fun dataToShapedRecipe(data: ShapedRecipeData, result: ItemStack): ShapedRecipe {
    // Create a new ShapedRecipe with a unique key (you can customize the key if needed)
    val name = data.name
    val recipe = ShapedRecipe(org.bukkit.NamespacedKey.minecraft(name), result)

    // Set the shape (rows)
    recipe.shape(*data.rows.toTypedArray())  // Convert List<String> to vararg Array

    // Map ingredients back to the recipe
    data.ingredients.forEach { (char, ingredient) ->
        when (ingredient.type) {
            IngredientType.MATERIAL -> {
                // Use RecipeChoice.MaterialChoice for a material-based ingredient
                val materials = ingredient.materials?.map { Material.matchMaterial(it) } ?: emptyList()
                if (materials.isNotEmpty()) {
                    recipe.setIngredient(char, RecipeChoice.MaterialChoice(materials))
                }
            }
            IngredientType.EXACT -> {
                // Use RecipeChoice.ExactChoice for item-specific (ItemStack-based) ingredient
                val items = ingredient.items?.map { ItemStack(it) } ?: emptyList()
                if (items.isNotEmpty()) {
                    recipe.setIngredient(char, RecipeChoice.ExactChoice(items[0]))  // Assuming one item per ingredient
                }
            }
        }
    }

    return recipe
}

/**
 * Converts a shaped recipe into Serializable Data
 */
fun shapedRecipeToData(recipe: ShapedRecipe): ShapedRecipeData {
    // Get the shape (rows) of the recipe
    val shape = recipe.shape.toList()

    // Get the ingredient map (mapping characters to ItemStack/Material)
    val ingredients = mutableMapOf<Char, RecipeIngredient>()

    recipe.ingredientMap.forEach { (key, itemStack) ->
        if (itemStack != null) {
            val ingredient = if (itemStack.type != Material.AIR) {
                // Handle Exact (ItemStack-based) and Material (Material-based)
                if (itemStack.amount == 1) {
                    RecipeIngredient(
                        type = IngredientType.MATERIAL,
                        materials = listOf(itemStack.type.name) // Material name for MaterialChoice
                    )
                } else {
                    RecipeIngredient(
                        type = IngredientType.EXACT,
                        items = listOf(itemStack.type) // Exact choice with ItemStack data
                    )
                }
            } else {
                null
            }

            if (ingredient != null) {
                ingredients[key] = ingredient
            }
        }
    }

    return ShapedRecipeData(
        rows = shape,
        ingredients = ingredients,
        name = recipe.key.toString()
    )
}