package org.oreo.oreosCustomCrafting.menus.recipeMenu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class RecipeMenuListener : Listener {

    /**
     * Handles any clicking of items that shouldn't be moved.
     */
    @EventHandler
    fun handleInvalidClick(e: InventoryClickEvent) {

        val menuInventory = RecipeMenu.getInstance(e.inventory) ?: return

        menuInventory.handleClickedItem(e.slot)

        e.isCancelled = true
    }

}