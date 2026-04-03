package com.picviewapp.data.repository

import android.content.Context
import android.os.Environment
import com.picviewapp.data.model.FolderInfo
import com.picviewapp.data.model.ImageInfo
import com.picviewapp.data.model.RecentFolder
import com.picviewapp.data.model.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")

    suspend fun getFoldersWithImages(rootPath: String? = null): List<FolderInfo> = withContext(Dispatchers.IO) {
        val root = rootPath?.let { File(it) } ?: Environment.getExternalStorageDirectory()
        val folders = mutableListOf<FolderInfo>()

        root.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val images = getImageFilesInDir(dir)
            if (images.isNotEmpty()) {
                folders.add(FolderInfo.fromFile(
                    file = dir,
                    imageCount = images.size,
                    coverPath = images.firstOrNull()?.absolutePath
                ))
            }
        }

        folders.sortedBy { it.name.lowercase() }
    }

    suspend fun getAllFolders(rootPath: String? = null): List<FolderInfo> = withContext(Dispatchers.IO) {
        val root = rootPath?.let { File(it) } ?: Environment.getExternalStorageDirectory()
        val folders = mutableListOf<FolderInfo>()

        collectFolders(root, folders)
        folders.sortedBy { it.name.lowercase() }
    }

    private fun collectFolders(dir: File, folders: MutableList<FolderInfo>) {
        val images = getImageFilesInDir(dir)
        if (images.isNotEmpty()) {
            folders.add(FolderInfo.fromFile(
                file = dir,
                imageCount = images.size,
                coverPath = images.firstOrNull()?.absolutePath
            ))
        }
        dir.listFiles()?.filter { it.isDirectory }?.forEach { subDir ->
            collectFolders(subDir, folders)
        }
    }

    suspend fun getImagesInFolder(folderPath: String, sortOrder: SortOrder = SortOrder.NAME_ASC): List<ImageInfo> = withContext(Dispatchers.IO) {
        val folder = File(folderPath)
        val images = folder.listFiles()
            ?.filter { it.isFile && isImageFile(it) }
            ?.map { ImageInfo.fromFile(it) }
            ?: emptyList()

        sortImages(images, sortOrder)
    }

    private fun sortImages(images: List<ImageInfo>, sortOrder: SortOrder): List<ImageInfo> {
        return when (sortOrder) {
            SortOrder.NAME_ASC -> images.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> images.sortedByDescending { it.name.lowercase() }
            SortOrder.DATE_ASC -> images.sortedBy { it.lastModified }
            SortOrder.DATE_DESC -> images.sortedByDescending { it.lastModified }
            SortOrder.SIZE_ASC -> images.sortedBy { it.size }
            SortOrder.SIZE_DESC -> images.sortedByDescending { it.size }
        }
    }

    suspend fun searchFolders(query: String): List<FolderInfo> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        val root = Environment.getExternalStorageDirectory()
        val results = mutableListOf<FolderInfo>()
        val lowerQuery = query.lowercase()

        searchFoldersRecursive(root, lowerQuery, results)
        results.take(100)
    }

    private fun searchFoldersRecursive(dir: File, query: String, results: MutableList<FolderInfo>) {
        if (results.size >= 100) return
        
        val images = getImageFilesInDir(dir)
        if (dir.name.lowercase().contains(query)) {
            results.add(FolderInfo.fromFile(
                file = dir,
                imageCount = images.size,
                coverPath = images.firstOrNull()?.absolutePath
            ))
        }

        dir.listFiles()?.filter { it.isDirectory }?.forEach { subDir ->
            searchFoldersRecursive(subDir, query, results)
        }
    }

    private fun isImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in imageExtensions
    }

    private fun getImageFilesInDir(dir: File): List<File> {
        return dir.listFiles()?.filter { it.isFile && isImageFile(it) } ?: emptyList()
    }
}
