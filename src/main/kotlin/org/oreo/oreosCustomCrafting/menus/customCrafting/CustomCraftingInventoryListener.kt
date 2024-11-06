package org.oreo.oreosCustomCrafting.menus.customCrafting

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
        // Check if the inventory is a custom crafting inventory
        if (!CustomCraftingInventory.isCustomInventory(e.inventory)) return
        //Let the player click in his own inventory
        if (e.clickedInventory is PlayerInventory) return
        // Allow clicks only in defined crafting slots and the result slot
        if (CustomCraftingInventory.CRAFTING_SLOTS.contains(e.slot) || CustomCraftingInventory.RESULT_SLOT == e.slot) return

        // Cancel the event for any other slot in the custom crafting inventory
        e.isCancelled = true

        val craftInvInstance = CustomCraftingInventory.getCustomCraftingInventory(e.inventory)


        if (e.currentItem == craftInvInstance!!.acceptButton){
            craftInvInstance.saveRecipe()
            craftInvInstance.closeInventory()
        } else if (e.currentItem == craftInvInstance.cancelButton) {
            craftInvInstance.closeInventory()
        }
    }


    /**
     * Make sure the object is properly deleted
     */
    @EventHandler
    fun playerCloseCraftingInv(e: InventoryCloseEvent) {

        if (!CustomCraftingInventory.isCustomInventory(e.inventory)) return

        CustomCraftingInventory.getCustomCraftingInventory(e.inventory)?.closeInventory() ?: return
    }
}
