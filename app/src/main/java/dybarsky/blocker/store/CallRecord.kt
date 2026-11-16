package dybarsky.blocker.store

import dybarsky.blocker.common.models.BlockedCall
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CallRecord(

    @SerialName("number")
    val number: String,

    @SerialName("mask")
    val mask: String,

    @SerialName("timestamp")
    val timestamp: Long,
)

fun CallRecord.asBlockedCall() = BlockedCall(number = number, mask = mask, timestamp = timestamp)

fun BlockedCall.asRecord() = CallRecord(number = number, mask = mask, timestamp = timestamp)

/** Appends [item] and keeps only the newest [limit] elements (list is oldest → newest). */
fun <T> List<T>.plusCapped(item: T, limit: Int): List<T> = (this + item).takeLast(limit)
