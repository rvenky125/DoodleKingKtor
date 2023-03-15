package com.famas.data.models

data class NewWords(
    val newWords: List<String>
): BaseModel(TYPE_NEW_WORDS)
