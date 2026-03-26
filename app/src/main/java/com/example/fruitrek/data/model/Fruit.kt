package com.example.fruitrek.data.model

import androidx.compose.ui.graphics.Color

data class Fruit(
    val id: String,
    val englishName: String, // Restored for UI compilation
    val filipinoName: String,
    val baybayin: String,
    val funFact: String,
    val emoji: String,
    val color: Color // Restored for Jetpack Compose overlays
)

object FruitCatalog {
    // Restored the 'all' variable name expected by your ViewModel
    val all = listOf(
        Fruit(
            id = "mango",
            englishName = "Mango",
            filipinoName = "Mangga",
            baybayin = "ᜋᜅ᜔ᜄ",
            funFact = "It's the national fruit of the Philippines! It is super sweet and shaped like a golden heart.",
            emoji = "🥭",
            color = Color(0xFFFFC107) // Amber
        ),
        Fruit(
            id = "banana",
            englishName = "Banana",
            filipinoName = "Saging",
            baybayin = "ᜐᜄᜒᜅ᜔",
            funFact = "It comes in its own yellow wrapper and gives you lots of energy to run and play!",
            emoji = "🍌",
            color = Color(0xFFFFEB3B) // Yellow
        ),
        Fruit(
            id = "pineapple",
            englishName = "Pineapple",
            filipinoName = "Pinya",
            baybayin = "ᜉᜒᜈ᜔ᜌ",
            funFact = "It wears a spiky crown on its head, but inside it is super juicy and refreshing.",
            emoji = "🍍",
            color = Color(0xFFFF9800) // Orange
        ),
        Fruit(
            id = "papaya",
            englishName = "Papaya",
            filipinoName = "Papaya",
            baybayin = "ᜉᜉᜌ",
            funFact = "It has magic black seeds inside! Eating it is great for your tummy and makes your skin glow.",
            emoji = "🍈",
            color = Color(0xFFFF5722) // Deep Orange
        ),
        Fruit(
            id = "coconut",
            englishName = "Coconut",
            filipinoName = "Buko",
            baybayin = "ᜊᜓᜃᜓ",
            funFact = "Known as the 'Tree of Life'. You can drink its water, eat its meat, and even build houses with its wood!",
            emoji = "🥥",
            color = Color(0xFF795548) // Brown
        ),
        Fruit(
            id = "jackfruit",
            englishName = "Jackfruit",
            filipinoName = "Langka",
            baybayin = "ᜎᜅ᜔ᜃ",
            funFact = "It is the largest tree-borne fruit in the world! When it's ripe, it smells just like sweet bubblegum.",
            emoji = "🍈",
            color = Color(0xFFCDDC39) // Lime
        ),
        Fruit(
            id = "guava",
            englishName = "Guava",
            filipinoName = "Bayabas",
            baybayin = "ᜊᜌᜊᜐ᜔",
            funFact = "A super fruit! Eating just one tiny guava gives you way more Vitamin C than a whole orange.",
            emoji = "🍏",
            color = Color(0xFF4CAF50) // Green
        )
    )

    // Restored the exact function signature expected by the UI
    fun findById(id: String): Fruit? {
        return all.find { it.id.lowercase() == id.lowercase() }
    }
}