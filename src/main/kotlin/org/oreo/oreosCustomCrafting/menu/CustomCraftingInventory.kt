package org.oreo.oreosCustomCrafting.menu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.utils.Utils

class CustomCraftingInventory(player: Player) {

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
     * Opens the custom crafting inventory for a player.
     */
    private fun openInventory(player: Player) {
        val newInventory = craftingInv
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player.
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
    fun saveRecipe(){
        val usedMaterials = HashMap<Material, Char>()

        for (slot in CRAFTING_SLOTS){

        }
    }

    companion object {
        const val RESULT_SLOT = 24

        val CRAFTING_SLOTS = listOf(11,12,13,20,21,22,29,30,31)

        val openInventories = mutableMapOf<Inventory, CustomCraftingInventory>()
        
        fun isCustomInventory(inv : Inventory): Boolean {
            return openInventories.contains(inv)
        }

        fun getCustomCraftingInventory(inv : Inventory): CustomCraftingInventory? {

            return openInventories[inv]
        }
    }
}
