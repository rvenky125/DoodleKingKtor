package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(TYPE_DISCONNECT_REQUEST)
class DisconnectRequest: BaseModel()