package com.seyone22.cook.parser

fun parseItemString(input: String):  Triple<String, Int, String> {
    val regex = Regex("""(\d+)\s*([a-zA-Z]+)\s*(.+)?|(.+?)\s*(\d+)\s*([a-zA-Z]+)?""")

    val matchResult = regex.find(input)

    if (matchResult != null) {
        val groups = matchResult.groupValues

        // Check which of the two patterns matched (20kg potatoes || potatoes 20kg)
        return if (groups[1].isNotBlank() && groups[2].isNotBlank()) {
            // Format: Quantity + Unit + Item Name
            val quantity = groups[1].toIntOrNull()
            val unit = groups[2].trim()
            val itemName = groups[3].trim() ?: throw IllegalArgumentException("Item name is missing")

            if (quantity != null)
                Triple(itemName, quantity, unit) else throw IllegalArgumentException("Quantity is missing")
        } else if (groups[4].isNotBlank() && groups[5].isNotBlank()) {
            // Format: Item Name + Quantity + Unit
            val itemName = groups[4].trim()
            val quantity = groups[5].toIntOrNull()
            val unit = groups[6].trim() ?: throw IllegalArgumentException("Unit is missing")

            if (quantity != null)
                Triple(itemName, quantity, unit) else throw IllegalArgumentException("Quantity is missing")
        } else {
            throw IllegalArgumentException("Invalid input format")

        }
    }
    throw IllegalArgumentException("Unable to parse input")
}