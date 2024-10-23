package org.oreo.customCrafting.data

import org.bukkit.Material
import org.oreo.customCrafting.data.itemMeta.ItemMetaData

/**
 * We store ItemStack in a data class to facilitate encoding it into JSON
 */
data class ItemStackData(
    val itemMeta: ItemMetaData,
    val count: Int,
    val material : Material,
)