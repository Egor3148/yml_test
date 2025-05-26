package com.example.lab8.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(recipe: FavoriteRecipeEntity)

    @Delete
    suspend fun deleteFavorite(recipe: FavoriteRecipeEntity)

    @Query("DELETE FROM favorite_recipes WHERE idMeal = :mealId")
    suspend fun deleteFavoriteById(mealId: String)

    @Query("SELECT * FROM favorite_recipes ORDER BY strMeal ASC")
    fun getAllFavorites(): Flow<List<FavoriteRecipeEntity>>

    @Query("SELECT * FROM favorite_recipes WHERE idMeal = :mealId")
    fun getFavoriteById(mealId: String): Flow<FavoriteRecipeEntity?>
}