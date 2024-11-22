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
import org.oreo.oreosCustomCrafting.customIngredientListener.CustomIngredientListener
import org.oreo.oreosCustomCrafting.data.*
import org.oreo.oreosCustomCrafting.menus.customCrafting.CustomCraftingInventoryListener
import org.oreo.oreosCustomCrafting.menus.recipeGroupAssignmentMenu.RecipeGroupAssignmentMenuListener
import org.oreo.oreosCustomCrafting.menus.recipeGroupMenu.RecipeGroupMenuListener
import org.oreo.oreosCustomCrafting.menus.recipeMenu.RecipeMenuListener
import org.oreo.oreosCustomCrafting.menus.recipeShowOff.RecipeShowoffInventoryListener
import org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu.DisabledRecipeListener
import org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu.RecipeToggleMenuListener
import org.oreo.oreosCustomCrafting.utils.SerializeUtils
import org.oreo.oreosCustomCrafting.utils.Utils
import java.io.File
import java.io.FileReader


class CustomCrafting : JavaPlugin() {  //TODO organise the code
    //TODO make a button to add a recipe on the crafting group eventually

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

        if (config.getBoolean("clear-all-recipes")) {
            Bukkit.clearRecipes()
        }

        getCommand("crusalisCrafting")!!.setExecutor(CraftingCommand(this)) // Register a command

        registerSavedRecipes()

        server.pluginManager.registerEvents(CustomCraftingInventoryListener(), this)
        server.pluginManager.registerEvents(DisabledRecipeListener(), this)
        server.pluginManager.registerEvents(RecipeToggleMenuListener(), this)
        server.pluginManager.registerEvents(RecipeMenuListener(), this)
        server.pluginManager.registerEvents(RecipeGroupMenuListener(), this)
        server.pluginManager.registerEvents(RecipeGroupAssignmentMenuListener(), this)
        server.pluginManager.registerEvents(RecipeShowoffInventoryListener(), this)
        server.pluginManager.registerEvents(CustomIngredientListener(), this)

        saveDefaultConfig()
        //loadGroupsFromFile()
    }

    override fun onDisable() {
        saveDefaultConfig()
        //saveGroupsToFile(groups)
    }

    /**
     * Register and save the recipe as a file
     */
    fun registerAndSaveRecipe(
        recipe: ShapedRecipe,
        recipeName: String,
        customItemIngredients: List<String>
    ) { //Shaped recipes

        val recipeData = shapedRecipeToData(recipe, this, customItemIngredients)

        saveCustomIngredientRecipe(recipeData, recipe)

        allRecipesSaved.add(recipe)

        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeName))

        Bukkit.getServer().addRecipe(recipe)

        if (!itemDir?.exists()!!) {
            itemDir?.mkdirs()
        }
        val file = File(shapedRecipeDir, "$recipeName.json")

        customRecipes.add(
            CustomRecipeData(
                recipe = recipe,
                recipeData = recipeData,
            )
        )

        file.writeText(gson.toJson(recipeData))
    }

    // Shapeless recipes
    fun registerAndSaveRecipe(recipe: ShapelessRecipe, recipeName: String, customItemIngredients: List<String>) {

        val recipeData = shapeLessRecipeToData(recipe, this, customItemIngredients)

        allRecipesSaved.add(recipe)

        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeName))

        Bukkit.getServer().addRecipe(recipe)

        if (!itemDir?.exists()!!) {
            itemDir?.mkdirs()
        }
        val file = File(shapelessRecipeDir, "$recipeName.json")

        saveCustomIngredientRecipe(recipeData, recipe)

        customRecipes.add(
            CustomRecipeData(
                recipe = recipe,
                recipeData = recipeData,
            )
        )

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


                        saveCustomIngredientRecipe(recipeData, recipeFromData)

                        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeData.name))
                        Bukkit.getServer().addRecipe(recipeFromData)

                        customRecipes.add(
                            CustomRecipeData(
                                recipe = recipeFromData,
                                recipeData = recipeData,
                            )
                        )
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

                        saveCustomIngredientRecipe(recipeData, recipeFromData)

                        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft(recipeData.name))
                        Bukkit.getServer().addRecipe(recipeFromData)

                        customRecipes.add(
                            CustomRecipeData(
                                recipe = recipeFromData,
                                recipeData = recipeData,
                            )
                        )
                        allRecipesSaved.add(recipeFromData)

                        logger.info("Registered custom shapeless recipe ${recipeData.name} successfully")
                    }
                }
            } catch (e: JsonSyntaxException) {
                Bukkit.getLogger().warning("Failed to parse shapeless recipe in file ${file.name}: ${e.message}")
            }
        }
    }


    fun saveCustomIngredientRecipe(recipeData: ShapedRecipeData, recipe: Recipe) {
        if (recipeData.customIngredients != null && recipeData.customIngredients.isNotEmpty()) {
            val customItems: ArrayList<ItemStack> = arrayListOf()

            for (itemName in recipeData.customIngredients) {
                customItems.add(Utils.getCustomItem(itemName))
            }
            customIngredientRecipes.add(Pair(customItems.toList(), recipe.result))
        }
    }

    fun saveCustomIngredientRecipe(recipeData: ShapeLessRecipeData, recipe: Recipe) {
        if (recipeData.ingredientsItems != null && recipeData.ingredientsItems.isNotEmpty()) {
            val customItems: ArrayList<ItemStack> = arrayListOf()

            for (itemName in recipeData.ingredientsItems) {
                customItems.add(Utils.getCustomItem(itemName))
            }
            customIngredientRecipes.add(Pair(customItems.toList(), recipe.result))
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
    fun removeCraftingRecipe(name: String): Boolean {

        try {

            val key: NamespacedKey = NamespacedKey.fromString(name)!!

            Bukkit.getServer().removeRecipe(key)

            for (recipe in customRecipes) {

                val recipeData = recipe.recipeData

                if (recipeData is ShapedRecipeData) {
                    if (recipeData.name == name) {
                        customRecipes.remove(recipe)
                        break
                    }

                } else if (recipeData is ShapeLessRecipeData) {
                    if (recipeData.name == name) {
                        break
                    }
                }
            }

            for (file in shapedRecipeDir?.listFiles()!!) {
                if (file.name != "$name.json") continue

                file.delete()
                return true
            }

            for (file in shapelessRecipeDir?.listFiles()!!) {
                if (file.name != "$name.json") continue

                file.delete()
                return true
            }

            return false
        } catch (_: Exception) {
            return false
        }
    }

    // Function to save the hashmap to a file
//    fun saveGroupsToFile(groups: HashMap<String, Pair<ItemStack, ArrayList<CustomRecipeData>>>) {
//        TODO("Will do once I have the more important stuff done")
//    }
//
//
//    fun loadGroupsFromFile(): HashMap<String, Pair<ItemStack, ArrayList<CustomRecipeData>>> {
//        TODO("Will do once I have the more important stuff done")
//    }


    companion object {

        const val GROUP_FILE = "groups.json"

        val customIngredientRecipes: ArrayList<Pair<List<ItemStack>, ItemStack>> = arrayListOf()

        /**
         * A list of all custom items which is loaded from the save file
         */
        val customItems: HashMap<String, ItemStack> = hashMapOf()

        val customRecipes: MutableList<CustomRecipeData> = mutableListOf()

        val disabledRecipes: MutableList<Recipe> = mutableListOf()

        var groups: HashMap<String, Pair<ItemStack, ArrayList<CustomRecipeData>>> = hashMapOf() //TODO save this somehow

        /**
         * We save all recipes to our own list because since the vanilla ones are accessed via Iterator they have
        a different memory address every time and there's no such thing as .equals() for recipes
         */
        val allRecipesSaved: ArrayList<Recipe> = getAllRecipes()

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
