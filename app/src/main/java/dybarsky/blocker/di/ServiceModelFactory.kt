package dybarsky.blocker.di

import dybarsky.blocker.common.components.Matcher
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.service.BlockerServiceModel
import dybarsky.blocker.service.BlockerServiceModelImpl

interface ServiceModelFactory {
    fun <T> obtain(type: Class<T>): T
}

fun serviceModelFactory(
    store: NumberStore,
    matcher: Matcher
) = object : ServiceModelFactory {

    override fun <T> obtain(type: Class<T>): T {
        when (type) {
            BlockerServiceModel::class.java -> {
                return BlockerServiceModelImpl(
                    store = store,
                    matcher = matcher
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ServiceModel class: $type")
        }
    }
}
