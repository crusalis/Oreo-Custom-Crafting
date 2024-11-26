package org.oreo.oreosCustomCrafting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.oreo.oreosCustomCrafting.commands.CraftingCommand
import org.oreo.oreosCustomCrafting.customIngredientListener.CustomIngredientListener
import org.oreo.oreosCustomCrafting.data.*
import org.oreo.oreosCustomCrafting.menus.CustomInventoryListener
import org.oreo.oreosCustomCrafting.menus.DisabledRecipeListener
import org.oreo.oreosCustomCrafting.utils.SerializeUtils
import org.oreo.oreosCustomCrafting.utils.Utils
import java.io.File
import java.io.FileReader
import java.lang.NullPointerException


class CustomCrafting : JavaPlugin() {  //TODO organise the code
    //TODO make a button to add a recipe on the crafting group eventually

    private val gson = Gson()

    var itemDir: File? = null

    private var craftingDir: File? = null

    var shapedRecipeDir: File? = null
    var shapelessRecipeDir: File? = null

    /**
     * Handle all the directories and recipes upon loading in
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

        server.pluginManager.registerEvents(CustomInventoryListener(), this)
        server.pluginManager.registerEvents(DisabledRecipeListener(), this)
        server.pluginManager.registerEvents(CustomIngredientListener(), this)

        saveDefaultConfig()
        loadDisabledRecipes()
        //loadGroupsFromFile()
    }

    override fun onDisable() {
        saveDefaultConfig()
        saveDisabledRecipes()

        //saveGroupsToFile(groups)
    }

    /**
     * Loads all the disabled recipes and puts them back in the disabledRecipes list
     * These recipes are saved as the recipe key under the minecraft nameSpace
     */
    private fun loadDisabledRecipes() {
        val pluginDirectory = dataFolder
        val file = File(pluginDirectory, "disabled_recipes.json")

        if (!file.exists()) {
            Bukkit.getLogger().warning("disabled_recipes.json not found. Creating a new empty file.")
            file.createNewFile()
            file.writeText("[]") // Initialize with an empty JSON array
            return
        }

        val disabledKeyList: List<String>
        try {
            val json = file.readText()
            val type = object : TypeToken<List<String>>() {}.type
            disabledKeyList = Gson().fromJson(json, type) ?: throw NullPointerException("JSON file is empty or null")
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Failed to load disabled recipes: ${e.message}")
            e.printStackTrace()
            return
        }

        // Process the keys only if they were successfully loaded
        for (key in disabledKeyList) {
            val recipe = Bukkit.getRecipe(NamespacedKey.minecraft(key))
            if (recipe == null) {
                Bukkit.getLogger().warning("Recipe with key '$key' not found in the server's registry.")
                continue
            }
            if (recipe is CraftingRecipe) {
                disabledRecipes.add(recipe)
            }
        }
    }

    /**
     * Saves all the disabled recipes into a json file that is a list of the recipe's keys
     * The key is stored as the nameSpaceKey's key (Every recipe uses the default Minecraft namespace)
     */
    private fun saveDisabledRecipes() {
        val recipes: ArrayList<String> = arrayListOf()

        for (recipe in disabledRecipes) {
            recipes.add(recipe.key.key)
        }

        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(recipes)

        // Get the Bukkit plugin directory
        val pluginDirectory = dataFolder
        val file = File(pluginDirectory, "disabled_recipes.json")

        try {
            file.writeText(json)
            Bukkit.getLogger().info("Disabled recipes saved successfully.")
        } catch (e: Exception) {
            Bukkit.getLogger().severe("Failed to save disabled recipes: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Register and save the shaped recipe as a file
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

    /**
     * Register and save the shapeless recipe as a file
     */
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
     * Registers all custom recipes saved in the files shaped and shapeless
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

    /**
     * Saves the custom ingredients of a shaped recipe so that they can be checked in the recipe listener
     */
    private fun saveCustomIngredientRecipe(recipeData: ShapedRecipeData, recipe: Recipe) {
        if (recipeData.customIngredients.isNotEmpty()) {
            val customItems: ArrayList<ItemStack> = arrayListOf()

            for (itemName in recipeData.customIngredients) {
                customItems.add(Utils.getCustomItem(itemName))
            }
            customIngredientRecipes.add(Pair(customItems.toList(), recipe.result))
        }
    }

    /**
     * Saves the custom ingredients of a shapeless recipe so that they can be checked in the recipe listener
     */
    private fun saveCustomIngredientRecipe(recipeData: ShapeLessRecipeData, recipe: Recipe) {
        if (recipeData.ingredientsItems.isNotEmpty()) {
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

        /**
         * A list that has all the custom items of recipes used to reference in the custom item listener
         */
        val customIngredientRecipes: ArrayList<Pair<List<ItemStack>, ItemStack>> = arrayListOf()

        /**
         * A list of all custom items which is loaded from the save file
         */
        val customItems: HashMap<String, ItemStack> = hashMapOf()

        /**
         * All the custom recipes as their raw data form
         */
        val customRecipes: MutableList<CustomRecipeData> = mutableListOf()

        /**
         * All the disabled recipes referenced in the disabled recipes listener
         */
        val disabledRecipes: MutableList<CraftingRecipe> = mutableListOf()

        /**
         * All the recipe groups used for /recipes
         */
        var groups: HashMap<String, Pair<ItemStack, ArrayList<CustomRecipeData>>> = hashMapOf() //TODO save this somehow

        /**
         * We save all recipes to our own list because since the vanilla ones are accessed via Iterator, they have
        a different memory address every time and there's no such thing as .equals() for recipes
         */
        val allRecipesSaved: ArrayList<CraftingRecipe> = getAllRecipes()

        private fun getAllRecipes(): ArrayList<CraftingRecipe> {
            val recipes = mutableListOf<CraftingRecipe>()

            // Iterate through all the registered recipes
            for (recipe in Bukkit.recipeIterator()) {
                if (recipe is CraftingRecipe) {
                    recipes.add(recipe)
                }
            }

            return recipes as ArrayList<CraftingRecipe>
        }
    }
}
