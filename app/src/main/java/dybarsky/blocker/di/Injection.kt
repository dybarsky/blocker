package dybarsky.blocker.di

import android.content.Context
import dybarsky.blocker.common.components.Matcher
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.domain.MatcherImpl
import dybarsky.blocker.store.NumberStoreImpl

class Injection(context: Context) {
    private val store: NumberStore by lazy { NumberStoreImpl(context) }
    private val matcher: Matcher by lazy { MatcherImpl() }

    val viewModelFactory = viewModelFactory(store)
    val serviceModelFactory = serviceModelFactory(store, matcher)
}
