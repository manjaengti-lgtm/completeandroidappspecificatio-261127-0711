package com.arin.tube18.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arin.tube18.data.prefs.AgePrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val agePrefs: AgePrefs) : ViewModel() {

    /** null = still reading storage, false = show gate, true = app unlocked. */
    val ageAccepted: StateFlow<Boolean?> = agePrefs.accepted
        .map<Boolean, Boolean?> { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun accept() {
        viewModelScope.launch { agePrefs.setAccepted(true) }
    }
}
