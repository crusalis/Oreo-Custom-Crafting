package org.oreo.oreosCustomCrafting.menus.recipeMenu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.Recipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeInventory(val player: Player,type: ViewType) {

    private val rows = 5
    private val columns = 9
    private val invSize = rows * columns
    private val craftingInvName = "Enable / Disable recipes"
    private val craftingInv = Bukkit.createInventory(null, invSize, craftingInvName)

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage : Int = 0

    val recipes : List<Recipe> = when (type){
        ViewType.ENABLED -> CustomCrafting.getAllRecipes()
        ViewType.DISABLED -> CustomCrafting.disabledRecipes
        ViewType.ALL -> (CustomCrafting.getAllRecipes() + CustomCrafting.disabledRecipes)
    }

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

        player.sendMessage("Loading page $page")

        currentPage = page

        craftingInv.clear() // Clear the inventory before loading the new page

        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        // Set the items for the current page
        var i = startIndex
        var recipeNumber = i
        while (i < endIndex) {
            val slot = i - startIndex // Calculate the slot within the current page
            val recipe : Recipe = recipes[recipeNumber]

            if (recipe.result.type == Material.AIR) {
                recipeNumber++
                continue
            }

            val itemResult = Utils.createGuiItem(item = recipe.result, name = "Enabled", prefix = "§l§a", recipe.result.itemMeta?.displayName)

            craftingInv.setItem(slot, itemResult)
            recipeNumber++
            i++ // Increment to the next recipe
        }


        // Set navigation items in the last row
        if (currentPage > 0){
            craftingInv.setItem(invSize - 9, Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "Previous", null))
        }

        if (!hasBlank()) {
                craftingInv.setItem(invSize - 1, Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, "Next", null))
        }
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

        if (name.contains("Next")){
            loadPage(currentPage + 1)
        } else if (name.contains("Previous")){
            loadPage(currentPage-1)
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

    private fun hasBlank() : Boolean {

        for (row in 0 until rows-1) {
            for (column in 0 until columns) {
                val item = craftingInv.getItem(row * column)
                if (item == null || item.type == Material.AIR) return true
            }
        }

        return false
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

enum class ViewType {
    ENABLED,
    DISABLED,
    ALL
}