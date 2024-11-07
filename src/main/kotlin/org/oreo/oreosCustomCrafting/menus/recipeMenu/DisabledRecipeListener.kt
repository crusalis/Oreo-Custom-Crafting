package org.oreo.oreosCustomCrafting.menus.recipeMenu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class DisabledRecipeListener : Listener {

    @EventHandler
    fun playerCrafting(e : PrepareItemCraftEvent){

        val recipe = e.recipe



    }


}