package com.picviewapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.picviewapp.data.model.RecentFolder
import com.picviewapp.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _recentFolders = MutableStateFlow<List<RecentFolder>>(emptyList())
    val recentFolders: StateFlow<List<RecentFolder>> = _recentFolders.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.recentFolders.collect { folders ->
                _recentFolders.value = folders
            }
        }
    }

    fun removeRecentFolder(path: String) {
        viewModelScope.launch {
            preferencesRepository.removeRecentFolder(path)
        }
    }
}
