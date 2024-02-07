package com.famas.util

val programmers_words = readWordList("telugu_movies.txt")
//val programmers_words = readWordList("programmers_wordlist.txt")
val common_words = readWordList("wordlist.txt")
val words = (programmers_words + common_words).shuffled()

fun readWordList(fileName: String): List<String> {
    val inputStream = object {}.javaClass.classLoader.getResourceAsStream(fileName)
        ?: throw IllegalArgumentException("File not found: $fileName")
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
    return toCharArray().mapIndexed { index, it ->
        if (index == 0) it else if (index == length - 1 && length > 4) it else if (it != ' ') '_' else ' '
    }.joinToString(" ")
}