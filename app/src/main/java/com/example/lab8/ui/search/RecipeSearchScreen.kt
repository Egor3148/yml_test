package com.example.lab8.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lab8.data.CategoryItem
import com.example.lab8.data.MealSummary
import com.example.lab8.navigation.Screen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSearchScreen(
    navController: NavController,
    recipeSearchViewModel: RecipeSearchViewModel = viewModel()
) {
    val recipesList by recipeSearchViewModel.recipes.collectAsState()
    val categoriesList by recipeSearchViewModel.categories.collectAsState()
    val isLoading by recipeSearchViewModel.isLoading.collectAsState()
    val errorMessage by recipeSearchViewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = Unit) {
        if (categoriesList.isEmpty()) {
            recipeSearchViewModel.fetchCategories()
        }
        println("RecipeSearchScreen: LaunchedEffect executed (test mode - no initial fetch)")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Поиск Рецептов") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Favorites.route)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Избранные рецепты",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChanged = { searchQuery = it },
                onSearchClicked = {
                    if (searchQuery.isNotBlank()) {
                        recipeSearchViewModel.searchRecipes(searchQuery)
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            if (categoriesList.isNotEmpty()) {
                Text("Категории:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                CategoryList(
                    categories = categoriesList,
                    onCategoryClick = { categoryName ->
                        searchQuery = ""
                        recipeSearchViewModel.fetchRecipesByCategory(categoryName)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }


            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Button(onClick = { recipeSearchViewModel.clearErrorMessage() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("OK")
                }
            }

            if (!isLoading && recipesList.isNotEmpty()) {
                Text("Результаты:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                RecipeList(
                    recipes = recipesList,
                    navController = navController
                )
            } else if (!isLoading && recipesList.isEmpty() && searchQuery.isNotBlank() && errorMessage == null) {
            } else if (!isLoading && recipesList.isEmpty() && errorMessage == null && categoriesList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Введите запрос для поиска рецептов.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            label = { Text("Найти рецепт...") },
            modifier = Modifier.weight(1.0f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearchClicked() }
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSearchClicked) {
            Text("Поиск")
        }
    }
}

@Composable
fun CategoryList(
    categories: List<CategoryItem>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(categories) { category ->
            SuggestionChip(
                onClick = { onCategoryClick(category.name) },
                label = { Text(category.name) }
            )
        }
    }
}


@Composable
fun RecipeList(
    recipes: List<MealSummary>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(recipes) { recipe ->
            RecipeListItem(
                recipe = recipe,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListItem(
    recipe: MealSummary,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {
            navController.navigate(Screen.RecipeDetail.createRoute(recipe.id))
        },
        modifier = modifier.fillMaxWidth()
                    .testTag("RecipeListItem_${recipe.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = recipe.thumbnailUrl,
                contentDescription = "Фото ${recipe.name}",
                modifier = Modifier
                    .size(88.dp)
                    .padding(end = 12.dp)
            )
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}