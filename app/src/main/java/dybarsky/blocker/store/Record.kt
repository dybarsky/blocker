package dybarsky.blocker.store

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Record(

    @SerialName("id")
    val id: Long,

    @SerialName("mask")
    val mask: String
)
