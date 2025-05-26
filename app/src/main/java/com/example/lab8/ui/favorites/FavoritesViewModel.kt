package com.example.lab8.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab8.data.local.AppDatabase
import com.example.lab8.data.local.FavoriteRecipeDao
import com.example.lab8.data.local.FavoriteRecipeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel

// class FavoritesViewModel(application: Application) : AndroidViewModel(application) { // Старая версия
// С учетом того, как вы создаете viewModel в тесте, она должна выглядеть так:
class FavoritesViewModel(
    application: Application, // application все еще здесь из-за AndroidViewModel
    private val favoriteRecipeDao: FavoriteRecipeDao
) : AndroidViewModel(application) {

    val favoriteRecipes: StateFlow<List<FavoriteRecipeEntity>> =
        favoriteRecipeDao.getAllFavorites()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
}


class FavoritesViewModelFactory(
    private val application: Application,
    private val favoriteRecipeDao: FavoriteRecipeDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(application, favoriteRecipeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}