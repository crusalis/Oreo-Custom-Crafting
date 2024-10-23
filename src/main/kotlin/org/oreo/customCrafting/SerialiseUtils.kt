package org.oreo.customCrafting

import org.bukkit.inventory.ItemStack
import java.io.*
import java.util.*


object SerialiseUtils {

    // Serialize ItemStack to Base64 string
    fun serializeItemStack(itemStack: ItemStack?): String {
        try {
            val out = ByteArrayOutputStream()
            val dataOut = ObjectOutputStream(out)

            // Write the ItemStack object to the stream
            dataOut.writeObject(itemStack)

            dataOut.close()
            return Base64.getEncoder().encodeToString(out.toByteArray())
        } catch (e: IOException) {
            throw RuntimeException("Failed to serialize item stack", e)
        }
    }

    // Deserialize base64 string to ItemStack
    fun deSerializeItemStack(base64: String?): ItemStack? {

        return try {
            val input = ByteArrayInputStream(Base64.getDecoder().decode(base64))
            val dataIn = ObjectInputStream(input)

            val itemStack = dataIn.readObject() as ItemStack

            dataIn.close()
            itemStack
        } catch (e: IOException) {
            throw RuntimeException("Failed to deserialize item stack", e)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("Failed to deserialize item stack", e)
        }
    }

}