package com.picviewapp.domain.usecase

import com.picviewapp.data.model.FolderInfo
import com.picviewapp.data.model.ImageInfo
import com.picviewapp.data.model.SortOrder
import com.picviewapp.data.repository.FileRepository
import javax.inject.Inject

class GetFoldersUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(): List<FolderInfo> {
        return fileRepository.getAllFolders()
    }
}

class GetImagesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(folderPath: String, sortOrder: SortOrder = SortOrder.NAME_ASC): List<ImageInfo> {
        return fileRepository.getImagesInFolder(folderPath, sortOrder)
    }
}

class SearchFoldersUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(query: String): List<FolderInfo> {
        return fileRepository.searchFolders(query)
    }
}
