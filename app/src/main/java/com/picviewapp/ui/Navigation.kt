package com.picviewapp.ui

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
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

    fun browser(folderPath: String) = "browser/${URLEncoder.encode(folderPath, StandardCharsets.UTF_8.toString())}"
    fun viewer(folderPath: String, imageIndex: Int) = "viewer/${URLEncoder.encode(folderPath, StandardCharsets.UTF_8.toString())}/$imageIndex"
}

@Composable
fun PicViewNavHost(
    navController: NavController = rememberNavController()
) {
    var pendingFolderPath by remember { mutableStateOf<String?>(null) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val path = getPathFromUri(it)
            if (path != null) {
                pendingFolderPath = path
            }
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
            val folderPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
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
            val folderPath = URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.toString())
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
