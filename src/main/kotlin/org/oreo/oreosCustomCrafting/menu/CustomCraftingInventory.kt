package org.oreo.oreosCustomCrafting.menu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils

class CustomCraftingInventory(player: Player, private val recipeName : String, private val plugin : CustomCrafting) {

    private val craftingInvName = "Create a custom recipe"
    private val craftingInv = Bukkit.createInventory(null, 9 * 6, craftingInvName)

    private val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)
    val acceptButton = Utils.createGuiItem(Material.GREEN_CONCRETE, "Save", null)
    val cancelButton = Utils.createGuiItem(Material.RED_CONCRETE, "Cancel", null)

    init {
        initializeMenuItems()
        openInventory(player)
    }

    /**
     * Initializes the crafting inventory items.
     */
    private fun initializeMenuItems() {
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
        craftingInv.setItem(24, ItemStack(Material.AIR)) // Empty square

        // Fill the remaining bottom row slots (48 to 53) with blank items
        for (i in 44..53) {
            craftingInv.setItem(i, blank)
        }

        // Set the buttons
        craftingInv.setItem(47, ItemStack(acceptButton)) // Save button
        craftingInv.setItem(51, ItemStack(cancelButton)) // Cancel button
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

    /**
     * Saves the recipe from the inventory into a file and registers it
     */
    fun saveRecipe(override : Boolean){

        val resultSlotItem : ItemStack = craftingInv.getItem(RESULT_SLOT) ?: throw NullPointerException()

        if (craftingInv.getItem(RESULT_SLOT) != null && Utils.isCustomItem(resultSlotItem)||
            !Utils.customItemExists(resultSlotItem)) {

            Utils.saveCustomItemAsFile(resultSlotItem, plugin)

        }


        val recipeMapping = handleStringConversion()

        val recipe = ShapedRecipe(NamespacedKey.minecraft(recipeName), resultSlotItem)
        recipe.shape(recipeMapping.first[0],
                     recipeMapping.first[1],
                     recipeMapping.first[2])

        for ((char, ingredient) in recipeMapping.second) {
            recipe.setIngredient(char, ingredient)
        }

        plugin.registerAndSaveRecipe(recipe,recipeName,override)
    }

    /**
     * Takes the items from the custom crafting inventory and converts them into a list of three strings
     for the actual creation of the recipe along with the corresponding characters
     */
    private fun handleStringConversion(): Pair<List<String>, Map<Char, Material>> {
        val items = ArrayList<ItemStack?>()
        val materialMap = mutableMapOf<Material, Char>()
        val charToMaterialMap = mutableMapOf<Char, Material>()
        var currentChar = 'A'

        // Fill the list with items
        for (slot in CRAFTING_SLOTS) {
            val item = craftingInv.getItem(slot)
            items.add(item)
        }

        // Create a list to hold the output strings
        val outputLines = mutableListOf<StringBuilder>()

        // Initialize three lines
        repeat(3) { outputLines.add(StringBuilder()) }

        for (item in items) {
            val char = when {
                item == null || item.type == Material.AIR -> ' '
                else -> materialMap.getOrPut(item.type) {
                    currentChar++.also { if (currentChar > 'Z') currentChar = 'A' } // Reset to 'A' if over 'Z'
                }
            }

            // Store the character and material in the inverted map
            if (char != ' ') {
                if (item != null) {
                    charToMaterialMap[char] = item.type
                }
            }

            // Append the character to the corresponding line
            for (line in outputLines) {
                if (line.length < 3) {
                    line.append(char)
                    break
                }
            }
        }

        // Ensure each line is exactly 3 characters long by padding with spaces
        for (line in outputLines) {
            while (line.length < 3) {
                line.append(' ')
            }
        }

        val outputStrings = outputLines.map { it.toString() }
        return Pair(outputStrings, charToMaterialMap)
    }




    companion object {
        const val RESULT_SLOT = 24

        val CRAFTING_SLOTS = listOf(11,12,13,20,21,22,29,30,31)

        val openInventories = mutableMapOf<Inventory, CustomCraftingInventory>()

        /**
         * Checks if the inventory is a custom crafting instance
         */
        fun isCustomInventory(inv : Inventory): Boolean {
            return openInventories.contains(inv)
        }

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getCustomCraftingInventory(inv : Inventory): CustomCraftingInventory? {

            return openInventories[inv]
        }
    }
}
