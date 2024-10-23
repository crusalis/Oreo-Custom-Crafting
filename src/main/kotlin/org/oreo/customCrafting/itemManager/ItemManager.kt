package org.oreo.siegebridge.itemManager

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

object ItemManager {
    private var plugin: JavaPlugin? = null
    var siegeBridge: ItemStack? = null

    /**
     * Item initialisation
     */
    fun init(pluginInstance: JavaPlugin?) {
        plugin = pluginInstance
        createBridge()
    }

    /**
     * Creates the item
     */
    private fun createBridge() {
        siegeBridge = createUniqueSiegeBridge()
    }

    /**
     * @return the item
     * Makes the siege ladder item , gives it the enchantment glow description and lore
     */
    fun createUniqueSiegeBridge(): ItemStack {
        val item = ItemStack(Material.GRINDSTONE, 1)
        val meta = item.itemMeta



        if (meta != null) {
            meta.setDisplayName("§eSiege Bridge")

            val lore: MutableList<String> = ArrayList()
            lore.add("§7Used cross enemy rivers and ganks")
            lore.add("§5\"Oreo antiGankInator5000\"") //The funni
            meta.lore = lore

            meta.addEnchant(Enchantment.LUCK, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS) //to add the enchant glint but not have it be visible

            // Add a unique identifier to make the item non-stackable
            val data = meta.persistentDataContainer
            val key = NamespacedKey(plugin!!, "unique_id")
            data.set(key, PersistentDataType.STRING, UUID.randomUUID().toString())

            item.setItemMeta(meta)
        }
        return item
    }
}