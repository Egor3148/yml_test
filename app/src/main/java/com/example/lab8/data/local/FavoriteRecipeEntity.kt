package com.example.lab8.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_recipes")
data class FavoriteRecipeEntity(
    @PrimaryKey val idMeal: String,
    val strMeal: String,
    val strMealThumb: String?
)