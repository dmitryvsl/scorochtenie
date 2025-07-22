package com.example.scorochenie.domain

public enum class TechniqueType(val displayName: String) {
    BlockReading("Чтение блоками"),
    DiagonalReading("Чтение по диагонали"),
    KeywordSearch("Поиск ключевых слов"),
    PointerMethod("Метод указки"),
    SentenceReverse("Предложения наоборот"),
    WordReverse("Слова наоборот"),
}