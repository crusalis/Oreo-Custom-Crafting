package org.oreo.oreosCustomCrafting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.oreo.oreosCustomCrafting.commands.TestCommand
import java.io.File

class CustomCrafting : JavaPlugin() {

    private val gson = Gson()
    var itemDir: File? = null

    /**
     * Handle all the directories and recipes on load
     */
    override fun onLoad() {
        handleDirectoriesCustomItems()
    }

    override fun onEnable() {

        getCommand("saveItem")!!.setExecutor(TestCommand(this)) // Register a command
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun craftSiegeBridge() {

        val sr: ShapedRecipe = ShapedRecipe(NamespacedKey.minecraft("siege bridge"), ItemStack(Material.STONE))
        sr.shape(
            "YXY",
            "X T",
            "TXX"
        )
        sr.setIngredient('Y', Material.YELLOW_DYE)
        sr.setIngredient('X', Material.JUKEBOX)
        sr.setIngredient('T', Material.TUFF)
        Bukkit.getServer().addRecipe(sr)

    }

    private fun registerCustomRecipes() {

        for (file in dataFolder.listFiles()!!) {

            if (!file.isFile) continue


        }

    }

    /**
     * Handles everything to do with custom directory creation and item loading from base64 files
     */
    private fun handleDirectoriesCustomItems() {

        val craftingDir = createDataDirectory("Recipes", dataFolder)

        if (craftingDir != null && craftingDir.exists()) {

            itemDir = createDataDirectory("Custom Items", craftingDir)

            if (itemDir != null) {
                initialiseCustomItems(itemDir!!, mutableListOf())
            } else {
                logger.warning("Failed to load custom Items")
            }
        } else {
            logger.warning("Failed to create custom Recipes directory")
        }

    }

    private fun initialiseCustomItems(itemDir: File, customItems: MutableList<ItemStack>) {
        // Initialize Gson
        val gson = Gson()

        // Ensure the itemDir contains files
        val files = itemDir.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory || !file.canRead()) continue

            val serializedItem = file.readText()

            try {
                // Deserialize the ItemStack from JSON
                gson.fromJson(serializedItem, ItemStack::class.java)?.let {
                    customItems.add(it)
                }
            } catch (e: Exception) {
                logger.warning("Failed to deserialize item from file ${file.name}: ${e.message}")
            }
        }
    }

    /**
     * Creates a directory where its needed
     * @return the directory created is returned if it already existed it returns the file in that path
    and null if it failed
     */
    private fun createDataDirectory(directoryName: String, dataFolder: File): File? {

        // Create a new directory within the data folder
        val newDir = File(dataFolder, directoryName)

        // Check if the directory exists, if not, create it
        if (!newDir.exists()) {
            val created = newDir.mkdirs()
            if (created) {
                logger.info("Custom crafting directory created: ${newDir.absolutePath}")
                return newDir
            } else {
                logger.warning("Failed to create custom crafting directory: ${newDir.absolutePath}")
                return null
            }
        } else {
            logger.info("Custom crafting directory already exists: ${newDir.absolutePath}")
            return File(newDir.path)
        }
    }


    fun saveCustomItemAsFile(item: ItemStack, fileName : String) {
        // Initialize Gson with pretty printing for better readability

        // Serialize the ItemStack
        val itemSerialized : String = SerializeUtils.serializeItem(item)

        // Ensure itemDir is not null and directory exists
        if (itemDir == null) {
            logger.warning("Item directory is not set!")
            return
        }

        val directory = File(itemDir!!.path)
        if (!directory.exists()) {
            directory.mkdirs()  // Create directories if they don't exist
        }

        // Create the file "test.json" inside the itemDir
        val newFile = File(directory, "$fileName.txt")
        try {
            // Write serialized item to the file
            newFile.writeText(itemSerialized)
            logger.info("Custom item file created successfully!")
        } catch (e: Exception) {
            logger.warning("Failed to write custom item to file: ${e.message}")
        }
    }


    fun giveSavedItems(player: Player) {
        // Initialize Gson

        // Ensure itemDir is not null and contains files
        val files = itemDir?.listFiles() ?: return

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


    companion object {

        /**
         * A list of all custom items which is loaded from the save file
         */
        val customItems: MutableList<ItemStack> = mutableListOf()
    }

}
