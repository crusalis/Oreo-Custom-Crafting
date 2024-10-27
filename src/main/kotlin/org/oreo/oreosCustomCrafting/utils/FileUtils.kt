package org.oreo.oreosCustomCrafting.utils

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import java.io.File

object FileUtils {


    /**
     * Saves the current item as a file
     */
    fun saveCustomItemAsFile(item: ItemStack, fileName : String, plugin: CustomCrafting) {
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

        // Create the file "test.json" inside the itemDir
        val newFile = File(directory, "$fileName.txt")
        try {
            // Write serialized item to the file
            newFile.writeText(itemSerialized)
            plugin.logger.info("Custom item file created successfully!")
        } catch (e: Exception) {
            plugin.logger.warning("Failed to write custom item to file: ${e.message}")
        }
    }

    /**
     * Gives all the saved items to the player
     * This is for debugging and should be removed
     */
    fun giveSavedItems(player: Player,plugin: CustomCrafting) {

        // Ensure itemDir is not null and contains files
        val files = plugin.itemDir?.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory || !file.canRead()) continue

            val serializedItem = file.readText()


            // Deserialize the ItemStack from bytes
            val item = SerializeUtils.deserializeItem(serializedItem)
            if (item != null) {
                player.inventory.addItem(item)
            }
        }
    }

    fun getCustomItem(itemName : String) : ItemStack {

        return CustomCrafting.customItems[itemName]
            ?: throw IllegalArgumentException("Item '$itemName' not found in custom items.")
    }


}