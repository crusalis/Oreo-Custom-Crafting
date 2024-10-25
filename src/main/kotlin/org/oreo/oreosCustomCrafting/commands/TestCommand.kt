package org.oreo.oreosCustomCrafting.commands

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.oreo.oreosCustomCrafting.CustomCrafting

class TestCommand(val plugin : CustomCrafting) : CommandExecutor {
    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {

        if (p0 !is Player) {
            return false
        }

        val itemInHand = p0.inventory.itemInMainHand

        if (itemInHand.type == Material.AIR){
            plugin.giveSavedItems(p0)
            return true
        }

        // Check if an item name was provided as the second parameter
        if (p3 == null || p3.isEmpty()) {
            p0.sendMessage("Please provide an item name.")
            return false
        }

        // Extract the item name from the command arguments
        val itemName = p3[0]

        plugin.saveCustomItemAsFile(itemInHand,itemName)
        itemInHand.amount = 0
        return true
    }
}