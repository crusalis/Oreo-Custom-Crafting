package org.oreo.oreosCustomCrafting.menus.recipeGroupAssignmentMenu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class RecipeGroupAssignmentMenuListener : Listener {

    /**
     * Handles any clicking of items.
     */
    @EventHandler
    fun handleInvalidClick(e: InventoryClickEvent) {

        val menuInventory = RecipeGroupAssignmentMenu.getInstance(e.inventory) ?: return

        menuInventory.handleClickedItem(e.slot)

        e.isCancelled = true
    }

    /**
     * Make sure when the inventory is closed, the instance gets deleted
     */
    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val menuInventory = RecipeGroupAssignmentMenu.getInstance(e.inventory) ?: return

        menuInventory.closeInventory()
    }
}