package org.oreo.oreosCustomCrafting.menus.menuClasses

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.oreo.oreosCustomCrafting.data.RecipeData
import org.oreo.oreosCustomCrafting.menus.AbstractInventoryMenu
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeGroupAssignmentMenu(private val player: Player, private val group: String,
                                private val removeRecipes: Boolean): AbstractInventoryMenu(player) {

    private val rows = 5
    private val columns = 9
    override val invSize = rows * columns
    private val recipeMenuInvName = "Recipe settings"
    override val inventory = Bukkit.createInventory(null, invSize, recipeMenuInvName)

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage: Int = 0


    //All recipes that will be changed
    private val recipesToChange = arrayListOf<CustomRecipeData>()

    private val recipes: List<CustomRecipeData> = if (removeRecipes) {
        CustomCrafting.customRecipes.filter {
            it in (CustomCrafting.groups[group]?.second ?: throw IllegalArgumentException("Invalid group name"))
        }
    } else {
        CustomCrafting.customRecipes.filterNot {
            it in (CustomCrafting.groups[group]?.second ?: throw IllegalArgumentException("Invalid group name"))
        }
    }
    //The "Icon item" of the group
    private val groupIcon: ItemStack = Utils.createGuiItem(CustomCrafting.groups[group]?.first!!, "§l$group", null)

    init {
        addToList()
        loadPage(0)
        openInventory()
    }


    /**
     * Loads a specified page of recipes into the crafting inventory.
     * @param page The page number to load (0-based).
     */
    private fun loadPage(page: Int) {

        if (page < 0) throw IllegalArgumentException("Page can't be negative")


        for (slot in (rows - 1) * columns..invSize - 1) {
            inventory.setItem(slot, blank)
        }

        currentPage = page
        inventory.clear() // Clear the inventory before loading the new page

        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        var i = startIndex
        var recipeNumber = i
        while (i < endIndex) {
            val slot = i - startIndex
            val recipe = recipes[recipeNumber].recipeData

            val itemResult: ItemStack = if (recipe.fileResult != null) {

                CustomCrafting.customItems[recipe.fileResult]!!

            } else {
                ItemStack(recipe.materialResult!!)
            }

            val itemName = recipe.name


            val itemToAdd = Utils.createGuiItem(itemResult, itemName, null)

            inventory.setItem(slot, itemToAdd)
            recipeNumber++
            i++
        }

        inventory.setItem(invSize - 5, groupIcon)

        // Set navigation items in the last row
        setNextAndPrevItem(currentPage)
    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        val newInventory = inventory
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Handle any item being clicked
     */
    override fun handleClickedItem(slot: Int) {

        // Validate the slot is within the inventory size
        if (slot !in 0 until inventory.size) return

        val item = inventory.getItem(slot) ?: return

        when (item) {

            nextItem ->{
                loadPage(currentPage + 1)
                return
            }

            previousItem -> {
                loadPage(currentPage - 1)
                return
            }

            groupIcon -> {
                try {
                    val groupNames = CustomCrafting.groups.keys.toList() // Ensure it's a list
                    val groupIndex = group.let { groupNames.indexOf(it) }
                    val nextGroupName = groupNames[groupIndex + 1]

                    RecipeGroupAssignmentMenu(player, nextGroupName, removeRecipes)
                } catch (_: IndexOutOfBoundsException) {
                    RecipeGroupAssignmentMenu(player, CustomCrafting.groups.keys.toList()[0], removeRecipes)
                }

                return
            }
        }

        // Calculate the recipe index, ensuring it ignores the bottom row
        val recipeIndex = currentPage * itemsPerPage + slot

        if (recipeIndex in recipes.indices) {
            val recipe: RecipeData = recipes[recipeIndex].recipeData

            if (item.itemMeta?.displayName == "§a§lAdded" || item.itemMeta?.displayName == "§c§lRemoved") {
                // Item is marked as added; revert it
                val originalName = recipe.fileResult ?: recipe.materialResult?.toString()

                item.itemMeta = item.itemMeta?.apply {
                    setDisplayName(originalName)
                    removeEnchant(org.bukkit.enchantments.Enchantment.LUCK)
                }
                recipesToChange.remove(recipes[recipeIndex])
            } else {
                // Item is not marked; mark as added
                item.itemMeta = item.itemMeta?.apply {
                    if (removeRecipes) {
                        setDisplayName("§c§lRemoved")
                    } else {
                        setDisplayName("§a§lAdded")
                    }

                    addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true)
                    addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
                }
                if (!removeRecipes) {
                    if (!CustomCrafting.groups[group]!!.second.contains(recipes[recipeIndex])) {
                        recipesToChange.add(recipes[recipeIndex])
                    }
                } else {
                    recipesToChange.add(recipes[recipeIndex])
                }
            }
            inventory.setItem(slot, item) // Update the item in the inventory
            return
        }


    }

}