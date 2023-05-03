package com.famas.util

import java.io.File

val programmers_words = readWordList("programmers_wordlist.txt")
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


fun String.transformToUnderscores() =
    toCharArray().map {
        if(it != ' ') '_' else ' '
    }.joinToString(" ")