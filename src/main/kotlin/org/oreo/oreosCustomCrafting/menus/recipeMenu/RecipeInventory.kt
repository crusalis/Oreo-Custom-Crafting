package org.oreo.oreosCustomCrafting.menus.recipeMenu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeInventory(val player: Player) {

    private val rows = 5
    private val columns = 9
    private val invSize = rows * columns
    private val craftingInvName = "Enable / Disable recipes"
    private val craftingInv = Bukkit.createInventory(null, invSize, craftingInvName)


    private var currentSlot = 0

    init {
        initializeMenuItems()
        openInventory()
    }


    /**
     * Initializes the crafting inventory items.
     */
    private fun initializeMenuItems() {

        for (recipe in CustomCrafting.getAllRecipes()){
            craftingInv.setItem(currentSlot,recipe.result)
            currentSlot++

            if(currentSlot == invSize - columns) break
        }

        craftingInv.setItem(invSize - 9,Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "Right", null))
        craftingInv.setItem(invSize - 1,Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "Left", null))
    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        val newInventory = craftingInv
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player and remove its references
     */
    fun closeInventory() {
        openInventories.remove(craftingInv)
        try {
            craftingInv.close()
        } catch (_: Exception){}
    }

    companion object {

        val openInventories = mutableMapOf<Inventory, RecipeInventory>()

        /**
         * Checks if the inventory is a custom crafting instance
         */
        fun isCustomInventory(inv: Inventory): Boolean {

            return openInventories.contains(inv)
        }

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getCustomCraftingInventory(inv: Inventory): RecipeInventory? {

            return openInventories[inv]
        }
    }

}