package org.oreo.oreosCustomCrafting.customIngredientListener

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting

class CustomIngredientListener : Listener {

    @EventHandler
    fun allowCustomIngredients(e: PrepareItemCraftEvent) {
        val inventoryMatrix = e.inventory.matrix.filterNotNull() // Get the crafting grid contents and ignore null slots


        for (itemList in CustomCrafting.customIngredientRecipes) {

            val customIngredients = itemList.first
            val customResult = itemList.second

            // Check if the materials match
            if (areMaterialListsEqual(customIngredients, inventoryMatrix)) {
                // If materials match but ItemStacks don't match, disable the recipe
                if (!areItemStackListsEqual(customIngredients, inventoryMatrix)) {
                    e.inventory.result = ItemStack(Material.AIR) // Disable the recipe
                    return // Exit early since the recipe is disabled
                }

                // If both materials and ItemStacks match, allow the recipe (do nothing)
                if (e.recipe?.result == customResult) {
                    return // Recipe is valid, so exit
                }
            }
        }
        // If no custom recipes match, allow the recipe (do nothing)
    }

    /**
     * Checks if the materials and the custom recipe list's materials match
     */
    private fun areItemStackListsEqual(list1: List<ItemStack>, list2: List<ItemStack>): Boolean {
        // Early exit if sizes differ
        if (list1.size != list2.size) return false

        // Group items by their properties (e.g., material, amount, and item meta)
        val grouped1 = list1.groupingBy { it.clone() }.eachCount()
        val grouped2 = list2.groupingBy { it.clone() }.eachCount()

        // Check if the grouped maps are identical
        return grouped1 == grouped2
    }

    /**
     * Checks if the exact items and the custom recipe items
     */
    private fun areMaterialListsEqual(list1: List<ItemStack>, list2: List<ItemStack>): Boolean {
        // Early exit if sizes differ
        if (list1.size != list2.size) return false

        // Extract the materials from both lists
        val materials1 = list1.map { it.type }.sorted()
        val materials2 = list2.map { it.type }.sorted()

        // Compare the lists of materials
        return materials1 == materials2
    }
}