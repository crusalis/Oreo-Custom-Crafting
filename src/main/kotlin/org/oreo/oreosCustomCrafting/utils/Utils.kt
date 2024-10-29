package org.oreo.oreosCustomCrafting.utils

import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import java.io.File

object Utils {


    /**
     * Saves the current item as a file
     */
    fun saveCustomItemAsFile(item: ItemStack, plugin: CustomCrafting) {
        // Initialize Gson with pretty printing for better readability

        // Serialize the ItemStack
        val itemSerialized : String = SerializeUtils.serializeItem(item)

        // Ensure itemDir is not null and directory exists
        if (plugin.itemDir == null) {
            plugin.logger.warning("Item directory is not set!")
            return
        }

        val directory = File(plugin.itemDir!!.path)
        if (!directory.exists()) {
            directory.mkdirs()  // Create directories if they don't exist
        }

        //Naming the file with a flat number for now
        val fileName : Int = plugin.itemDir!!.listFiles()!!.size - 1
        // Create the file
        val newFile = File(directory, "$fileName.txt")
        try {
            // Write serialized item to the file
            newFile.writeText(itemSerialized)

            CustomCrafting.customItems["$fileName.txt"] = item

            plugin.logger.info("Custom item file created successfully!")
        } catch (e: Exception) {
            plugin.logger.warning("Failed to write custom item to file: ${e.message}")
        }
    }

    /**
     * Searches through the list and gets an item with the corresponding name
     */
    fun getCustomItem(itemName : String) : ItemStack {

        return CustomCrafting.customItems[itemName]
            ?: throw IllegalArgumentException("Item '$itemName' not found in custom items.")
    }


    /**
     * Checks if the item has any custom properties
     */
    fun isCustomItem(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        return meta.hasEnchants() || meta.hasLore() || meta.hasDisplayName()
    }

    /**
     * Checks if the item exists already
     */
    fun customItemExists(customItem: ItemStack): Boolean {
        return CustomCrafting.customItems.values.contains(customItem)
    }

}