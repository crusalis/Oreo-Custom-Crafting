package org.oreo.oreosCustomCrafting.menus.recipeMenu

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.data.ShapeLessRecipeData
import org.oreo.oreosCustomCrafting.data.ShapedRecipeData
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeMenu (val player: Player) {

    private val rows = 5
    private val columns = 9
    private val invSize = rows * columns
    private val recipeMenuInvName = "Recipe settings"
    private val recipeMenuInv = Bukkit.createInventory(null, invSize, recipeMenuInvName)

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage : Int = 0

    private val recipes = CustomCrafting.customRecipes.map {it.recipeData}


    init {
        loadPage(0)
        openInventory()
    }


    /**
     * Loads a specified page of recipes into the crafting inventory.
     * @param page The page number to load (0-based).
     */
    fun loadPage(page: Int) {

        if (page < 0) throw IllegalArgumentException("Page can't be negative")

        currentPage = page
        recipeMenuInv.clear() // Clear the inventory before loading the new page

        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        var i = startIndex
        var recipeNumber = i
        while (i < endIndex) {
            val slot = i - startIndex
            val recipe = recipes[recipeNumber]

            val itemResult : ItemStack = if (recipe is ShapedRecipeData) {

                if (recipe.fileResult != null){

                    CustomCrafting.customItems.get(recipe.fileResult)!!

                } else {
                    ItemStack(recipe.materialResult!!)
                }

            } else if (recipe is ShapeLessRecipeData) {

                if (recipe.fileResult != null){

                    CustomCrafting.customItems.get(recipe.fileResult)!!

                } else {
                    ItemStack(recipe.materialResult!!)
                }

            } else {
                closeInventory()
                player.sendMessage("${ChatColor.RED}ERROR item is not of correct type")
                throw IllegalArgumentException("Unexpected object type withing RecipeMenu instance 'recipes' list")
            }

            recipeMenuInv.setItem(slot, itemResult)
            recipeNumber++
            i++
        }

        // Set navigation items in the last row
        if (currentPage > 0) {
            recipeMenuInv.setItem(invSize - 7, Utils.createGuiItem(Material.CRIMSON_SIGN, "Previous", null))
        }
        if (!hasBlank()) {
            recipeMenuInv.setItem(invSize - 3, Utils.createGuiItem(Material.WARPED_SIGN, "Next", null))
        }
    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        val newInventory = recipeMenuInv
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player and remove its references
     */
    fun closeInventory() {
        openInventories.remove(recipeMenuInv)
        try {
            recipeMenuInv.close()
        } catch (_: Exception){}
    }

    /**
     * Handle any item being clicked
     */
    fun handleClickedItem(slot : Int){ //TODO this

        val item = recipeMenuInv.getItem(slot) ?: return

        val name = item.itemMeta?.displayName ?: return

        if (name.contains("Next")){
            loadPage(currentPage + 1)
        } else if (name.contains("Previous")) {
            loadPage(currentPage - 1)
        }

    }

    /**
     * Checks if the inventory has a blank space
     */
    private fun hasBlank() : Boolean {

        for (row in 0 until rows /*-1*/) {
            for (column in 0 until columns) {
                val item = recipeMenuInv.getItem(row * column)
                if (item == null || item.type == Material.AIR) return true
            }
        }

        return false
    }



    companion object {

        val openInventories = mutableMapOf<Inventory, RecipeMenu>()

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getInstance(inv: Inventory): RecipeMenu? {
            return openInventories[inv]
        }
    }

}