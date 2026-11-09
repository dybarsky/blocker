package dybarsky.blocker.store

import dybarsky.blocker.common.models.Number
import dybarsky.blocker.common.models.Number.Id.Companion.New
import java.lang.System.currentTimeMillis

fun Record.asNumber(): Number {
    return Number(id = Number.Id(id), mask = mask)
}

fun Number.asRecord(): Record {
    val id = if (id == New) currentTimeMillis() else id.value
    return Record(id = id, mask = mask)
}
