package com.picviewapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.picviewapp.data.model.RecentFolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recent_folders")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recentFoldersKey = stringPreferencesKey("recent_folders")

    val recentFolders: Flow<List<RecentFolder>> = context.dataStore.data.map { preferences ->
        val json = preferences[recentFoldersKey] ?: "[]"
        parseRecentFolders(json)
    }

    suspend fun addRecentFolder(path: String, name: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[recentFoldersKey] ?: "[]"
            val folders = parseRecentFolders(currentJson).toMutableList()

            val existing = folders.find { it.path == path }
            if (existing != null) {
                folders.remove(existing)
                folders.add(0, existing.copy(
                    lastAccessed = System.currentTimeMillis(),
                    accessCount = existing.accessCount + 1
                ))
            } else {
                folders.add(0, RecentFolder(
                    path = path,
                    name = name,
                    lastAccessed = System.currentTimeMillis(),
                    accessCount = 1
                ))
            }

            val trimmed = folders.take(10)
            preferences[recentFoldersKey] = serializeRecentFolders(trimmed)
        }
    }

    suspend fun removeRecentFolder(path: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[recentFoldersKey] ?: "[]"
            val folders = parseRecentFolders(currentJson).filter { it.path != path }
            preferences[recentFoldersKey] = serializeRecentFolders(folders)
        }
    }

    private fun parseRecentFolders(json: String): List<RecentFolder> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                RecentFolder(
                    path = obj.getString("path"),
                    name = obj.getString("name"),
                    lastAccessed = obj.getLong("lastAccessed"),
                    accessCount = obj.optInt("accessCount", 1)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeRecentFolders(folders: List<RecentFolder>): String {
        val array = JSONArray()
        folders.forEach { folder ->
            val obj = JSONObject().apply {
                put("path", folder.path)
                put("name", folder.name)
                put("lastAccessed", folder.lastAccessed)
                put("accessCount", folder.accessCount)
            }
            array.put(obj)
        }
        return array.toString()
    }
}
