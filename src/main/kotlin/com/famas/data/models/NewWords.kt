package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(TYPE_NEW_WORDS)
data class NewWords(
    @SerialName("new_words")
    val newWords: List<String>
): BaseModel()
