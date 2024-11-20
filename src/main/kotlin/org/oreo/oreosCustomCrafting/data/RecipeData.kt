package org.oreo.oreosCustomCrafting.data

import org.bukkit.Material

abstract class RecipeData(
    ){
    abstract val name: String
    abstract val fileResult : String?
    abstract val materialResult: Material?
    abstract val amount: Int
}
