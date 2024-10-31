package org.oreo.oreosCustomCrafting.commands

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.menu.CustomCraftingInventory
import org.oreo.oreosCustomCrafting.utils.Utils


class TestCommand(private val plugin: CustomCrafting) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return false
        }

        val itemInHand = sender.inventory.itemInMainHand

        if (args.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}Please specify a subcommand: save, give <name>, or all.")
            return false
        }

        when (args[0].lowercase()) {

            "save" -> {
                if (itemInHand.type == Material.AIR) {
                    sender.sendMessage("${ChatColor.RED}You must hold an item to save.")
                    return false
                }

                Utils.saveCustomItemAsFile(itemInHand, plugin)
                itemInHand.amount = 0
                sender.sendMessage("${ChatColor.GREEN}Item saved.")
            }

            "give" -> {
                if (args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Please specify the name of the item to give.")
                    return false
                }
                val itemName = args[1]
                val customItem = Utils.getCustomItem(itemName)
                sender.inventory.addItem(customItem)
                sender.sendMessage("${ChatColor.GREEN}Gave you one $itemName.")
            }

            "craftin" -> {
                CustomCraftingInventory(sender)
            }

            "all" -> {
                for (item in CustomCrafting.customItems.values) {
                    sender.inventory.addItem(item)
                }
                sender.sendMessage("${ChatColor.GREEN}All custom items have been added to your inventory.")
            }

            else -> {
                sender.sendMessage("${ChatColor.RED} Invalid subcommand. Use save, give <name>, or all.")
                return false
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("save", "give", "all","craftin").filter { it.startsWith(args[0], ignoreCase = true) }
            2 -> if (args[0].equals("give", ignoreCase = true)) {
                CustomCrafting.customItems.keys.filter { it.startsWith(args[1], ignoreCase = true) }
            } else {
                emptyList()
            }

            else -> emptyList()
        }
    }
}