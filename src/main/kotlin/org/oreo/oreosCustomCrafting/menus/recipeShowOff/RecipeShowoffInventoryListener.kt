package org.oreo.oreosCustomCrafting.menus.recipeShowOff

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.PlayerInventory

class CustomCraftingInventoryListener : Listener {

    /**
     * Handles any clicking of items that shouldn't be moved.
     */
    @EventHandler
    fun handleInvalidClick(e: InventoryClickEvent) {

        // Cancel the event for any other slot in the custom crafting inventory
        val craftInvInstance = RecipeShowoffInventory.getCustomCraftingInventory(e.inventory) ?: return

        e.isCancelled = true

        craftInvInstance.closeInventory()
    }


    /**
     * Make sure the object is properly deleted
     */
    @EventHandler
    fun playerCloseCraftingInv(e: InventoryCloseEvent) {

        if (!RecipeShowoffInventory.isCustomInventory(e.inventory)) return

        RecipeShowoffInventory.getCustomCraftingInventory(e.inventory)?.closeInventory() ?: return
    }
}
