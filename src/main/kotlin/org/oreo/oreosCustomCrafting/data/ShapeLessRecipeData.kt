package org.oreo.oreosCustomCrafting.data

import com.google.gson.annotations.SerializedName
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils
import java.io.Serializable

/**
 * All the necessary data for a shapeless recipe to be created / saved as json
 * I made this so that I don't have to do a custom JSON serializer
 */
data class ShapeLessRecipeData(
    val ingredientsMaterials: List<Material>,
    val ingredientsItems: List<String>,
    @SerializedName("name") override val name: String,
    override val fileResult: String?,
    override val materialResult: Material?,
    override val amount: Int,
    override var group: String?
) : RecipeData()

/**
 * Converts data into a shapelessRecipe bukkit object
 */
fun dataToShapeLessRecipe(data: ShapeLessRecipeData): ShapelessRecipe {

    val result: ItemStack = when {
        data.fileResult != null -> {
            val customItem = Utils.getCustomItem(data.fileResult)
            customItem.amount = data.amount
            customItem
        } // Get custom item by name
        data.materialResult != null -> ItemStack(
            data.materialResult,
            data.amount
        ) // Use the material if it's a default item
        else -> throw IllegalArgumentException("Invalid recipe result")
    }

    val recipe = ShapelessRecipe(NamespacedKey.minecraft(data.name), result)

    for (ingredient in data.ingredientsMaterials) {
        recipe.addIngredient(ingredient)
    }

    return recipe
}

/**
 * Converts a shapeless recipe bukkit object into data
 */
fun shapeLessRecipeToData(
    recipe: ShapelessRecipe,
    plugin: CustomCrafting,
    ingredientsItems: List<String>
): ShapeLessRecipeData {
    val ingredientsMaterials = mutableListOf<Material>()

    // Process each ingredient in the recipe
    recipe.ingredientList.forEach { ingredient ->
        if (ingredient.type != Material.AIR) { // Avoid adding empty slots
            ingredientsMaterials.add(ingredient.type)
        }
    }

    // Determine result item data
    val (fileResult, materialResult) = if (Utils.isCustomItem(recipe.result)) {
        val customItemName = CustomCrafting.customItems.getKeyFromValue(recipe.result)
            ?: Utils.saveCustomItemAsFile(recipe.result, plugin)?.name
        Pair(customItemName, null)
    } else {
        Pair(null, recipe.result.type)
    }

    return ShapeLessRecipeData(
        name = recipe.key.key,
        ingredientsMaterials = ingredientsMaterials,
        ingredientsItems = ingredientsItems,

        fileResult = fileResult,
        materialResult = materialResult,
        amount = recipe.result.amount,
        group = null
    )
}


