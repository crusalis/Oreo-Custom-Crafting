package org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.PlayerInventory

class RecipeInventoryListener : Listener {

    @EventHandler
    fun playerClickItem(e: InventoryClickEvent) {

        // Check if the inventory is a custom crafting inventory
        if (!RecipeInventory.isCustomInventory(e.inventory)) return
        //Let the player click in his own inventory
        if (e.clickedInventory is PlayerInventory) return

        e.isCancelled = true

        val recipeMenu = RecipeInventory.getCustomCraftingInventory(e.inventory)

        recipeMenu?.handleClickedItem(e.slot)
    }


    /**
     * Make sure the object is properly deleted
     */
    @EventHandler
    fun playerCloseCraftingInv(e: InventoryCloseEvent) {

        if (!RecipeInventory.isCustomInventory(e.inventory)) return

        RecipeInventory.getCustomCraftingInventory(e.inventory)?.closeInventory() ?: return
    }

}