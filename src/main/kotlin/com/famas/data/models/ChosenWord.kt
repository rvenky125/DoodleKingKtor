package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChosenWord(
    val chosenWord: String,
    @SerialName("room_id")
    val roomId: String
): BaseModel(TYPE_CHOSEN_WORD)
