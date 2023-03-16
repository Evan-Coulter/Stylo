package com.example.stylo.util

object ColorStringMap {
    private val colorMap = hashMapOf(
        "Yellow" to listOf("#FCDF4B", "#FFED94"),
        "Pink" to listOf("#E8736A", "#FFC4BF"),
        "Blue" to listOf("#6699CC", "#8EAECF"),
        "Grey" to listOf("#50514F", "#B3B9AD"),
        "Green" to listOf("#59FFA0", "#9CFFC6")
    )
    fun getColor(string: String) : String {
        return colorMap[string]?.get(0) ?: colorMap["Pink"]!![0]
    }
    fun getLightColor(string: String) : String {
        return colorMap[string]?.get(1) ?: colorMap["Pink"]!![1]
    }
}