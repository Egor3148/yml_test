package com.example.lab8.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab8.data.network.RetrofitClient
import com.example.lab8.data.network.TheMealDbApiService
import com.example.lab8.data.MealSummary
import com.example.lab8.data.CategoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeSearchViewModel : ViewModel() {

    private val mealDbService: TheMealDbApiService = RetrofitClient.instance

    private val _recipes = MutableStateFlow<List<MealSummary>>(emptyList())
    val recipes: StateFlow<List<MealSummary>> = _recipes.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        println("RecipeSearchViewModel: init block executed (test mode - no initial fetch)")
    }

    fun searchRecipes(query: String) {
        if (query.isBlank()) {
            _recipes.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = mealDbService.searchMealsByName(query)
                if (response.isSuccessful) {
                    _recipes.value = response.body()?.meals ?: emptyList()
                    if (_recipes.value.isEmpty() && query.isNotBlank()) {
                        _errorMessage.value = "Рецепты не найдены по запросу: \"$query\""
                    }
                } else {
                    _recipes.value = emptyList()
                    _errorMessage.value = "Ошибка сервера (поиск): ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _recipes.value = emptyList()
                _errorMessage.value = "Ошибка сети или другая проблема (поиск): ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchRandomRecipes(count: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val randomRecipesList = mutableListOf<MealSummary>()
            try {
                for (i in 1..count) {
                    val response = mealDbService.getRandomMeal()
                    if (response.isSuccessful && response.body()?.mealDetails?.isNotEmpty() == true) {
                        val mealDetail = response.body()?.mealDetails?.first()!!
                        randomRecipesList.add(
                            MealSummary(
                                id = mealDetail.id,
                                name = mealDetail.name,
                                thumbnailUrl = mealDetail.thumbnailUrl
                            )
                        )
                    } else if (!response.isSuccessful) {
                        _errorMessage.value = "Ошибка сервера (случайный рецепт): ${response.code()} ${response.message()}"
                        break
                    }
                }
                _recipes.value = randomRecipesList
                if (randomRecipesList.isEmpty() && _errorMessage.value == null) {
                    _errorMessage.value = "Не удалось загрузить случайные рецепты."
                }
            } catch (e: Exception) {
                _recipes.value = emptyList()
                _errorMessage.value = "Ошибка сети (случайный рецепт): ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchRecipesByCategory(category: String) {
        if (category.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = mealDbService.filterByCategory(category)
                if (response.isSuccessful) {
                    _recipes.value = response.body()?.meals ?: emptyList()
                    if (_recipes.value.isEmpty()) {
                        _errorMessage.value = "Рецепты не найдены в категории: \"$category\""
                    }
                } else {
                    _recipes.value = emptyList()
                    _errorMessage.value = "Ошибка сервера (категория): ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _recipes.value = emptyList()
                _errorMessage.value = "Ошибка сети (категория): ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = mealDbService.listCategories()
                if (response.isSuccessful) {
                    _categories.value = response.body()?.categories ?: emptyList()
                } else {
                    println("Ошибка загрузки категорий: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Сетевая ошибка при загрузке категорий: ${e.message}")
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}