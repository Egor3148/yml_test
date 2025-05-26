package com.example.lab8.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.lab8.data.local.AppDatabase
import com.example.lab8.data.local.FavoriteRecipeDao
import com.example.lab8.data.local.FavoriteRecipeEntity
import com.example.lab8.data.network.RetrofitClient
import com.example.lab8.data.network.TheMealDbApiService
import com.example.lab8.data.MealDetail
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val mealDbService: TheMealDbApiService = RetrofitClient.instance
    private val favoriteRecipeDao: FavoriteRecipeDao = AppDatabase.getDatabase(application).favoriteRecipeDao()

    private val mealId: String? = savedStateHandle["mealId"]

    private val _mealDetail = MutableStateFlow<MealDetail?>(null)
    val mealDetail: StateFlow<MealDetail?> = _mealDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val isFavorite: StateFlow<Boolean> = mealId?.let { id ->
        favoriteRecipeDao.getFavoriteById(id)
            .map { it != null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    } ?: flowOf(false).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    init {
        mealId?.let {
            if (it.isNotBlank()) {
                fetchRecipeDetails(it)
            } else {
                _errorMessage.value = "Неверный ID рецепта."
                _isLoading.value = false;
            }
        } ?: run {
            _errorMessage.value = "ID рецепта не передан."
            _isLoading.value = false;
        }
    }

    fun fetchRecipeDetails(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = mealDbService.getMealDetailsById(id)
                if (response.isSuccessful && response.body()?.mealDetails?.isNotEmpty() == true) {
                    _mealDetail.value = response.body()?.mealDetails?.first()
                } else {
                    _mealDetail.value = null
                    _errorMessage.value = "Не удалось загрузить детали рецепта. Код: ${response.code()}"
                }
            } catch (e: Exception) {
                _mealDetail.value = null
                _errorMessage.value = "Ошибка при загрузке деталей: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentDetail = _mealDetail.value
            if (currentDetail != null && mealId != null) {
                val isCurrentlyFavorite = isFavorite.value
                if (isCurrentlyFavorite) {
                    favoriteRecipeDao.deleteFavoriteById(mealId)
                } else {
                    val favoriteRecipe = FavoriteRecipeEntity(
                        idMeal = currentDetail.id,
                        strMeal = currentDetail.name,
                        strMealThumb = currentDetail.thumbnailUrl
                    )
                    favoriteRecipeDao.insertFavorite(favoriteRecipe)
                }
            } else {
                _errorMessage.value = "Детали рецепта не загружены для изменения статуса избранного."
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}