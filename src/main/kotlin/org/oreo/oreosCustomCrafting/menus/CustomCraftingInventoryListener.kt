package org.oreo.oreosCustomCrafting.menus

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.PlayerInventory
import org.oreo.oreosCustomCrafting.menus.customCrafting.CustomCraftingInventory

class   CustomCraftingInventoryListener : Listener {

    /**
     * Handles any clicking of items that shouldn't be moved.
     */
    @EventHandler
    fun handleInvalidClick(e: InventoryClickEvent) {

        val invInstance = AbstractInventoryMenu.getCustomInventory(e.inventory) ?: return

        e.isCancelled = true

        invInstance.handleClickedItem(e.slot)
    }


    /**
     * Make sure the object is properly deleted
     */
    @EventHandler
    fun playerCloseCraftingInv(e: InventoryCloseEvent) {

        val invInstance = AbstractInventoryMenu.getCustomInventory(e.inventory) ?: return

        invInstance.closeInventory()
    }
}
