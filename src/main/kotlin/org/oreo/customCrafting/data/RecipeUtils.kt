package org.oreo.customCrafting.data

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.meta.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.oreo.customCrafting.data.itemMeta.ItemMetaData
import org.oreo.customCrafting.data.itemMeta.itemSpecific.AttributeModifierData
import org.oreo.customCrafting.data.itemMeta.itemSpecific.PotionEffectData
import org.oreo.customCrafting.data.recipe.RecipeChoiceData
import java.util.*


object RecipeUtils {

    /**
     * Converts the RecipeChoiceData class into a RecipeChoice instance
     */
    fun dataToRecipeChoice(recipeChoiceData: RecipeChoiceData): RecipeChoice {
        return when (recipeChoiceData) {
            is RecipeChoiceData.MaterialChoiceData -> {
                // Convert MaterialChoiceData back to RecipeChoice.MaterialChoice
                RecipeChoice.MaterialChoice(
                    recipeChoiceData.materials.map { Material.valueOf(it) }
                )
            }
            is RecipeChoiceData.ExactChoiceData -> {
                // Convert ExactChoiceData back to RecipeChoice.ExactChoice
                RecipeChoice.ExactChoice(
                    recipeChoiceData.items.map { itemData ->
                        val itemStack = ItemStack(Material.valueOf(itemData.material), itemData.amount)
                        itemData.itemMetaData?.let {
                            itemStack.itemMeta = dataToItemMeta(it, itemStack.itemMeta!!)
                        }
                        itemStack
                    }
                )
            }
        }
    }

    /**
     * Converts the projects data class back into ItemMeta
     * This is used to convert from Json file to Item
     */
    fun dataToItemMeta(itemMetaData: ItemMetaData, itemMeta: ItemMeta): ItemMeta {
        // Set display name
        itemMeta.setDisplayName(itemMetaData.displayName)

        // Set lore
        itemMeta.lore = itemMetaData.lore

        // Apply enchantments
        itemMetaData.enchantments?.forEach { (enchantmentKey, level) ->
            val enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantmentKey))
            if (enchantment != null) {
                itemMeta.addEnchant(enchantment, level, true)
            }
        }

        // Apply item flags
        itemMetaData.itemFlags?.forEach { flagName ->
            val itemFlag = ItemFlag.valueOf(flagName)
            itemMeta.addItemFlags(itemFlag)
        }

        // Set unbreakable
        itemMeta.isUnbreakable = itemMetaData.unbreakable ?: false

        // Set custom model data
        if (itemMetaData.customModelData != null) {
            itemMeta.setCustomModelData(itemMetaData.customModelData)
        }

        // Apply attribute modifiers (if they exist)
        itemMetaData.attributeModifiers?.forEach { modifierData ->
            val attribute = Attribute.valueOf(modifierData.attribute)
            val uuid = UUID.fromString(modifierData.uuid)
            val modifier = AttributeModifier(uuid, "custom", modifierData.amount, AttributeModifier.Operation.valueOf(modifierData.operation))
            itemMeta.addAttributeModifier(attribute, modifier)
        }

        // Apply damage (if it's a Damageable item)
        if (itemMeta is Damageable && itemMetaData.damage != null) {
            itemMeta.damage = itemMetaData.damage
        }

        // Apply persistent data
        itemMetaData.persistentData?.forEach { (key, value) ->
            val namespacedKey = NamespacedKey.minecraft(key)
            itemMeta.persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, value)
        }

        // Apply potion effects (if it's a PotionMeta)
        if (itemMeta is PotionMeta && itemMetaData.potionEffects != null) {
            itemMetaData.potionEffects.forEach { effectData ->
                val effectType = PotionEffectType.getByName(effectData.type)
                if (effectType != null) {
                    val potionEffect = PotionEffect(
                        effectType,
                        effectData.duration,
                        effectData.amplifier,
                        effectData.ambient,
                        effectData.particles,
                        effectData.icon
                    )
                    itemMeta.addCustomEffect(potionEffect, true)
                }
            }
        }

        // Apply skull owner (if it's a SkullMeta)
        if (itemMeta is SkullMeta && itemMetaData.skullOwner != null) {
            itemMeta.owningPlayer = Bukkit.getOfflinePlayer(itemMetaData.skullOwner)
        }

        // Apply leather armor color (if it's a LeatherArmorMeta)
        if (itemMeta is LeatherArmorMeta && itemMetaData.leatherArmorColor != null) {
            val color = Color.fromRGB(
                Integer.parseInt(itemMetaData.leatherArmorColor.substring(1, 3), 16),
                Integer.parseInt(itemMetaData.leatherArmorColor.substring(3, 5), 16),
                Integer.parseInt(itemMetaData.leatherArmorColor.substring(5, 7), 16)
            )
            itemMeta.setColor(color)
        }

        return itemMeta
    }

    /**
     * Converts an item into this projects custom Data class instance to allow easy JSON serialisation
     */
    fun itemMetaToData(itemMeta: ItemMeta): ItemMetaData {
        val enchantments = itemMeta.enchants.mapKeys { it.key.key.toString() } // Convert enchantments to a map
        val itemFlags = itemMeta.itemFlags.map { it.name } // Convert item flags to a list of strings
        val attributeModifiers = itemMeta.attributeModifiers?.entries()?.map {
            AttributeModifierData(
                attribute = it.key.name,
                amount = it.value.amount,
                operation = it.value.operation.name,
                uuid = it.value.uniqueId.toString()
            )
        }

        // Example for potion effects (if it's a PotionMeta)
        val potionEffects = if (itemMeta is PotionMeta) {
            itemMeta.customEffects.map {
                PotionEffectData(
                    type = it.type.name,
                    duration = it.duration,
                    amplifier = it.amplifier,
                    ambient = it.isAmbient,
                    particles = it.hasParticles(),
                    icon = it.hasIcon()
                )
            }
        } else null

        // Example for skull owner (if it's a SkullMeta)
        val skullOwner = if (itemMeta is SkullMeta) {
            itemMeta.owningPlayer?.name
        } else null

        // Example for leather armor color (if it's a LeatherArmorMeta)
        val leatherArmorColor = if (itemMeta is LeatherArmorMeta) {
            itemMeta.color.asARGB() // Converting into RGB
        } else null

        return ItemMetaData(
            displayName = itemMeta.displayName,
            lore = itemMeta.lore,
            enchantments = enchantments.ifEmpty { null },
            itemFlags = itemFlags.ifEmpty { null },
            unbreakable = itemMeta.isUnbreakable,
            customModelData = itemMeta.customModelData,
            attributeModifiers = attributeModifiers,
            damage = (itemMeta as? Damageable)?.damage,
            persistentData = itemMeta.persistentDataContainer.keys.associate { key ->
                key.key to itemMeta.persistentDataContainer[key, PersistentDataType.STRING]!!
            },
            potionEffects = potionEffects,
            skullOwner = skullOwner,
            leatherArmorColor = leatherArmorColor.toString()
        )
    }

}