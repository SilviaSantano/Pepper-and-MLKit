package de.inovex.pepper.intelligence.mlkit.utils

enum class Mode(val string: String) {
    DRAWING("zxx-Zsym-x-autodraw"), ENGLISH("en"), GERMAN("de"), SPANISH("es")
}

enum class Language {
    ENGLISH, SPANISH, GERMAN, OTHER
}

var languageMap: Map<String, Language> = mapOf(
    "englisch" to Language.ENGLISH,
    "inglés" to Language.ENGLISH,
    "german" to Language.GERMAN,
    "alemán" to Language.GERMAN,
    "spanish" to Language.SPANISH,
    "spanisch" to Language.SPANISH
).withDefault { Language.OTHER }

const val HEIGHT = 512
const val WIDTH = 512
const val MAX_RESULT_DISPLAY = 10
