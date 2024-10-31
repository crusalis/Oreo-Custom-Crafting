package org.oreo.oreosCustomCrafting.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory

object InvUtils { //TODO make into a class and initialise listeners only while it exists

    private const val craftingInvName = "Create a custom recipe"
    val craftingInv = Bukkit.createInventory(null, 9 * 6, craftingInvName);

    fun initializeMenuItems(){
        val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE," ", null)

        for (i in 0..8){
            craftingInv.setItem(i,blank)
        }
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