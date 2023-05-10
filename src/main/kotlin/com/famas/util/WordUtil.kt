package com.famas.util

import java.io.File

val programmers_words = readWordList("mywords.txt")
val common_words = readWordList("wordlist.txt")
val words = (programmers_words + common_words).shuffled()

fun readWordList(fileName: String): List<String> {
    val classLoader = object {}.javaClass.classLoader
    val file = classLoader.getResource(fileName)?.file ?: throw IllegalArgumentException("File not found: $fileName")
    val inputStream = File(file).inputStream()
    val words = mutableListOf<String>()
    inputStream.bufferedReader().forEachLine {
        words.add(it)
    }
    return words
}

fun getRandomWords(amount: Int): List<String> {
    println(words)
    var curAmount = 0
    val result = mutableListOf<String>()
    while (curAmount < amount) {
        val word = words.random()
        if (!result.contains(word)) {
            result.add(word)
            curAmount++
        }
    }
    return result
}


fun List<Int>.getTwoDistinctRandomIntegers(): List<Int> {
    val randomIntegers = mutableListOf<Int>()
    while (randomIntegers.size < 2) {
        val randomInt = this.random()
        if (!randomIntegers.contains(randomInt)) {
            randomIntegers.add(randomInt)
        }
    }

    return randomIntegers
}

fun String.transformToUnderscores(): String {
//    fun letterIndicesToShow(string: String) =
//        if (string.length < 4) listOf(string.indices.random()) else string.indices.toList().getTwoDistinctRandomIntegers()
//
//    val wordsInString = split(" ")
//    val letterIndicesToShowInEachWord = wordsInString.map { word -> letterIndicesToShow(word) }
//    println(letterIndicesToShowInEachWord)

//    val resultListOfWords = wordsInString.mapIndexed { wordIndex, word ->
//        val indicesToShow = letterIndicesToShowInEachWord[wordIndex]
//        word.toCharArray()
//            .mapIndexed { letterIndex, letter ->
//                if (indicesToShow.contains(letterIndex)) '_' else letter
//            }
//    }
//
//    return resultListOfWords.joinToString(" ")

    return toCharArray().mapIndexed { index, it ->
        if (index == 0) it else if (index == length - 1 && length > 4) it else if (it != ' ') '_' else ' '
    }.joinToString(" ")
}