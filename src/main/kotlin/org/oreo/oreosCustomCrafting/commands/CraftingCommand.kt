package org.oreo.oreosCustomCrafting.commands

import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.menus.customCrafting.CustomCraftingInventory
import org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu.RecipeInventory
import org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu.ViewType


class CraftingCommand(private val plugin: CustomCrafting) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return false
        }

        if (!sender.isOp){
            sender.sendMessage("${ChatColor.RED} This command can only be used by players.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Please specify a subcommand")
            return false
        }

        when (args[0].lowercase()) {

            "add" -> {

                if (args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Please specify the name of the recipe.")
                    return true
                }

                val recipeName = args[1]
                CustomCraftingInventory(sender, recipeName, plugin)
            }

            "remove" -> {

                if (args.size <= 2 || args[1].isEmpty()) {

                    sender.sendMessage("${ChatColor.RED}Please specify a recipe name.")
                    return true
                }

                val recipeName = args[1]

                if (plugin.removeCraftingRecipe(recipeName)){

                    sender.sendMessage("${ChatColor.GREEN}Recipe $recipeName was removed successfully.")

                } else {
                    sender.sendMessage("${ChatColor.RED}Recipe $recipeName does not exist.")
                    sender.sendMessage("${ChatColor.RED}or something has gone terribly wrong.")
                }

            }

            "toggle" -> {
                if (args.size < 2 || args[1].isEmpty()){
                    sender.sendMessage("${ChatColor.RED}Please specify subcommand.")
                    return true
                }
                val showOnlyCustom = (args.size >= 3 && args[2].isNotEmpty() && args[2] == "custom")
                when (args[1]) {
                    "all" -> {
                        RecipeInventory(sender,ViewType.ALL,showOnlyCustom)

                    }
                    "enabled" -> RecipeInventory(sender,ViewType.ENABLED,showOnlyCustom)
                    "disabled" -> RecipeInventory(sender,ViewType.DISABLED,showOnlyCustom)

                    else -> {
                        sender.sendMessage("${ChatColor.RED}Please specify subcommand.")
                        return true
                    }
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {

            1 -> {
                return if (sender.isOp) {
                    listOf("add","remove","toggle","recipes").filter { it.startsWith(args[0], ignoreCase = true) }
                } else {
                    listOf("recipes")
                }
            }

            2 -> {

                var recipes = arrayListOf<String>()

                if (args[0] == "remove") {

                    for (file in plugin.craftingDir?.listFiles()!!) {

                        if (file.isDirectory) continue
                        //drop the ".json" part to match the recipes identifier
                        recipes.add(file.name.dropLast(5))
                    }
                } else if (args[0] == "toggle") {

                    recipes.add("all")
                    recipes.add("enabled")
                    recipes.add("disabled")
                }

                recipes
            }

            3 -> {
                if (args[0] == "toggle") {

                    return listOf("custom")
                }

                return emptyList()
            }

            else -> emptyList()
        }
    }
}