package dybarsky.blocker.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dybarsky.blocker.common.components.NumberStore
import dybarsky.blocker.ui.screens.edit.EditorViewModel
import dybarsky.blocker.ui.screens.edit.EditorViewModelImpl
import dybarsky.blocker.ui.screens.home.HomeViewModel
import dybarsky.blocker.ui.screens.home.HomeViewModelImpl

fun viewModelFactory(store: NumberStore) = viewModelFactory {
    initializer<HomeViewModel> {
        HomeViewModelImpl(store = store)
    }
    initializer<EditorViewModel> {
        val id = get(EditorViewModel.idKey)
        EditorViewModelImpl(id = id, store = store)
    }
}
