package org.oreo.oreosCustomCrafting.menus.recipeGroupAssignmentMenu

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.oreo.oreosCustomCrafting.data.ShapeLessRecipeData
import org.oreo.oreosCustomCrafting.data.ShapedRecipeData
import org.oreo.oreosCustomCrafting.menus.recipeGroupMenu.RecipeGroupMenu
import org.oreo.oreosCustomCrafting.menus.recipeGroupMenu.RecipeGroupMenu.Companion.groups
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeGroupAssignmentMenu (val player: Player,val group : String,val removeRecipes : Boolean) {

    private val rows = 5
    private val columns = 9
    private val invSize = rows * columns
    private val recipeMenuInvName = "Recipe settings"
    private val recipeMenuInv = Bukkit.createInventory(null, invSize, recipeMenuInvName)

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage : Int = 0

    private val blank = Utils.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, " ", null)

    private val recipesToCahnge = arrayListOf<CustomRecipeData>()

    private val recipes : List<CustomRecipeData> = if(removeRecipes) {
        CustomCrafting.customRecipes.filter {
            it in (groups[group]?.second ?: throw IllegalArgumentException("Invalid group name"))
        }
    } else {
        CustomCrafting.customRecipes.filterNot {
            it in (groups[group]?.second ?: throw IllegalArgumentException("Invalid group name"))
        }
    }

    val groupIcon: ItemStack = Utils.createGuiItem(groups[group]?.first!!, "§l$group", null)

    init {
        loadPage(0)
        openInventory()
    }


    /**
     * Loads a specified page of recipes into the crafting inventory.
     * @param page The page number to load (0-based).
     */
    fun loadPage(page: Int) {

        if (page < 0) throw IllegalArgumentException("Page can't be negative")


        for (slot in (rows - 1) * columns..invSize - 1) {
            recipeMenuInv.setItem(slot ,blank)
        }

        currentPage = page
        recipeMenuInv.clear() // Clear the inventory before loading the new page

        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        var i = startIndex
        var recipeNumber = i
        while (i < endIndex) {
            val slot = i - startIndex
            val recipe = recipes[recipeNumber].recipeData

            val itemResult : ItemStack = if (recipe is ShapedRecipeData) {

                if (recipe.fileResult != null){

                    CustomCrafting.customItems.get(recipe.fileResult)!!

                } else {
                    ItemStack(recipe.materialResult!!)
                }

            } else if (recipe is ShapeLessRecipeData) {

                if (recipe.fileResult != null){

                    CustomCrafting.customItems.get(recipe.fileResult)!!

                } else {
                    ItemStack(recipe.materialResult!!)
                }

            } else {
                closeInventory()
                player.sendMessage("${ChatColor.RED}ERROR item is not of correct type")
                throw IllegalArgumentException("Unexpected object type withing RecipeMenu instance 'recipes' list")
            }

            val itemName = if (recipe is ShapedRecipeData) {
                recipe.name
            } else if  (recipe is ShapeLessRecipeData) {
                recipe.name
            } else {
                closeInventory()
                player.sendMessage("${ChatColor.RED}ERROR item is not of correct type")
                throw IllegalArgumentException("Unexpected object type withing RecipeMenu instance 'recipes' list")
            }

            val itemToAdd = Utils.createGuiItem(itemResult,itemName,null)

            recipeMenuInv.setItem(slot, itemToAdd)
            recipeNumber++
            i++
        }

        recipeMenuInv.setItem(invSize - 5, groupIcon)

        // Set navigation items in the last row
        if (currentPage > 0) {
            recipeMenuInv.setItem(invSize - 7, Utils.createGuiItem(Material.CRIMSON_SIGN, "Previous", null))
        }
        if (!hasBlank()) {
            recipeMenuInv.setItem(invSize - 3, Utils.createGuiItem(Material.WARPED_SIGN, "Next", null))
        }
    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        val newInventory = recipeMenuInv
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player and remove its references
     */
    fun closeInventory() {
        openInventories.remove(recipeMenuInv)
        if (removeRecipes) {
            groups[group]!!.second.removeAll(recipesToCahnge)
        } else {
            groups[group]!!.second.addAll(recipesToCahnge)
        }

        try {
            recipeMenuInv.close()
        } catch (_: Exception){}
    }

    /**
     * Handle any item being clicked
     */
    fun handleClickedItem(slot: Int) {
        // Ignore slots in the bottom row (reserved for navigation)

        // Validate the slot is within the inventory size
        if (slot !in 0 until recipeMenuInv.size) return

        val item = recipeMenuInv.getItem(slot) ?: return


        // Handle group icon click
        if (item == groupIcon) {
            try {
                val groupNames = groups.keys.toList() // Ensure it's a list
                val groupIndex = group.let { groupNames.indexOf(it) }
                val nextGroupName = groupNames[groupIndex + 1]

                RecipeGroupAssignmentMenu(player, nextGroupName,removeRecipes)
            } catch (_: IndexOutOfBoundsException) {
                RecipeGroupAssignmentMenu(player, groups.keys.toList()[0],removeRecipes)
            }

            return
        }


        // Calculate the recipe index, ensuring it ignores the bottom row
        val recipeIndex = currentPage * itemsPerPage + slot

        if (recipeIndex in recipes.indices) {
            val recipe = recipes[recipeIndex].recipeData

            if (item.itemMeta?.displayName == "§a§lAdded" || item.itemMeta?.displayName == "§c§lRemoved") {
                // Item is marked as added; revert it
                val originalName = if (recipe is ShapedRecipeData) {
                    recipe.fileResult ?: recipe.materialResult?.toString()
                } else if(recipe is ShapeLessRecipeData){
                    recipe.fileResult ?: recipe.materialResult?.toString()
                } else {
                    "Unknown Item"
                }
                item.itemMeta = item.itemMeta?.apply {
                    setDisplayName(originalName)
                    removeEnchant(org.bukkit.enchantments.Enchantment.LUCK)
                }
                recipesToCahnge.remove(recipe)
            } else {
                // Item is not marked; mark as added
                item.itemMeta = item.itemMeta?.apply {
                    if (removeRecipes){
                        setDisplayName("§c§lRemoved")
                    } else {
                        setDisplayName("§a§lAdded")
                    }

                    addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true)
                    addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
                }
                if (!removeRecipes){
                    if (!groups[group]!!.second.contains(recipes[recipeIndex])){
                        recipesToCahnge.add(recipes[recipeIndex])
                    }
                } else {
                    recipesToCahnge.add(recipes[recipeIndex])
                }
            }
            recipeMenuInv.setItem(slot, item) // Update the item in the inventory
            return
        }

        // Handle navigation if it's in the bottom row
        if (slot in (invSize - columns until invSize)) {
            val name = item.itemMeta?.displayName ?: return
            when {
                name.contains("Next", true) -> loadPage(currentPage + 1)
                name.contains("Previous", true) -> loadPage(currentPage - 1)
            }
            return
        }
    }



    /**
     * Checks if the inventory has a blank space
     */
    private fun hasBlank() : Boolean {

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val item = recipeMenuInv.getItem(row * column)
                if (item == null || item.type == Material.AIR) return true
            }
        }

        return false
    }


    companion object {

        val openInventories = mutableMapOf<Inventory, RecipeGroupAssignmentMenu>()

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getInstance(inv: Inventory): RecipeGroupAssignmentMenu? {
            return openInventories[inv]
        }
    }

}