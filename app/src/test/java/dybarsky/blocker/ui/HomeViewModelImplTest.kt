package dybarsky.blocker.ui

import dybarsky.blocker.testing.FakeNumberStore
import dybarsky.blocker.testing.MainDispatcherRule
import dybarsky.blocker.testing.number
import dybarsky.blocker.ui.screens.home.HomeState
import dybarsky.blocker.ui.screens.home.HomeViewModelImpl
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelImplTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val items = listOf(number(1, "111"), number(2, "222"))
    private val store = FakeNumberStore(items)

    @Test
    fun load_emitsContent() {
        assertEquals(HomeState.Content(items), HomeViewModelImpl(store).stateFlow.value)
    }

    @Test
    fun loadFailure_emitsError() {
        store.failLoad = true
        assertEquals(HomeState.Error, HomeViewModelImpl(store).stateFlow.value)
    }

    @Test
    fun retry_afterFailure_recovers() {
        store.failLoad = true
        val model = HomeViewModelImpl(store)
        store.failLoad = false
        model.retry()
        assertEquals(HomeState.Content(items), model.stateFlow.value)
    }

    @Test
    fun delete_removesNumber() {
        val model = HomeViewModelImpl(store)
        model.deleteNumber(items[0].id)
        assertEquals(HomeState.Content(listOf(items[1])), model.stateFlow.value)
    }

    @Test
    fun deleteFailure_keepsDataAndFlagsError() {
        val model = HomeViewModelImpl(store)
        store.failDelete = true
        model.deleteNumber(items[0].id)
        assertEquals(HomeState.Content(items, deleteFailed = true), model.stateFlow.value)
    }

    @Test
    fun dismissError_clearsFlag() {
        val model = HomeViewModelImpl(store)
        store.failDelete = true
        model.deleteNumber(items[0].id)
        model.dismissError()
        assertEquals(HomeState.Content(items, deleteFailed = false), model.stateFlow.value)
    }
}
