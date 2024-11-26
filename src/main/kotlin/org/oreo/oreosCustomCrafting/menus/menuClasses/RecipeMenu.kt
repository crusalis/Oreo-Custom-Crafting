package org.oreo.oreosCustomCrafting.menus.menuClasses

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.oreo.oreosCustomCrafting.menus.AbstractInventoryMenu
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeMenu(private val player: Player, group: String?) : AbstractInventoryMenu(player) {

    private val rows = 5
    private val columns = 9
    override val invSize = rows * columns
    private val recipeMenuInvName = "Recipe settings"
    override val inventory = Bukkit.createInventory(null, invSize, recipeMenuInvName)

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage: Int = 0

    private val slotToRecipeData = hashMapOf<Int, CustomRecipeData>()

    /**
     * The recipes that will be used for this menu are pre-determined here
     */
    private val recipes: List<CustomRecipeData> = if (group == null) {
        CustomCrafting.customRecipes.filterNot { it.recipe in CustomCrafting.disabledRecipes }
    } else {
        CustomCrafting.groups[group]?.second ?: throw IllegalArgumentException("Invalid group name")
    }


    init {
        addToList()
        loadPage(0)
        openInventory()
    }


    /**
     * Loads a specified page of recipes into the crafting inventory.
     * @param page The page number to load (0-based).
     */
    fun loadPage(page: Int) {

        if (page < 0) throw IllegalArgumentException("Page can't be negative")

        for (slot in (rows - 1) * columns..invSize - 1) {
            inventory.setItem(slot, blank)
        }

        currentPage = page
        inventory.clear() // Clear the inventory before loading the new page

        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        slotToRecipeData.clear()

        var i = startIndex
        var recipeNumber = i
        while (i < endIndex) {
            val slot = i - startIndex
            val recipe = recipes[recipeNumber].recipeData

            val itemResult: ItemStack = if (recipe.fileResult != null) {

                CustomCrafting.customItems[recipe.fileResult]!!

            } else {
                ItemStack(recipe.materialResult!!)
            }

            slotToRecipeData.put(slot, recipes[recipeNumber])

            val itemName = recipe.name

            val itemToAdd = Utils.createGuiItem(itemResult, itemName, null)

            inventory.setItem(slot, itemToAdd)
            recipeNumber++
            i++
        }

        // Set navigation items in the last row
        setNextAndPrevItem(currentPage)
    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        val newInventory = inventory
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player and remove its references
     */
    private fun closeInventory() {
        openInventories.remove(inventory)
        try {
            inventory.close()
        } catch (_: Exception) {
        }
    }

    /**
     * Handle any item being clicked
     */
    override fun handleClickedItem(slot: Int) {

        val item = inventory.getItem(slot) ?: return

        val name = item.itemMeta?.displayName ?: return

        if (slotToRecipeData.containsKey(slot)) {
            RecipeShowoffInventory(player, slotToRecipeData.get(slot)!!)
            closeInventory()
            return
        }

        if (name.contains("Next")) {
            loadPage(currentPage + 1)
        } else if (name.contains("Previous")) {
            loadPage(currentPage - 1)
        }
    }
}