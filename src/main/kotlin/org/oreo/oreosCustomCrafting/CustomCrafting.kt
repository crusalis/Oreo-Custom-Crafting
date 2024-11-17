package org.oreo.oreosCustomCrafting

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.oreo.oreosCustomCrafting.commands.CraftingCommand
import org.oreo.oreosCustomCrafting.customException.WtfIsHappeningException
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.oreo.oreosCustomCrafting.data.ShapeLessRecipeData
import org.oreo.oreosCustomCrafting.data.ShapedRecipeData
import org.oreo.oreosCustomCrafting.data.dataToShapeLessRecipe
import org.oreo.oreosCustomCrafting.data.dataToShapedRecipe
import org.oreo.oreosCustomCrafting.data.shapeLessRecipeToData
import org.oreo.oreosCustomCrafting.data.shapedRecipeToData
import org.oreo.oreosCustomCrafting.menus.customCrafting.CustomCraftingInventoryListener
import org.oreo.oreosCustomCrafting.menus.recipeMenu.RecipeMenuListener
import org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu.DisabledRecipeListener
import org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu.RecipeToggleMenuListener
import org.oreo.oreosCustomCrafting.utils.SerializeUtils
import java.io.File
import java.io.FileReader


class CustomCrafting : JavaPlugin() {  //TODO organise the code

    private val gson = Gson()

    var itemDir: File? = null

    var craftingDir: File? = null

    var shapedRecipeDir: File? = null
    var shapelessRecipeDir: File? = null

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

        if (config.getBoolean("clear-all-recipes")){
            Bukkit.clearRecipes()
        }

        getCommand("crusalisCrafting")!!.setExecutor(CraftingCommand(this)) // Register a command

        registerSavedRecipes()

        server.pluginManager.registerEvents(CustomCraftingInventoryListener(), this)
        server.pluginManager.registerEvents(DisabledRecipeListener(), this)
        server.pluginManager.registerEvents(RecipeToggleMenuListener(), this)
        server.pluginManager.registerEvents(RecipeMenuListener(), this)

        saveDefaultConfig()

    }

    /**
     * Register and save the recipe as a file
     */
    fun registerAndSaveRecipe(recipe : ShapedRecipe, recipeName : String) { //Shaped recipes

        allRecipesSaved.add(recipe)

        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeName))

        Bukkit.getServer().addRecipe(recipe)

        if (!itemDir?.exists()!!) {
            itemDir?.mkdirs()
        }
        val file = File(shapedRecipeDir, "$recipeName.json")

        val recipeData = shapedRecipeToData(recipe, this)

        customRecipes.add(CustomRecipeData(
            recipe = recipe,
            recipeData = recipeData,
        ))

        file.writeText(gson.toJson(recipeData))
    }

    // Shapeless recipes
    fun registerAndSaveRecipe(recipe : ShapelessRecipe, recipeName : String){


        allRecipesSaved.add(recipe)

        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeName))

        Bukkit.getServer().addRecipe(recipe)

        if (!itemDir?.exists()!!) {
            itemDir?.mkdirs()
        }
        val file = File(shapelessRecipeDir, "$recipeName.json")

        val recipeData = shapeLessRecipeToData(recipe, this)

        customRecipes.add(CustomRecipeData(
            recipe = recipe,
            recipeData = recipeData,
        ))

        file.writeText(gson.toJson(recipeData))
    }

    /**
     * Registers all custom recipes saved
     */
    private fun registerSavedRecipes() {

        for (file in shapedRecipeDir?.listFiles()!!) {

            if (!file.isFile || !file.extension.equals("json", ignoreCase = true)) continue

            try {
                FileReader(file).use { reader ->
                    val recipeData = gson.fromJson(reader, ShapedRecipeData::class.java)
                    if (recipeData != null) {

                        val recipeFromData = dataToShapedRecipe(recipeData)

                        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeData.name))
                        Bukkit.getServer().addRecipe(recipeFromData)

                        customRecipes.add(CustomRecipeData(
                            recipe = recipeFromData,
                            recipeData = recipeData,
                        ))
                        allRecipesSaved.add(recipeFromData)

                        logger.info("Registered custom shaped recipe ${recipeData.name} successfully")
                    }
                }
            } catch (e: JsonSyntaxException) {
                Bukkit.getLogger().warning("Failed to parse shaped recipe in file ${file.name}: ${e.message}")
            }
        }


        for (file in shapelessRecipeDir?.listFiles()!!) {

            if (!file.isFile || !file.extension.equals("json", ignoreCase = true)) continue

            try {
                FileReader(file).use { reader ->
                    val recipeData = gson.fromJson(reader, ShapeLessRecipeData::class.java)
                    if (recipeData != null) {

                        val recipeFromData = dataToShapeLessRecipe(recipeData)

                        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeData.name))
                        Bukkit.getServer().addRecipe(recipeFromData)

                        customRecipes.add(CustomRecipeData(
                            recipe = recipeFromData,
                            recipeData = recipeData,
                        ))
                        allRecipesSaved.add(recipeFromData)

                        logger.info("Registered custom shapeless recipe ${recipeData.name} successfully")
                    }
                }
            } catch (e: JsonSyntaxException) {
                Bukkit.getLogger().warning("Failed to parse shapeless recipe in file ${file.name}: ${e.message}")
            }
        }
    }

    /**
     * Handles everything to do with custom directory creation and item loading from base64 files
     */
    private fun handleCustomItemDirectories() {

        craftingDir = createDataDirectory("Recipes", dataFolder)

        itemDir = createDataDirectory("Custom Items", dataFolder)

        if (craftingDir != null && craftingDir!!.exists()) {

            shapedRecipeDir = createDataDirectory("Shaped Recipes", craftingDir!!)
            shapelessRecipeDir = createDataDirectory("Shapeless Recipes", craftingDir!!)

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
                val itemName: String = file.name ?: continue
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

    /**
     * Tries to remove the recipe ith that nameSpaceKey
     * @return Weather it succeeded
     */
    fun removeCraftingRecipe(name: String) : Boolean {

        try {

            val key : NamespacedKey = NamespacedKey.fromString(name)!!

            Bukkit.getServer().removeRecipe(key)

            for (recipe in customRecipes) {

                val recipeData = recipe.recipeData

                if (recipeData is ShapedRecipeData) {
                    if (recipeData.name == name) {
                        customRecipes.remove(recipe)
                        break
                    }

                } else if (recipeData is ShapeLessRecipeData){
                    if (recipeData.name == name) {
                        customRecipes.remove(recipe)
                        break
                    }
                }
            }

            for (file in craftingDir?.listFiles()!!){
                if (file.name != "$name.json") continue

                file.delete()
                return true
            }

            return false
        } catch (_: Exception) {
            return false
        }

    }

    companion object {

        /**
         * A list of all custom items which is loaded from the save file
         */
        val customItems: HashMap<String, ItemStack> = hashMapOf()

        val customRecipes : MutableList<CustomRecipeData> = mutableListOf()

        val disabledRecipes : MutableList<Recipe> = mutableListOf()

        /**
         * We save all recipes to our own list because since the vanilla ones are accessed via Iterator they have
         a different memory address every time and there's no such thing as .equals() for recipes
         */
        val allRecipesSaved : ArrayList<Recipe> = getAllRecipes()

        private fun getAllRecipes(): ArrayList<Recipe> {
            val recipes = mutableListOf<Recipe>()

            // Iterate through all the registered recipes
            for (recipe in Bukkit.recipeIterator()) {
                recipes.add(recipe)
            }

            return recipes as ArrayList<Recipe>
        }
    }
}
