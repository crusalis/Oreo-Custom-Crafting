package org.oreo.oreosCustomCrafting.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jdk.jshell.execution.Util
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.NamespacedKey
import org.bukkit.entity.Item
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils

data class ShapedRecipeData(
    val rows: List<String>,
    val name: String,
    val ingredients: Map<Char, Material>, // Remain as Map<Char, Material>
    val result: Either<Material, String> // Changed to Material
)

/**
 * Converts data back into a ShapedRecipe.
 */
fun dataToShapedRecipe(data: ShapedRecipeData): ShapedRecipe {
    // Create the result ItemStack from the Material
    val value : ItemStack = when(data.result){

        is Either.Left -> ItemStack(data.result.value)

        is Either.Right -> Utils.getCustomItem(data.result.value)

    }

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
            ingredients[key] = itemStack.type
        }
    }

    // Determine if the result is a custom item or a regular material
    val result: Either<Material, String> = if (Utils.isCustomItem(recipe.result)) {

        val result = recipe.result

        if (Utils.customItemExists(result)) {
            val customItemName : String = CustomCrafting.customItems.getKeyFromValue(result)!!

            Either.Right(customItemName)
        } else  {
            TODO()

        }


    } else {
        Either.Left(recipe.result.type)
    }

    return ShapedRecipeData(
        rows = rows,
        ingredients = ingredients,
        name = recipe.key.key,
        result = result
    )
}


fun <K, V> HashMap<K, V>.getKeyFromValue(value: V): K? {
    return this.entries.firstOrNull { it.value == value }?.key
}


