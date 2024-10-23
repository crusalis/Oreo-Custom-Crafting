package org.oreo.customCrafting

import com.google.gson.Gson
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.oreo.siegebridge.itemManager.ItemManager
import java.io.File

class CustomCrafting : JavaPlugin() {


    private val gson = Gson()

    override fun onLoad() {
        handleDirectories()
    }

    override fun onEnable() {

        ItemManager.init(this) //This is just for testing purposes
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun craftSiegeBridge(){

        val item = ItemManager.siegeBridge

        val sr : ShapedRecipe = ShapedRecipe(NamespacedKey.minecraft("siege bridge"),item!!)
        sr.shape("YXY",
                 "X T",
                 "TXX")
        sr.setIngredient('Y', Material.YELLOW_DYE)
        sr.setIngredient('X', Material.JUKEBOX)
        sr.setIngredient('T', Material.TUFF)
        Bukkit.getServer().addRecipe(sr)

    }

    private fun registerCustomRecipes(){

        for (file in dataFolder.listFiles()!!) {

            if (!file.isFile) return



        }

    }

    private fun handleDirectories(){

        val craftingDir = createDataDirectory("Custom Crafting", dataFolder)

        if (craftingDir != null && craftingDir.exists()) {
            createDataDirectory("Custom Items",craftingDir)
        } else {
            logger.warning("Failed to create custom Items directory")
        }

    }

    private fun createDataDirectory(directoryName: String, dataFolder: File) : File? {

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

        val customItems : MutableList<ItemStack> = mutableListOf()

    }

}
