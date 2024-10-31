package org.oreo.oreosCustomCrafting.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object InvUtils { //TODO make into a class and initialise listeners only while it exists

    private const val craftingInvName = "Create a custom recipe"
    private val craftingInv = Bukkit.createInventory(null, 9 * 6, craftingInvName);

    private val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)
    val acceptButton = Utils.createGuiItem(Material.GREEN_CONCRETE, "Save", null)
    val cancelButton = Utils.createGuiItem(Material.RED_CONCRETE, "Cancel", null)


    /**
     * The inventory filling isn't exactly optimised but uts more comprehensive
     */
    fun initializeMenuItems() {

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
     * Gets a copy of the custom crafting inventory
     */
    fun getCustomCraftingInv() : Inventory {
        val newInventory = Bukkit.createInventory(null, craftingInv.size, craftingInvName)
        for (i in 0 until craftingInv.size) {
            newInventory.setItem(i, craftingInv.getItem(i))
        }
        return newInventory
    }


}