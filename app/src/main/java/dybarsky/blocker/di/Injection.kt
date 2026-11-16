package dybarsky.blocker.di

import android.content.Context
import dybarsky.blocker.common.components.CallLogStore
import dybarsky.blocker.common.components.Matcher
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.domain.MatcherImpl
import dybarsky.blocker.store.CallLogStoreImpl
import dybarsky.blocker.store.NumberStoreImpl

class Injection(context: Context) {
    private val store: NumberStore by lazy { NumberStoreImpl(context) }
    private val callLog: CallLogStore by lazy { CallLogStoreImpl(context) }
    private val matcher: Matcher by lazy { MatcherImpl() }

    val viewModelFactory = viewModelFactory(store, callLog)
    val serviceModelFactory = serviceModelFactory(store, callLog, matcher)
}
