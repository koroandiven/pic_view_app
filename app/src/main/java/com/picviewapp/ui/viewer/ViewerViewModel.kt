package com.picviewapp.ui.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.picviewapp.data.model.ImageInfo
import com.picviewapp.domain.usecase.GetImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val getImagesUseCase: GetImagesUseCase
) : ViewModel() {

    private val _images = MutableStateFlow<List<ImageInfo>>(emptyList())
    val images: StateFlow<List<ImageInfo>> = _images.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadImages(folderPath: String, initialIndex: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getImagesUseCase(folderPath)
            _images.value = result
            _currentIndex.value = initialIndex.coerceIn(0, result.size - 1)
            _isLoading.value = false
        }
    }

    fun setCurrentIndex(index: Int) {
        _currentIndex.value = index.coerceIn(0, _images.value.size - 1)
    }
}
