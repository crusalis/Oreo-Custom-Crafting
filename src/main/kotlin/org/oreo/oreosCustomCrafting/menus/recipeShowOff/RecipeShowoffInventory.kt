package org.oreo.oreosCustomCrafting.menus.recipeShowOff

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.oreo.oreosCustomCrafting.data.ShapeLessRecipeData
import org.oreo.oreosCustomCrafting.data.ShapedRecipeData
import org.oreo.oreosCustomCrafting.menus.InventoryMenu
import org.oreo.oreosCustomCrafting.utils.MenuUtils

class RecipeShowoffInventory(private val player: Player, private val recipe: CustomRecipeData): InventoryMenu(player) {

    private val name = recipe.recipeData.name

    private val craftingInvName = name
    private val craftingInv = Bukkit.createInventory(null, 9 * 6, craftingInvName)


    init {
        initializeMenuItems()
        openInventory(player)
    }

    /**
     * Initializes the crafting inventory items.
     */
    private fun initializeMenuItems() {

        val recipeData = recipe.recipeData

        // Fill all slots with blank items first
        for (i in 0..53) {
            craftingInv.setItem(i, blank)
        }

        // Fill the result slots
        if (recipeData is ShapedRecipeData) {
            var slotIndex = 0

            // Loop through each row in the recipe
            for (row in recipeData.rows) {
                // Loop through each character in the row
                for (char in row) {
                    if (char == ' ') {
                        // Empty slot in the crafting grid
                        craftingInv.setItem(CRAFTING_SLOTS[slotIndex], ItemStack(Material.AIR))
                    } else {
                        // Get the material corresponding to the character from ingredients map
                        val material = recipeData.ingredients[char]
                        if (material != null) {
                            craftingInv.setItem(CRAFTING_SLOTS[slotIndex], ItemStack(material))
                        } else {
                            // If no material is found, set the slot to AIR
                            craftingInv.setItem(CRAFTING_SLOTS[slotIndex], ItemStack(Material.AIR))
                        }
                    }
                    slotIndex++
                }
            }
        } else if (recipeData is ShapeLessRecipeData) {

            for (slot in CRAFTING_SLOTS) {
                craftingInv.setItem(slot, ItemStack(Material.AIR))
            }

            for (slot in CRAFTING_SLOTS) {

                try {
                    val item = ItemStack(recipeData.ingredientsMaterials[CRAFTING_SLOTS.indexOf(slot)])
                    craftingInv.setItem(slot, item)

                } catch (_: IndexOutOfBoundsException) {
                    break
                }
            }

        } else {
            player.sendMessage("${ChatColor.RED}RecipeData is of unknown type")
            throw IllegalArgumentException("RecipeData is of unknown type")
            player.closeInventory()
        }

        // Result slot
        craftingInv.setItem(RESULT_SLOT, recipe.recipe.result)

        // Fill the remaining bottom row slots (48 to 53) with blank items
        for (i in 44..53) {
            craftingInv.setItem(i, blank)
        }

        craftingInv.setItem(49, closeItem)
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
        } catch (_: Exception) {
        }
    }


    fun handleClickedItem(slot : Int){

        val clickedItem = craftingInv.getItem(slot) ?: return

        if (clickedItem == closeItem){
            closeInventory()
        }
    }


    companion object {
        //The slot for the resulting item
        const val RESULT_SLOT = 24

        //All the items for the crafting inventory
        val CRAFTING_SLOTS = listOf(11, 12, 13, 20, 21, 22, 29, 30, 31)

        val openInventories = mutableMapOf<Inventory, RecipeShowoffInventory>()

        /**
         * Checks if the inventory is a custom crafting instance
         */
        fun isCustomInventory(inv: Inventory): Boolean {
            return openInventories.contains(inv)
        }

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getCustomCraftingInventory(inv: Inventory): RecipeShowoffInventory? {

            return openInventories[inv]
        }
    }
}
