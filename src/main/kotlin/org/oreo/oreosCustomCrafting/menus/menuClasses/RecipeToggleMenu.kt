package org.oreo.oreosCustomCrafting.menus.menuClasses

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.CraftingRecipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.menus.AbstractInventoryMenu
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeInventory(player: Player, private val viewType: ViewType, private val showOnlyCustom: Boolean)
            : AbstractInventoryMenu(player){

    private val rows = 5
    private val columns = 9
    override val invSize = rows * columns
    private val craftingInvName = "Recipe settings"
    override val inventory = Bukkit.createInventory(null, invSize, craftingInvName)

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage: Int = 0

    private var slotToRecipe: MutableMap<Int, CraftingRecipe> = mutableMapOf()

    init {
        addToList()
        player.openInventory(inventory)
        loadPage(0)
    }


    /**
     * Loads a specified page of recipes into the crafting inventory.
     * @param page The page number to load (0-based).
     */
    private fun loadPage(page: Int) {

        if (page < 0) throw IllegalArgumentException("Page can't be negative")

        // Update recipes based on current ViewType to reflect any changes
        val recipes: List<CraftingRecipe> = if (showOnlyCustom) {
            when (viewType) {
                // ViewType.ENABLED: Only get enabled recipes (those not in disabledRecipes)
                ViewType.ENABLED -> CustomCrafting.customRecipes
                    .filterNot { it.recipe in CustomCrafting.disabledRecipes }
                    .map { it.recipe }

                // ViewType.DISABLED: Only get disabled recipes (those in disabledRecipes)
                ViewType.DISABLED -> CustomCrafting.customRecipes
                    .filter { it.recipe in CustomCrafting.disabledRecipes }
                    .map { it.recipe }

                // ViewType.ALL: Get all recipes, regardless of their state
                ViewType.ALL -> CustomCrafting.customRecipes.map { it.recipe }
            }
        } else {
            when (viewType) {
                // ViewType.ENABLED: Get all enabled recipes (those not in disabledRecipes)
                ViewType.ENABLED -> CustomCrafting.allRecipesSaved
                    .filterNot { it in CustomCrafting.disabledRecipes }

                // ViewType.DISABLED: Combine disabledRecipes and non-enabled recipes from customRecipes
                ViewType.DISABLED -> CustomCrafting.disabledRecipes.plus(
                    CustomCrafting.customRecipes
                        .filter { it.recipe in CustomCrafting.disabledRecipes }
                        .map { it.recipe }
                )

                // ViewType.ALL: Get all saved recipes, including custom and disabled ones
                ViewType.ALL -> CustomCrafting.allRecipesSaved
            }
        }

        for (slot in (rows - 1) * columns..invSize - 1) {
            inventory.setItem(slot, blank)
        }

        slotToRecipe.clear()
        currentPage = page
        inventory.clear() // Clear the inventory before loading the new page

        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        var i = startIndex
        var recipeNumber = i
        while (i < endIndex) {
            val slot = i - startIndex

            val recipe: CraftingRecipe = try {
                recipes[recipeNumber]
            } catch (_: IndexOutOfBoundsException) {
                break
            }


            if (recipe.result.type == Material.AIR) {
                recipeNumber++
                continue
            }


            // Update item status based on whether it’s enabled or disabled
            val isDisabled: Boolean = CustomCrafting.disabledRecipes.contains(recipe)

            val hasGlint : Boolean = if (isDisabled) {
                viewType != ViewType.DISABLED
            } else {
                viewType == ViewType.DISABLED
            }

            val statusName = if (isDisabled) "Disabled" else "Enabled"
            val statusPrefix = if (isDisabled) "§c§l" else "§a§l"

            val itemResult = Utils.createGuiItem(
                item = recipe.result,
                name = statusName,
                prefix = statusPrefix,
                recipe.result.itemMeta?.displayName,
                addEnchantGlint = hasGlint
            )

            slotToRecipe[slot] = recipe
            inventory.setItem(slot, itemResult)
            recipeNumber++
            i++
        }

        // Set navigation items in the last row
        setUpHidItems(currentPage)
    }

    /**
     * Handle any item being clicked
     */
    override fun handleClickedItem(slot: Int) {

        val item = inventory.getItem(slot) ?: return

        val name = item.itemMeta?.displayName ?: return

        if (name.contains("Next")) {
            loadPage(currentPage + 1)
        } else if (name.contains("Previous")) {
            loadPage(currentPage - 1)
        } else {

            val recipe : CraftingRecipe = slotToRecipe[slot] ?: return

            val enableMode : Boolean = viewType != ViewType.DISABLED

            if (name.contains("Enabled")) {

                if (!CustomCrafting.disabledRecipes.contains(recipe)) {
                    CustomCrafting.disabledRecipes.add(recipe)
                }

                val itemToAdd = Utils.createGuiItem(
                    item = item, name = "Disabled",
                    prefix = "§c§l", addEnchantGlint = enableMode
                )

                if (!enableMode) {
                    val meta = itemToAdd.itemMeta
                    meta?.apply {
                        // Remove the LUCK enchantment
                        removeEnchant(Enchantment.LUCK)
                    }
                    itemToAdd.itemMeta = meta
                }

                inventory.setItem(
                    slot, itemToAdd
                )

            } else if (name.contains("Disabled")) {
                CustomCrafting.disabledRecipes.remove(recipe)

                val itemToAdd =  Utils.createGuiItem(
                item = item, name = "Enabled",
                prefix = "§a§l", addEnchantGlint = !enableMode,
                )

                if (enableMode) {
                    val meta = itemToAdd.itemMeta
                    meta?.apply {
                        // Remove the LUCK enchantment
                        removeEnchant(Enchantment.LUCK)
                    }
                    itemToAdd.itemMeta = meta
                }

                inventory.setItem(slot,itemToAdd)
            }
        }
    }


}

/**
 * The ways you can view this inventory
 */
enum class ViewType {
    ENABLED,
    DISABLED,
    ALL
}