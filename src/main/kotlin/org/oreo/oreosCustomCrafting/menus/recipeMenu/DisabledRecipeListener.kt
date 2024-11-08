package org.oreo.oreosCustomCrafting.menus.recipeMenu

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.oreo.oreosCustomCrafting.CustomCrafting

class DisabledRecipeListener : Listener {

    /**
     * Make disabled recipes not give results basically disabling them
     */
    @EventHandler
    fun playerCrafting(e : PrepareItemCraftEvent){

        if (!isRecipeDisabled(e.recipe)) return

        e.inventory.result = ItemStack(Material.AIR);
    }

    /**
     * Check if the recipe is disabled
     */
    fun isRecipeDisabled(recipe: Recipe?): Boolean {
        for (disabledRecipe in CustomCrafting.disabledRecipes){
            if (recipe?.result == disabledRecipe.result) return true
        }
        return false
    }
}