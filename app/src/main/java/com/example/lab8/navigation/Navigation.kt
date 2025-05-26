package com.example.lab8.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab8.ui.detail.RecipeDetailScreen
import com.example.lab8.ui.favorites.FavoritesScreen
import com.example.lab8.ui.search.RecipeSearchScreen

sealed class Screen(val route: String) {

    object RecipeSearch : Screen("recipe_search_screen")


    object RecipeDetail : Screen("recipe_detail_screen/{mealId}") {

        fun createRoute(mealId: String) = "recipe_detail_screen/$mealId"
    }


    object Favorites : Screen("favorites_screen")
}

@Composable
fun AppNavigation(navControllerProvided: NavHostController? = null) {
    val navController = navControllerProvided ?: rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Screen.RecipeSearch.route
    ) {
        composable(route = Screen.RecipeSearch.route) {
            RecipeSearchScreen(navController = navController)
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("mealId") {
                type = NavType.StringType
            })
        ) {
            RecipeDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }
    }
}