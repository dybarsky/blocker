package dybarsky.blocker.ui

import dybarsky.blocker.testing.FakeCallLogStore
import dybarsky.blocker.testing.MainDispatcherRule
import dybarsky.blocker.testing.call
import dybarsky.blocker.ui.screens.log.LogState
import dybarsky.blocker.ui.screens.log.LogViewModelImpl
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LogViewModelImplTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val items = listOf(call("+4322", timestamp = 2), call("+4311", timestamp = 1))
    private val store = FakeCallLogStore(items)

    @Test
    fun load_emitsContentInStoreOrder() {
        assertEquals(LogState.Content(items), LogViewModelImpl(store).stateFlow.value)
    }

    @Test
    fun emptyLog_emitsEmptyContent() {
        val model = LogViewModelImpl(FakeCallLogStore())
        assertEquals(LogState.Content(emptyList()), model.stateFlow.value)
    }

    @Test
    fun newCall_appearsAtTop() {
        val model = LogViewModelImpl(store)
        val newest = call("+4333", timestamp = 3)
        store.calls.value = listOf(newest) + items
        assertEquals(LogState.Content(listOf(newest) + items), model.stateFlow.value)
    }

    @Test
    fun loadFailure_emitsError_thenRetryRecovers() {
        store.failLoad = true
        val model = LogViewModelImpl(store)
        assertEquals(LogState.Error, model.stateFlow.value)
        store.failLoad = false
        model.retry()
        assertEquals(LogState.Content(items), model.stateFlow.value)
    }

    @Test
    fun clear_emptiesLog() {
        val model = LogViewModelImpl(store)
        model.clear()
        assertEquals(LogState.Content(emptyList()), model.stateFlow.value)
    }

    @Test
    fun clearFailure_keepsItemsAndFlagsError() {
        val model = LogViewModelImpl(store)
        store.failClear = true
        model.clear()
        assertEquals(LogState.Content(items, clearFailed = true), model.stateFlow.value)
    }

    @Test
    fun dismissError_clearsFlag() {
        val model = LogViewModelImpl(store)
        store.failClear = true
        model.clear()
        model.dismissError()
        assertEquals(LogState.Content(items, clearFailed = false), model.stateFlow.value)
    }
}
