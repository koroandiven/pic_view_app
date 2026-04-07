package com.picviewapp.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.picviewapp.ui.browser.FolderBrowserScreen
import com.picviewapp.ui.home.HomeScreen
import com.picviewapp.ui.search.SearchScreen
import com.picviewapp.ui.viewer.ImageViewerScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BROWSER = "browser/{folderPath}"
    const val VIEWER = "viewer/{folderPath}/{imageIndex}"

    fun browser(folderPath: String): String {
        val encoded = if (folderPath.startsWith("content://")) {
            android.util.Base64.encodeToString(folderPath.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        } else {
            URLEncoder.encode(folderPath, StandardCharsets.UTF_8.toString())
        }
        return "browser/$encoded"
    }
    
    fun viewer(folderPath: String, imageIndex: Int): String {
        val encoded = if (folderPath.startsWith("content://")) {
            android.util.Base64.encodeToString(folderPath.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        } else {
            URLEncoder.encode(folderPath, StandardCharsets.UTF_8.toString())
        }
        return "viewer/$encoded/$imageIndex"
    }
}

@Composable
fun PicViewNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    var pendingFolderPath by remember { mutableStateOf<String?>(null) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: SecurityException) {
                try {
                    context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e2: SecurityException) {
                }
            }
            pendingFolderPath = it.toString()
        }
    }

    LaunchedEffect(pendingFolderPath) {
        pendingFolderPath?.let { path ->
            pendingFolderPath = null
            navController.navigate(Routes.browser(path)) {
                popUpTo(Routes.HOME)
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onFolderClick = { folderPath ->
                    navController.navigate(Routes.browser(folderPath))
                },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                },
                onOpenFolderPicker = {
                    folderPickerLauncher.launch(null)
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onFolderClick = { folderPath ->
                    navController.navigate(Routes.browser(folderPath))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.BROWSER,
            arguments = listOf(navArgument("folderPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("folderPath") ?: ""
            val folderPath = if (encodedPath.length > 20) {
                try {
                    String(android.util.Base64.decode(encodedPath, android.util.Base64.NO_WRAP), Charsets.UTF_8)
                } catch (e: Exception) {
                    URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
                }
            } else {
                URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
            }
            FolderBrowserScreen(
                folderPath = folderPath,
                onImageClick = { imageIndex ->
                    navController.navigate(Routes.viewer(folderPath, imageIndex))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.VIEWER,
            arguments = listOf(
                navArgument("folderPath") { type = NavType.StringType },
                navArgument("imageIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("folderPath") ?: ""
            val folderPath = if (encodedPath.length > 20) {
                try {
                    String(android.util.Base64.decode(encodedPath, android.util.Base64.NO_WRAP), Charsets.UTF_8)
                } catch (e: Exception) {
                    URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
                }
            } else {
                URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
            }
            val imageIndex = backStackEntry.arguments?.getInt("imageIndex") ?: 0
            ImageViewerScreen(
                folderPath = folderPath,
                initialIndex = imageIndex,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
