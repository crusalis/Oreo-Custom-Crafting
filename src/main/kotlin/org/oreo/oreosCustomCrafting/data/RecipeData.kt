package org.oreo.oreosCustomCrafting.data

import org.bukkit.Material

abstract class RecipeData(
    open val name: String,
    open val fileResult : String?,
    open val materialResult: Material?,
    open val amount: Int)
