package org.oreo.oreosCustomCrafting.menus.recipeGroupMenu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class RecipeGroupMenuListener : Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val menuInventory = RecipeGroupMenu.getInstance(e.inventory) ?: return

        menuInventory.handleClickedItem(e.slot)

        e.isCancelled = true
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val menuInventory = RecipeGroupMenu.getInstance(e.inventory) ?: return

        menuInventory.closeInventory()
    }
}