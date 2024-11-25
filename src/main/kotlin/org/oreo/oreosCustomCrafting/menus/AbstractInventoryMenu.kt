package org.oreo.oreosCustomCrafting.menus

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.oreo.oreosCustomCrafting.utils.Utils

abstract class AbstractInventoryMenu(private val player : Player) {

    val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)
    val closeItem = Utils.createGuiItem(Material.BARRIER, "§lClose", null)

    abstract val inventory: Inventory

    abstract fun handleClickedItem(slot: Int)

    abstract fun closeInventory()

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        player.openInventory(inventory)
        openInventories[inventory] = this
    }

    /**
     * Checks if the inventory has a blank space
     */
    fun hasBlank(): Boolean {
        for (slot in 0 until inventory.size) {
            val item = inventory.getItem(slot)
            if (item == null || item.type == Material.AIR) return true
        }
        return false
    }

    init {
        openInventory()
    }

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