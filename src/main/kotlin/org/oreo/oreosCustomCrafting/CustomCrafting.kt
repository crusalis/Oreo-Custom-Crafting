package org.oreo.oreosCustomCrafting

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.oreo.oreosCustomCrafting.commands.TestCommand
import org.oreo.oreosCustomCrafting.data.ShapedRecipeData
import org.oreo.oreosCustomCrafting.data.dataToShapedRecipe
import org.oreo.oreosCustomCrafting.data.shapedRecipeToData
import org.oreo.oreosCustomCrafting.utils.SerializeUtils
import java.io.File
import java.io.FileReader

class CustomCrafting : JavaPlugin() {

    private val gson = Gson()
    var itemDir: File? = null

    private var craftingDir : File? = null

    /**
     * Handle all the directories and recipes on load
     */
    override fun onLoad() {
        handleCustomItemDirectories()

        if (itemDir != null) {
            initialiseCustomItems(itemDir!!)
        } else {
            logger.warning("Failed to load custom Items")
        }
    }

    override fun onEnable() {

        getCommand("oreosCrafting")!!.setExecutor(TestCommand(this)) // Register a command

        registerSavedRecipes()

        craftSiegeBridge()
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Placeholder
     */
    private fun craftSiegeBridge() {

        val sr: ShapedRecipe = ShapedRecipe(NamespacedKey.minecraft("siege-bridge"), ItemStack(Material.STONE))
        sr.shape(
            "YXY",
            "X T",
            "TXX"
        )
        sr.setIngredient('Y', Material.YELLOW_DYE)
        sr.setIngredient('X', Material.JUKEBOX)
        sr.setIngredient('T', Material.TUFF)

        if (!itemDir?.exists()!!) {
            itemDir?.mkdirs()
        }
        val file = File(craftingDir, "recipe.json")
        file.writeText(gson.toJson(shapedRecipeToData(sr)))
    }

    /**
     * Registers all custom recipes saved in the file
     */
    private fun registerSavedRecipes() {

        for (file in craftingDir?.listFiles()!!) {

            if (!file.isFile || !file.extension.equals("json", ignoreCase = true)) continue

            try {
                FileReader(file).use { reader ->
                    val recipeData = gson.fromJson(reader, ShapedRecipeData::class.java)
                    if (recipeData != null) {
                        Bukkit.getServer().addRecipe(dataToShapedRecipe(recipeData))
                        logger.info("Registered custom recipe ${recipeData.name} successfully")
                    }
                }
            } catch (e: JsonSyntaxException) {
                Bukkit.getLogger().warning("Failed to parse recipe in file ${file.name}: ${e.message}")
            }
        }
    }

    /**
     * Handles everything to do with custom directory creation and item loading from base64 files
     */
    private fun handleCustomItemDirectories() {

        craftingDir = createDataDirectory("Recipes", dataFolder)

        if (craftingDir != null && craftingDir!!.exists()) {

            itemDir = createDataDirectory("Custom Items", craftingDir!!)

        } else {
            logger.warning("Failed to create custom Recipes directory")
        }

    }

    /**
     * Initialises all custom items and add them to the custom items list
     */
    private fun initialiseCustomItems(itemDir: File) {

        // Ensure the itemDir contains files
        val files = itemDir.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory || !file.canRead()) continue

            val serializedItem = file.readText()

            try {
                // Add the custom item to the custom Items list
                val itemName : String = file.name ?: continue
                val customItem = SerializeUtils.deserializeItem(serializedItem) ?: continue

                customItems[itemName] = customItem

            } catch (e: Exception) {
                logger.warning("Failed to deserialize item from file ${file.name}: ${e.message}")
            }
        }
    }

    /**
     * Creates a directory where its needed
     * @return The directory created is returned if it already existed it returns the file in that path
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

    companion object {

        /**
         * A list of all custom items which is loaded from the save file
         */
        val customItems: HashMap<String,ItemStack> = hashMapOf()
    }

}
