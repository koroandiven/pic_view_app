package com.picviewapp.ui.browser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.picviewapp.data.model.ImageInfo
import com.picviewapp.data.model.SortOrder
import com.picviewapp.data.repository.PreferencesRepository
import com.picviewapp.domain.usecase.GetImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val getImagesUseCase: GetImagesUseCase,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _images = MutableStateFlow<List<ImageInfo>>(emptyList())
    val images: StateFlow<List<ImageInfo>> = _images.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _showSortMenu = MutableStateFlow(false)
    val showSortMenu: StateFlow<Boolean> = _showSortMenu.asStateFlow()

    private var currentFolderPath: String = ""
    private var currentFolderName: String = ""

    fun loadFolder(folderPath: String) {
        currentFolderPath = folderPath
        currentFolderName = File(folderPath).name
        viewModelScope.launch {
            _isLoading.value = true
            preferencesRepository.addRecentFolder(folderPath, currentFolderName)
            val result = getImagesUseCase(folderPath, _sortOrder.value)
            _images.value = result
            _isLoading.value = false
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _showSortMenu.value = false
        viewModelScope.launch {
            _isLoading.value = true
            val result = getImagesUseCase(currentFolderPath, order)
            _images.value = result
            _isLoading.value = false
        }
    }

    fun toggleSortMenu() {
        _showSortMenu.value = !_showSortMenu.value
    }

    fun dismissSortMenu() {
        _showSortMenu.value = false
    }
}
