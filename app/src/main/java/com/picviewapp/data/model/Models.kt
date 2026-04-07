package com.picviewapp.data.model

import android.os.Environment
import java.io.File

data class FolderInfo(
    val path: String,
    val name: String,
    val imageCount: Int,
    val lastModified: Long,
    val coverImagePath: String? = null
) {
    companion object {
        fun fromFile(file: File, imageCount: Int, coverPath: String? = null): FolderInfo {
            return FolderInfo(
                path = file.absolutePath,
                name = file.name,
                imageCount = imageCount,
                lastModified = file.lastModified(),
                coverImagePath = coverPath
            )
        }
    }
}

data class ImageInfo(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val width: Int = 0,
    val height: Int = 0
) {
    companion object {
        fun fromFile(file: File): ImageInfo {
            return ImageInfo(
                path = file.absolutePath,
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified()
            )
        }
    }
}

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    DATE_ASC,
    DATE_DESC,
    SIZE_ASC,
    SIZE_DESC
}

data class RecentFolder(
    val path: String,
    val name: String,
    val lastAccessed: Long,
    val accessCount: Int = 1,
    val uri: String? = null
)
