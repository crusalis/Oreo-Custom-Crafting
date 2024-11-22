package org.oreo.oreosCustomCrafting.utils

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.oreo.oreosCustomCrafting.data.CustomRecipeData
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.*

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


    // Serialize an object to a byte array
    fun serializeGroups(obj: Any): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(obj)
        objectOutputStream.flush()
        return byteArrayOutputStream.toByteArray()
    }

    // Deserialize a byte array back to an object
    fun deserializeGroups(byteArray: ByteArray): HashMap<String, Pair<ItemStack, ArrayList<CustomRecipeData>>> {
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        @Suppress("UNCHECKED_CAST")
        return objectInputStream.readObject() as HashMap<String, Pair<ItemStack, ArrayList<CustomRecipeData>>>
    }


}