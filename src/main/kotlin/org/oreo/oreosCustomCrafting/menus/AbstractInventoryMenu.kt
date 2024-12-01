package org.oreo.oreosCustomCrafting.menus

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.oreo.oreosCustomCrafting.utils.Utils

abstract class AbstractInventoryMenu(private val player : Player) {

    val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)
    val closeItem = Utils.createGuiItem(Material.BARRIER, "§lClose", null)

    val nextItem = Utils.createGuiItem(Material.WARPED_SIGN, "Next", null)
    val previousItem = Utils.createGuiItem(Material.CRIMSON_SIGN, "Previous", null)

    abstract val invSize : Int

    abstract val inventory: Inventory

    abstract fun handleClickedItem(slot: Int)

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    fun addToList() { //Please change this
        openInventories[inventory] = this
    }

    /**
     * Checks if the inventory has a blank space
     */
    private fun hasBlank(): Boolean {
        val bottomRowStart = inventory.size - 9
        for (slot in 0 until bottomRowStart) {
            val item = inventory.getItem(slot)
            if (item == null || item.type == Material.AIR) return true
        }
        return false
    }

    fun setNextAndPrevItem(currentPage: Int) {

        if (currentPage > 0) {
            inventory.setItem(invSize - 7, Utils.createGuiItem(Material.CRIMSON_SIGN, "Previous", null))
        }
        if (!hasBlank()) {
            inventory.setItem(invSize - 3, Utils.createGuiItem(Material.WARPED_SIGN, "Next", null))
        }

    }

    open fun  closeInventory() {}

    companion object {
        val openInventories = mutableMapOf<Inventory, AbstractInventoryMenu>()

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getCustomInventory(inv: Inventory): AbstractInventoryMenu? {
            return openInventories[inv]
        }
    }
}