package com.example.lab8.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lab8.data.MealDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    onNavigateBack: () -> Unit,
    recipeDetailViewModel: RecipeDetailViewModel = viewModel()
) {
    val mealDetail by recipeDetailViewModel.mealDetail.collectAsState()
    val isLoading by recipeDetailViewModel.isLoading.collectAsState()
    val errorMessage by recipeDetailViewModel.errorMessage.collectAsState()
    val isFavoriteRecipe by recipeDetailViewModel.isFavorite.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        mealDetail?.name ?: "Детали рецепта",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("RecipeDetailScreen_Title")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    mealDetail?.let {
                        IconButton(onClick = {
                            recipeDetailViewModel.toggleFavorite()
                        }) {
                            Icon(
                                imageVector = if (isFavoriteRecipe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (isFavoriteRecipe) "Удалить из избранного" else "Добавить в избранное",
                                tint = if (isFavoriteRecipe) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            errorMessage?.let { message ->
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val currentMealId = mealDetail?.id
                        if (currentMealId != null) {
                            recipeDetailViewModel.fetchRecipeDetails(currentMealId)
                        } else {
                            recipeDetailViewModel.clearErrorMessage()
                        }
                    }) {
                        Text("Попробовать снова")
                    }
                }
            }

            mealDetail?.let { detail ->
                RecipeDetailContent(detail = detail)
            }
        }
    }
}

@Composable
fun RecipeDetailContent(detail: MealDetail, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        AsyncImage(
            model = detail.thumbnailUrl,
            contentDescription = "Фото ${detail.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            detail.category?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Категория: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            detail.area?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Кухня: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        detail.tags?.takeIf { it.isNotBlank() }?.let { tags ->
            Text(
                text = "Теги: ${tags.split(',').joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }


        SectionTitle("Ингредиенты:")
        val ingredients = detail.getIngredientsWithMeasures()
        if (ingredients.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)) {
                ingredients.forEach { (ingredient, measure) ->
                    Text(text = "• $ingredient - $measure", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            Text("Список ингредиентов не указан.", modifier = Modifier.padding(bottom = 16.dp))
        }

        SectionTitle("Инструкции:")
        Text(
            text = detail.instructions?.trim() ?: "Инструкции не указаны.",
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        SectionTitle("Дополнительно:")
        detail.youtubeUrl?.takeIf { it.isNotBlank() }?.let { url ->
            LinkTextItem(
                textBeforeLink = "Смотреть на ",
                linkText = "YouTube",
                url = url,
                uriHandler = uriHandler
            )
        }

        detail.sourceUrl?.takeIf { it.isNotBlank() }?.let { url ->
            LinkTextItem(
                textBeforeLink = "Источник рецепта: ",
                linkText = "Перейти",
                url = url,
                uriHandler = uriHandler
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
private fun LinkTextItem(
    textBeforeLink: String,
    linkText: String,
    url: String,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val annotatedString = buildAnnotatedString {
        append(textBeforeLink)
        pushStringAnnotation(tag = "URL_TAG", annotation = url)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
            append(linkText)
        }
        pop()
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(
            imageVector = Icons.Filled.Link,
            contentDescription = "Ссылка",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL_TAG", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        try {
                            uriHandler.openUri(annotation.item)
                        } catch (e: Exception) {
                            println("Could not open URI: ${annotation.item}, Error: ${e.message}")
                        }
                    }
            }
        )
    }
}