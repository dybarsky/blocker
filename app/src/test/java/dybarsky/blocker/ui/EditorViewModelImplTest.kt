package dybarsky.blocker.ui

import dybarsky.blocker.common.models.Number
import dybarsky.blocker.testing.FakeNumberStore
import dybarsky.blocker.testing.MainDispatcherRule
import dybarsky.blocker.testing.number
import dybarsky.blocker.ui.screens.edit.EditorState
import dybarsky.blocker.ui.screens.edit.EditorViewModelImpl
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class EditorViewModelImplTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val store = FakeNumberStore(listOf(number(1, "111")))

    @Test
    fun newNumber_startsWithEmptyContent() {
        val model = EditorViewModelImpl(null, store)
        assertEquals(EditorState.Content(null), model.stateFlow.value)
    }

    @Test
    fun existingNumber_loadsMask() {
        val model = EditorViewModelImpl(Number.Id(1), store)
        assertEquals(EditorState.Content("111"), model.stateFlow.value)
    }

    @Test
    fun unknownId_emitsError() {
        val model = EditorViewModelImpl(Number.Id(99), store)
        assertEquals(EditorState.Error, model.stateFlow.value)
    }

    @Test
    fun loadFailure_emitsError() {
        store.failLoad = true
        val model = EditorViewModelImpl(Number.Id(1), store)
        assertEquals(EditorState.Error, model.stateFlow.value)
    }

    @Test
    fun save_persistsAndEmitsSaved() {
        val model = EditorViewModelImpl(null, store)
        model.saveNumber("222")
        assertEquals(EditorState.Saved, model.stateFlow.value)
        assertEquals(listOf("111", "222"), store.numbers.value.map { it.mask })
    }

    @Test
    fun saveExisting_updatesMask() {
        val model = EditorViewModelImpl(Number.Id(1), store)
        model.saveNumber("999")
        assertEquals(listOf("999"), store.numbers.value.map { it.mask })
    }

    @Test
    fun blankMask_closesWithoutWriting() {
        val model = EditorViewModelImpl(null, store)
        model.saveNumber("   ")
        assertEquals(EditorState.Saved, model.stateFlow.value)
        assertEquals(0, store.saveCalls)
    }

    @Test
    fun saveFailure_staysOpenWithError() {
        val model = EditorViewModelImpl(null, store)
        store.failSave = true
        model.saveNumber("222")
        assertEquals(EditorState.Content(null, saveFailed = true), model.stateFlow.value)
    }

    @Test
    fun retryAfterSaveFailure_succeeds() {
        val model = EditorViewModelImpl(null, store)
        store.failSave = true
        model.saveNumber("222")
        store.failSave = false
        model.saveNumber("222")
        assertEquals(EditorState.Saved, model.stateFlow.value)
    }
}
