package com.example.fruitrek.data.model

data class Fruit(
    val id           : String,   // must match label in labels.txt exactly
    val englishName  : String,
    val filipinoName : String,
    val baybayin     : String,   // Unicode Baybayin characters
    val emoji        : String,
    val funFact      : String,
    val color        : Long,     // ARGB hex for card theming
    val isUnlocked   : Boolean = false
)

/** Master catalog — add more fruits as your model supports them. */
object FruitCatalog {

    val all: List<Fruit> = listOf(
        Fruit(
            id           = "mango",
            englishName  = "Mango",
            filipinoName = "Mangga",
            baybayin     = "ᜋᜅ᜔ᜄ",
            emoji        = "🥭",
            funFact      = "Mangoes are called the 'King of Fruits'! " +
                           "One mango gives you all the Vitamin C you need for a whole day! 👑",
            color        = 0xFFFFA726
        ),
        Fruit(
            id           = "banana",
            englishName  = "Banana",
            filipinoName = "Saging",
            baybayin     = "ᜐᜄᜒᜅ᜔",
            emoji        = "🍌",
            funFact      = "Bananas give you super quick energy before playing! " +
                           "Athletes eat them during games for a reason! ⚡",
            color        = 0xFFFFEB3B
        ),
        Fruit(
            id           = "pineapple",
            englishName  = "Pineapple",
            filipinoName = "Pinya",
            baybayin     = "ᜉᜒᜈ᜔ᜌ",
            emoji        = "🍍",
            funFact      = "It takes almost 2 years for one pineapple to grow! " +
                           "Farmers must be very, very patient! 🌱",
            color        = 0xFFFFD54F
        ),
        Fruit(
            id           = "papaya",
            englishName  = "Papaya",
            filipinoName = "Papaya",
            baybayin     = "ᜉᜉᜌ",
            emoji        = "🟠",
            funFact      = "Papaya has a special ingredient that helps your tummy " +
                           "digest food — like having a tiny helper inside! 💪",
            color        = 0xFFFF8A65
        ),
        Fruit(
            id           = "coconut",
            englishName  = "Coconut",
            filipinoName = "Niyog",
            baybayin     = "ᜈᜒᜌᜓᜄ᜔",
            emoji        = "🥥",
            funFact      = "The coconut tree is called the 'Tree of Life' in the Philippines " +
                           "because almost every part of it can be used! 🌴",
            color        = 0xFF8D6E63
        ),
        Fruit(
            id           = "jackfruit",
            englishName  = "Jackfruit",
            filipinoName = "Langka",
            baybayin     = "ᜎᜅ᜔ᜃ",
            emoji        = "🟡",
            funFact      = "Jackfruit is the BIGGEST fruit that grows on trees — " +
                           "it can weigh as much as a 7-year-old child! 😲",
            color        = 0xFFAED581
        ),
        Fruit(
            id           = "guava",
            englishName  = "Guava",
            filipinoName = "Bayabas",
            baybayin     = "ᜊᜌᜊᜐ᜔",
            emoji        = "🟢",
            funFact      = "One guava has 4× more Vitamin C than an orange! " +
                           "Guava leaves are also used as traditional Filipino medicine! 🌿",
            color        = 0xFF81C784
        ),
        Fruit(
            id           = "starfruit",
            englishName  = "Starfruit",
            filipinoName = "Balimbing",
            baybayin     = "ᜊᜎᜒᜋ᜔ᜊᜒᜅ᜔",
            emoji        = "⭐",
            funFact      = "When you slice a starfruit sideways, each piece looks " +
                           "exactly like a star — nature's own decoration! 🌟",
            color        = 0xFFFFF176
        )
    )

    fun findById(id: String): Fruit? = all.firstOrNull { it.id == id }
}
