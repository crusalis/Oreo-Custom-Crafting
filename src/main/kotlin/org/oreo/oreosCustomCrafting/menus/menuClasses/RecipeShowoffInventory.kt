package org.oreo.oreosCustomCrafting.menus.menuClasses

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.oreo.oreosCustomCrafting.data.ShapeLessRecipeData
import org.oreo.oreosCustomCrafting.data.ShapedRecipeData
import org.oreo.oreosCustomCrafting.menus.AbstractInventoryMenu
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeShowoffInventory(private val player: Player, private val recipe: CustomRecipeData): AbstractInventoryMenu(player) {

    private val name = recipe.recipeData.name

    private val suffix = if (recipe.recipeData is ShapedRecipeData){
        "[Shaped]"
    } else {
        "[Shapeless]"
    }

    private val craftingInvName = "$name $suffix"
    override val inventory = Bukkit.createInventory(null, 9 * 6, craftingInvName)
    override val invSize = inventory.size

    init {
        addToList()
        initializeMenuItems()
        openInventory(player)
    }

    /**
     * Initializes the crafting inventory items.
     */
    private fun initializeMenuItems() { //TODO not working on shapeless

        val recipeData = recipe.recipeData

        // Fill all slots with blank items first
        for (i in 0..53) {
            inventory.setItem(i, blank)
        }

        // Fill the result slots
        if (recipeData is ShapedRecipeData) {
            var slotIndex = 0

            val customItems = recipeData.customIngredients.map { customKey ->
                Utils.getCustomItem(customKey)
            }.toMutableList()

            // Loop through each row in the recipe
            for (row in recipeData.rows) {
                // Loop through each character in the row
                for (char in row) {

                    val slot = CRAFTING_SLOTS[slotIndex]

                    if (char == ' ') {
                        // Empty slot in the crafting grid
                        inventory.setItem(slot, ItemStack(Material.AIR))
                    } else {
                        // Get the material corresponding to the character from ingredient map
                        val material = recipeData.ingredients[char]

                        if (material != null) {

                            val customItem = customItems.firstOrNull { it.type == material }

                            if (customItem != null) {
                                inventory.setItem(slot, customItem)
                                customItems.remove(customItem)
                            } else {
                                inventory.setItem(slot, ItemStack(material))
                            }
                        } else {
                            // If no material is found, set the slot to AIR
                            inventory.setItem(slot, ItemStack(Material.AIR))
                        }
                    }
                    slotIndex++
                }
            }
        } else if (recipeData is ShapeLessRecipeData) {

            val customItems = recipeData.ingredientsItems.map { customKey ->
                Utils.getCustomItem(customKey)
            }.toMutableList()

            for (slot in CRAFTING_SLOTS) {
                inventory.setItem(slot, ItemStack(Material.AIR))
            }

            for (slot in CRAFTING_SLOTS) {

                try {
                    val item = ItemStack(recipeData.ingredientsMaterials[CRAFTING_SLOTS.indexOf(slot)])

                    val customItem = customItems.firstOrNull { it.type == item.type }

                    if (customItem != null) {
                        inventory.setItem(slot, customItem)
                        customItems.remove(customItem)
                    } else {
                        inventory.setItem(slot, item)
                    }

                } catch (_: IndexOutOfBoundsException) {
                    break
                }
            }

        } else {
            player.sendMessage("${ChatColor.RED}RecipeData is of unknown type")
            throw IllegalArgumentException("RecipeData is of unknown type")
        }

        // Result slot
        inventory.setItem(RESULT_SLOT, recipe.recipe.result)

        // Fill the remaining bottom row slots (48 to 53) with blank items
        for (i in 44..53) {
            inventory.setItem(i, blank)
        }

        inventory.setItem(49, closeItem)
    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory(player: Player) {
        val newInventory = inventory
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player and remove its references
     */
    override fun closeInventory() {
        openInventories.remove(inventory)
        try {
            inventory.close()
            RecipeGroupMenu(player)
        } catch (_: Exception) {}
    }


    override fun handleClickedItem(slot : Int){

        val clickedItem = inventory.getItem(slot) ?: return

        if (clickedItem == closeItem){
            closeInventory()
        }
    }


    companion object {
        //The slot for the resulting item
        const val RESULT_SLOT = 24

        //All the items for the crafting inventory
        val CRAFTING_SLOTS = listOf(11, 12, 13, 20, 21, 22, 29, 30, 31)
    }
}
