package org.oreo.oreosCustomCrafting.menus.recipeShowOff

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.oreo.oreosCustomCrafting.data.ShapeLessRecipeData
import org.oreo.oreosCustomCrafting.data.ShapedRecipeData
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeShowoffInventory(val player: Player, private val recipe : CustomRecipeData) {

    private val name = recipe.recipeData.name

    private val craftingInvName = name
    private val craftingInv = Bukkit.createInventory(null, 9 * 6, craftingInvName)

    private val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)



    init {
        initializeMenuItems()
        openInventory(player)
    }

    /**
     * Initializes the crafting inventory items.
     */
    private fun initializeMenuItems() {

        val realRecipe = try {
            recipe.recipeData as ShapedRecipeData
        } catch (_: Exception) {
            recipe.recipeData as ShapeLessRecipeData
        }

        // Fill all slots with blank items first
        for (i in 0..53) {
            craftingInv.setItem(i, blank)
        }

        // Create a 3x3 square in the middle (slots 20 to 28)
        for (row in -1..1) {
            for (col in 0..2) {
                craftingInv.setItem(20 + (row * 9) + col, ItemStack(Material.AIR))
            }
        }

        // Leave one empty square (slot 24)
        craftingInv.setItem(RESULT_SLOT, recipe.recipe.result) // Empty square

        // Fill the remaining bottom row slots (48 to 53) with blank items
        for (i in 44..53) {
            craftingInv.setItem(i, blank)
        }

    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory(player: Player) {
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
        const val RESULT_SLOT = 24

        val CRAFTING_SLOTS = listOf(11,12,13,20,21,22,29,30,31)

        val openInventories = mutableMapOf<Inventory, RecipeShowoffInventory>()

        /**
         * Checks if the inventory is a custom crafting instance
         */
        fun isCustomInventory(inv : Inventory): Boolean {
            return openInventories.contains(inv)
        }

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getCustomCraftingInventory(inv : Inventory): RecipeShowoffInventory? {

            return openInventories[inv]
        }
    }
}
