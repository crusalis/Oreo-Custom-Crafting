package org.oreo.customCrafting

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.oreo.siegebridge.itemManager.ItemManager

class CustomCrafting : JavaPlugin() {

    override fun onEnable() { //TODO ignore material data
        ItemManager.init(this) //This is just for testing purposes

        craftSiegeBridge()
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

}
