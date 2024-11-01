package org.oreo.oreosCustomCrafting.data

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils

data class ShapedRecipeData(
    val rows: List<String>,
    val name: String,
    val ingredients: Map<Char, Material>,
    val fileResult : String?,
    val materialResult: Material?,
)


/**
 * Converts data back into a ShapedRecipe.
 */
fun dataToShapedRecipe(data: ShapedRecipeData): ShapedRecipe {
    val value: ItemStack = when {
        data.fileResult != null -> Utils.getCustomItem(data.fileResult) // Get custom item by name
        data.materialResult != null -> ItemStack(data.materialResult) // Use the material if it's a default item
        else -> throw IllegalArgumentException("Invalid recipe result")
    }

    val recipe = ShapedRecipe(NamespacedKey.minecraft(data.name), value)
    recipe.shape(*data.rows.toTypedArray())

    data.ingredients.forEach { (char, material) ->
        recipe.setIngredient(char, material)
    }

    return recipe
}

/**
 * Converts a ShapedRecipe into ShapedRecipeData for Json serialization.
 */
fun shapedRecipeToData(recipe: ShapedRecipe, plugin: CustomCrafting): ShapedRecipeData {
    val rows = recipe.shape.toList()
    val ingredients = mutableMapOf<Char, Material>()

    recipe.ingredientMap.forEach { (key, itemStack) ->
        itemStack?.let {
            ingredients[key] = itemStack.type
        }
    }

    val (fileResult, materialResult) = if (Utils.isCustomItem(recipe.result)) {
        val resultItem = recipe.result

        if (Utils.customItemExists(resultItem)) {
            val customItemName: String = CustomCrafting.customItems.getKeyFromValue(resultItem)!!
            Pair(customItemName, null) // Custom item, materialResult is null
        } else {
            val fileName = Utils.saveCustomItemAsFile(resultItem, plugin = plugin)!!.name
            Pair(fileName, null) // Custom item, materialResult is null
        }
    } else {
        Pair(null, recipe.result.type) // Default item, fileResult is null
    }

    return ShapedRecipeData(
        rows = rows,
        ingredients = ingredients,
        name = recipe.key.key,
        fileResult = fileResult,
        materialResult = materialResult
    )
}


fun <K, V> HashMap<K, V>.getKeyFromValue(value: V): K? {
    return this.entries.firstOrNull { it.value == value }?.key
}


