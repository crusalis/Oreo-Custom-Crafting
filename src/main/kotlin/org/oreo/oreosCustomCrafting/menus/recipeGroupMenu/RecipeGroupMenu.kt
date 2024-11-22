package org.oreo.oreosCustomCrafting.menus.recipeGroupMenu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.menus.recipeMenu.RecipeMenu
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeGroupMenu(val player: Player) {


    private val rows = 3
    private val columns = 9
    private val invSize = rows * columns
    private val recipeGroupMenuInvName = "Custom recipes"
    private val recipeGroupMenuInv = Bukkit.createInventory(null, invSize, recipeGroupMenuInvName)

    private val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)
    private val allRecipesButton = Utils.createGuiItem(Material.CRAFTING_TABLE, "§6§lAll Recipes", null)
    private val closeItem = Utils.createGuiItem(Material.BARRIER, "Close", null)

    init {
        initialiseItems()
        openInventory()
    }

    fun initialiseItems() {
        for (i in 0..recipeGroupMenuInv.size - 1) {
            recipeGroupMenuInv.setItem(i, blank)
        }

        recipeGroupMenuInv.setItem(0, allRecipesButton)

        val keys = CustomCrafting.groups.keys.toList() // Convert keys to a list for indexed access

        for (i in keys.indices) { // Iterate over valid indices
            val item: ItemStack? = CustomCrafting.groups[keys[i]]?.first // Get the first element of the Pair

            Utils.createGuiItem(item!!, "§l${keys[i]}", null)

            recipeGroupMenuInv.setItem(i + 1, item) // Use a fallback if item is null
        }

        recipeGroupMenuInv.setItem(22, closeItem)

    }


    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        val newInventory = recipeGroupMenuInv
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player and remove its references
     */
    fun closeInventory() {
        openInventories.remove(recipeGroupMenuInv)
        try {
            recipeGroupMenuInv.close()
        } catch (_: Exception) {
        }
    }

    /**
     * Handle any item being clicked
     */
    fun handleClickedItem(slot: Int) {

        val clickedItem: ItemStack = recipeGroupMenuInv.getItem(slot) ?: return

        if (clickedItem == closeItem) {
            closeInventory()
            return
        }

        when (slot) {

            0 -> RecipeMenu(player, null)

            else -> {

                val keys = CustomCrafting.groups.keys.toList()

                val groupName = keys[slot - 1]

                RecipeMenu(player, groupName)
            }
        }
    }


    companion object {

        val openInventories = mutableMapOf<Inventory, RecipeGroupMenu>()

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getInstance(inv: Inventory): RecipeGroupMenu? {
            return openInventories[inv]
        }

    }
}