package com.picviewapp.ui

import androidx.compose.runtime.Composable
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
fun PicViewNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onFolderClick = { folderPath ->
                    navController.navigate(Routes.browser(folderPath))
                },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
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
