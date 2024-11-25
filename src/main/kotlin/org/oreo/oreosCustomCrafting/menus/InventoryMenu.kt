package org.oreo.oreosCustomCrafting.menus

import org.bukkit.Material
import org.bukkit.entity.Player
import org.oreo.oreosCustomCrafting.utils.Utils

open class InventoryMenu(private val player : Player) {

    val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)
    val closeItem = Utils.createGuiItem(Material.BARRIER, "§lClose", null)

}