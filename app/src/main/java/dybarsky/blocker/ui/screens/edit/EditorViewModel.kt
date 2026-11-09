package dybarsky.blocker.ui.screens.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras.Companion.Key
import androidx.lifecycle.viewmodel.MutableCreationExtras
import dybarsky.blocker.common.models.Number
import kotlinx.coroutines.flow.StateFlow

abstract class EditorViewModel : ViewModel() {
    abstract val stateFlow: StateFlow<EditorState>
    abstract fun saveNumber(mask: String)

    companion object {
        val idKey = Key<Number.Id?>()
        fun extras(id: Number.Id?) = MutableCreationExtras().apply { set(idKey, id) }
    }
}
