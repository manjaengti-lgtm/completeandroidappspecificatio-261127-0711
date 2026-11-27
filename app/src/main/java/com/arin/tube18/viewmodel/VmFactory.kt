package com.arin.tube18.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.arin.tube18.AppContainer

object VmFactory {
    fun main(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { MainViewModel(c.agePrefs) }
    }

    fun home(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { HomeViewModel(c.videos) }
    }

    fun search(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { SearchViewModel(c.videos) }
    }

    fun detail(c: AppContainer, id: String): ViewModelProvider.Factory = viewModelFactory {
        initializer { DetailViewModel(id, c.videos, c.saved) }
    }

    fun player(c: AppContainer, id: String): ViewModelProvider.Factory = viewModelFactory {
        initializer { PlayerViewModel(id, c.videos, c.saved) }
    }

    fun saved(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { SavedViewModel(c.saved) }
    }

    fun settings(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { SettingsViewModel(c.saved, c.agePrefs) }
    }
}
