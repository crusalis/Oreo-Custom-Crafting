package org.oreo.oreosCustomCrafting.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

object SerializeUtils {

    /**
     * Serialize the item to save it in a file
     */
    fun serializeItem(item: ItemStack?): String {
        try {
            val out = ByteArrayOutputStream()
            val dataOut = BukkitObjectOutputStream(out)

            dataOut.writeObject(if (item == null || item.type == Material.AIR) null else item)

            dataOut.close()
            return Base64Coder.encodeLines(out.toByteArray())
        } catch (e: IOException) {
            throw RuntimeException("Failed to serialize item", e)
        }
    }

    /**
     *     Deserialize base64 string to single ItemStack
     */
    fun deserializeItem(base64: String?): ItemStack? {
        try {
            val input = ByteArrayInputStream(Base64Coder.decodeLines(base64))
            val dataIn = BukkitObjectInputStream(input)

            val item = dataIn.readObject() as ItemStack?

            dataIn.close()
            return item
        } catch (e: IOException) {
            throw RuntimeException("Failed to deserialize item", e)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Failed to deserialize item", e)
        }
    }

}