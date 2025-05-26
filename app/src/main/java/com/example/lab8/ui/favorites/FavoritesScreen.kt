package com.example.lab8.ui.favorites

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lab8.data.local.AppDatabase
import com.example.lab8.data.local.FavoriteRecipeEntity
import com.example.lab8.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            favoriteRecipeDao = AppDatabase.getDatabase(LocalContext.current.applicationContext).favoriteRecipeDao()
        )
    )
) {
    val favoriteRecipes by favoritesViewModel.favoriteRecipes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранные рецепты") },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (favoriteRecipes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "У вас пока нет избранных рецептов.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Text(
                    "Ваши сохраненные рецепты:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favoriteRecipes, key = { it.idMeal }) { recipe ->
                        FavoriteRecipeListItem(
                            recipe = recipe,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipeListItem(
    recipe: FavoriteRecipeEntity,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {
            navController.navigate(Screen.RecipeDetail.createRoute(recipe.idMeal))
        },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = recipe.strMealThumb,
                contentDescription = "Фото ${recipe.strMeal}",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp)
            )
            Text(
                text = recipe.strMeal,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}