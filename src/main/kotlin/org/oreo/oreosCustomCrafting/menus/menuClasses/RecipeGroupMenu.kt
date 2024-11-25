package org.oreo.oreosCustomCrafting.menus.menuClasses

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.menus.AbstractInventoryMenu
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeGroupMenu(private val player: Player) : AbstractInventoryMenu(player){


    private val rows = 3
    private val columns = 9
    private val invSize = rows * columns
    private val recipeGroupMenuInvName = "Custom recipes"
    override val inventory = Bukkit.createInventory(null, invSize, recipeGroupMenuInvName)


    private val allRecipesButton = Utils.createGuiItem(Material.CRAFTING_TABLE, "§6§lAll Recipes", null)


    init {
        initialiseItems()
        openInventory()
    }

    /**
     * Set up all the items for the menu
     */
    private fun initialiseItems() {
        for (i in 0..inventory.size - 1) {
            inventory.setItem(i, blank)
        }

        inventory.setItem(0, allRecipesButton)

        val keys = CustomCrafting.groups.keys.toList() // Convert keys to a list for indexed access

        for (i in keys.indices) { // Iterate over valid indices
            val item: ItemStack? = CustomCrafting.groups[keys[i]]?.first // Get the first element of the Pair

            Utils.createGuiItem(item!!, "§l${keys[i]}", null)

            inventory.setItem(i + 1, item) // Use a fallback if item is null
        }

        inventory.setItem(22, closeItem)

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
    override fun closeInventory() {
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

        val clickedItem: ItemStack = inventory.getItem(slot) ?: return

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
}