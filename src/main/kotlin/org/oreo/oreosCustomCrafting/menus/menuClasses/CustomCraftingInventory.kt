package org.oreo.oreosCustomCrafting.menus.menuClasses

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.data.getKeyFromValue
import org.oreo.oreosCustomCrafting.menus.AbstractInventoryMenu
import org.oreo.oreosCustomCrafting.utils.Utils

class CustomCraftingInventory(private val player: Player, private val recipeName: String, private val plugin: CustomCrafting)
    : AbstractInventoryMenu(player){

    private val craftingInvName = "Create a custom recipe"
    override val inventory = Bukkit.createInventory(null, 9 * 6, craftingInvName)
    override val invSize = inventory.size
    // All the custom items common to most menus
    private val acceptButton = Utils.createGuiItem(Material.GREEN_CONCRETE, "Save", null)
    private val cancelButton = Utils.createGuiItem(Material.RED_CONCRETE, "Cancel", null)

    private val shapedButton = Utils.createGuiItem(Material.BLUE_CONCRETE, "Shaped", null)
    private val shapedLessButton = Utils.createGuiItem(Material.YELLOW_CONCRETE, "Shapeless", null)

    private var isShaped = true

    private val toggleSlot = 49
    private val acceptSlot = 47
    private val cancelSlot = 51

    private val customRecipeMaterials = arrayListOf<String>()

    init {
        addToList()
        initializeMenuItems()
        openInventory(player)
    }

    /**
     * Initializes the crafting inventory items.
     */
    private fun initializeMenuItems() {
        // Fill all slots with blank items first
        for (i in 0..53) {
            inventory.setItem(i, blank)
        }

        // Create a 3x3 square in the middle (slots 20 to 28)
        for (row in -1..1) {
            for (col in 0..2) {
                inventory.setItem(20 + (row * 9) + col, ItemStack(Material.AIR))
            }
        }

        // Leave one empty square (slot 24)
        inventory.setItem(24, ItemStack(Material.AIR)) // Empty square

        // Fill the remaining bottom row slots (48 to 53) with blank items
        for (i in 44..53) {
            inventory.setItem(i, blank)
        }

        // Set the buttons
        inventory.setItem(acceptSlot, ItemStack(acceptButton)) // Save button

        inventory.setItem(toggleSlot, ItemStack(shapedButton)) // Shaped/shapeless toggle

        inventory.setItem(cancelSlot, ItemStack(cancelButton)) // Cancel button
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
     * Saves the recipe from the inventory into a file and registers it
     */
    private fun saveRecipe() {


        val resultSlotItem: ItemStack = try {
            inventory.getItem(RESULT_SLOT) ?: throw NullPointerException("No result item set")
        } catch (e : NullPointerException) {
            player.sendMessage("${ChatColor.RED}${e.message}")
            return
        }

        if (inventory.getItem(RESULT_SLOT) != null && Utils.isCustomItem(resultSlotItem) ||
            !Utils.customItemExists(resultSlotItem)
        ) {

            Utils.saveCustomItemAsFile(resultSlotItem, plugin)
        }


        val returnRecipe = try {
            if (isShaped) {

                val recipeMapping = handleStringConversion()

                val recipe = ShapedRecipe(NamespacedKey.minecraft(recipeName), resultSlotItem)
                recipe.shape(
                    recipeMapping.first[0],
                    recipeMapping.first[1],
                    recipeMapping.first[2]
                )

                for ((char, ingredient) in recipeMapping.second) {
                    recipe.setIngredient(char, ingredient)
                }

                recipe
            } else {
                val recipe = ShapelessRecipe(NamespacedKey.minecraft(recipeName), resultSlotItem)

                for (slot in CRAFTING_SLOTS) {
                    val item = inventory.getItem(slot) ?: continue
                    recipe.addIngredient(item)
                }

                recipe
            }
        } catch (e: IllegalArgumentException) {
            player.sendMessage("${ChatColor.RED}${e.localizedMessage}")
            return
        }

        CustomCrafting.allRecipesSaved.add(returnRecipe)

        when (returnRecipe) {
            is ShapedRecipe -> {
                plugin.registerAndSaveRecipe(returnRecipe, recipeName, customRecipeMaterials)
            }

            is ShapelessRecipe -> {
                plugin.registerAndSaveRecipe(returnRecipe, recipeName, customRecipeMaterials)
            }

            else -> {
                plugin.logger.warning("Could not save recipe $craftingInvName")
            }
        }
    }

    /**
     * Takes the items from the custom crafting inventory and converts them into a list of three strings
    for the actual creation of the recipe along with the corresponding characters
     */
    private fun handleStringConversion(): Pair<List<String>, Map<Char, Material>> {
        val items = ArrayList<ItemStack?>()
        val materialMap = mutableMapOf<Material, Char>()
        val charToMaterialMap = mutableMapOf<Char, Material>()
        var currentChar = 'A'

        // Fill the list with items
        for (slot in CRAFTING_SLOTS) {
            val item = inventory.getItem(slot)
            items.add(item)
        }

        // Create a list to hold the output strings
        val outputLines = mutableListOf<StringBuilder>()

        // Initialize three lines
        repeat(3) { outputLines.add(StringBuilder()) }

        for (item in items) {
            val char = when {
                item == null || item.type == Material.AIR -> ' '
                else -> materialMap.getOrPut(item.type) {
                    currentChar++.also { if (currentChar > 'Z') currentChar = 'A' } // Reset to 'A' if over 'Z'
                }
            }

            // Store the character and material in the inverted map
            if (char != ' ') {

                if (item != null) {
                    charToMaterialMap[char] = item.type

                    val stringToAdd = if (Utils.customItemExists(item)) {
                        val customItemName: String = CustomCrafting.customItems.getKeyFromValue(item)!!
                        customItemName
                    } else {
                        val fileName = Utils.saveCustomItemAsFile(item, plugin = plugin)!!.name
                        fileName
                    }
                    customRecipeMaterials.add(stringToAdd)
                }
            }

            // Append the character to the corresponding line
            for (line in outputLines) {
                if (line.length < 3) {
                    line.append(char)
                    break
                }
            }
        }

        // Ensure each line is exactly 3 characters long by padding with spaces
        for (line in outputLines) {
            while (line.length < 3) {
                line.append(' ')
            }
        }

        val outputStrings = outputLines.map { it.toString() }
        return Pair(outputStrings, charToMaterialMap)
    }


    override fun handleClickedItem(slot: Int) {}

    fun handleCraftingItemClicked(slot: Int): Boolean {

        val clickedItem = inventory.getItem(slot) ?: return false
        when (clickedItem) {

            acceptButton -> {
                saveRecipe()
                inventory.close()
                return true
            }

            cancelButton -> {
                inventory.close()
                return true
            }

            shapedButton -> {
                handleShapeLessToggle()
                return true
            }

            shapedLessButton -> {
                handleShapedToggle()
                return true
            }

            blank ->{
                return true
            }
        }

        return false
    }

    /**
     * Handles the player toggling to "shaped" mode
     */
    private fun handleShapedToggle() {

        isShaped = true
        inventory.setItem(toggleSlot, shapedButton)
    }

    /**
     * Handles the player toggling to "shapeless" mode
     */
    private fun handleShapeLessToggle() {

        isShaped = false
        inventory.setItem(toggleSlot, shapedLessButton)
    }


    companion object {
        const val RESULT_SLOT = 24

        val CRAFTING_SLOTS = listOf(11, 12, 13, 20, 21, 22, 29, 30, 31)

        val openInventories = mutableMapOf<Inventory, CustomCraftingInventory>()
    }
}
