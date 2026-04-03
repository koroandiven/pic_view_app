package com.picviewapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import com.picviewapp.ui.PicViewNavHost
import com.picviewapp.ui.theme.PicViewTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var selectedFolderUri by mutableStateOf<Uri?>(null)

    private val openFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            selectedFolderUri = it
            val path = getPathFromUri(it)
            if (path != null) {
                navigateToFolder(path)
            }
        }
    }

    fun openFolderPicker() {
        openFolderLauncher.launch(null)
    }

    private fun getPathFromUri(uri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        val type = split.getOrNull(0)
        val relativePath = split.getOrNull(1) ?: ""

        return when {
            type == "primary" -> "${Environment.getExternalStorageDirectory()}/$relativePath"
            type == "home" -> "${Environment.getExternalStorageDirectory()}/$relativePath"
            type == "raw:/" -> relativePath
            relativePath.isNotEmpty() -> "/storage/$type/$relativePath"
            else -> "/storage/$type"
        }
    }

    private fun navigateToFolder(path: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("folder_path", path)
        }
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PicViewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val launcher = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocumentTree()
                    ) { uri ->
                        uri?.let {
                            selectedFolderUri = it
                            val path = getPathFromUri(it)
                            if (path != null) {
                                val newIntent = Intent(this, MainActivity::class.java).apply {
                                    action = Intent.ACTION_VIEW
                                    putExtra("folder_path", path)
                                }
                                startActivity(newIntent)
                            }
                        }
                    }

                    PicViewNavHost(
                        onOpenFolderPicker = { launcher.launch(null) }
                    )
                }
            }
        }
    }
}