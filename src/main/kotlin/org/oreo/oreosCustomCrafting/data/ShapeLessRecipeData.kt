package org.oreo.oreosCustomCrafting.data

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils

data class ShapeLessRecipeData(
    val name: String,
    val ingredients: List<ItemStack>,
    val fileResult : String?,
    val materialResult: Material?,
    val amount: Int
)

fun dataToShapeLessRecipe(data : ShapeLessRecipeData,) : ShapelessRecipe {

    val result: ItemStack = when {
        data.fileResult != null -> Utils.getCustomItem(data.fileResult) // Get custom item by name
        data.materialResult != null -> ItemStack(data.materialResult) // Use the material if it's a default item
        else -> throw IllegalArgumentException("Invalid recipe result")
    }


    val recipe = ShapelessRecipe(NamespacedKey.minecraft(data.name), result)

    for (ingredient in data.ingredients){
        recipe.addIngredient(ingredient)
    }

    return recipe
}


fun shapeLessRecipeToData(recipe : ShapelessRecipe , plugin: CustomCrafting) : ShapeLessRecipeData {

    recipe.ingredientList.forEach { ingredient ->}

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

    return ShapeLessRecipeData(
        name = recipe.key.key,
        ingredients = recipe.ingredientList,
        fileResult = fileResult,
        materialResult = materialResult,
        amount = recipe.result.amount,
    )

}

