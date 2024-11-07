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

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage : Int = 0

    init {
        loadPage(0)
        openInventory()
    }


    /**
     * Loads a specified page of recipes into the crafting inventory.
     *
     * @param page The page number to load (0-based).
     */
    fun loadPage(page: Int) {

        currentPage = page

        craftingInv.clear() // Clear the inventory before loading the new page

        val recipes = CustomCrafting.getAllRecipes()
        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        // Set the items for the current page
        for (i in startIndex until endIndex) {
            val slot = i - startIndex // Calculate the slot within the current page
            val recipe = recipes[i]

            craftingInv.setItem(
                slot,
                Utils.createGuiItem(
                    item = recipe.result,
                    name = "Enabled",
                    prefix = "§l§a",
                    recipe.result.itemMeta.displayName
                )
            )
        }

        // Set navigation items in the last row
        craftingInv.setItem(invSize - 9, Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "Previous", null))
        craftingInv.setItem(invSize - 1, Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "Next", null))
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


    fun handleClickedItem(slot : Int){

        val item = craftingInv.getItem(slot) ?: return

        val name = item.itemMeta?.displayName ?: return

        if (name.contains("Next")){ //TODO this no worky worky
            loadPage(currentPage++)
        } else if (name.contains("Previous")){
            loadPage(currentPage--)
        } else {

            if (name.contains("Enabled")){
                //CustomCrafting.disabledRecipes.add()
                craftingInv.setItem(slot, Utils.createGuiItem(item = item, name = "Disabled",
                    prefix = "§l§c",name))
            } else if (name.contains("Disabled")) {
                //CustomCrafting.disabledRecipes.remove()
                craftingInv.setItem(slot, Utils.createGuiItem(item = item, name = "Enabled",
                    prefix = "§l§a",name))
            }

        }

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