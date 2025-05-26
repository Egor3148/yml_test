package com.example.lab8.data.network

import com.example.lab8.data.CategoryResponse
import com.example.lab8.data.MealDetailResponse
import com.example.lab8.data.MealResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApiService {

    @GET("api/json/v1/1/search.php")
    suspend fun searchMealsByName(@Query("s") query: String): Response<MealResponse>

    @GET("api/json/v1/1/lookup.php")
    suspend fun getMealDetailsById(@Query("i") id: String): Response<MealDetailResponse>

    @GET("api/json/v1/1/filter.php")
    suspend fun filterByCategory(@Query("c") category: String): Response<MealResponse>

    @GET("api/json/v1/1/filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): Response<MealResponse>

    @GET("api/json/v1/1/filter.php")
    suspend fun filterByArea(@Query("a") area: String): Response<MealResponse>

    @GET("api/json/v1/1/list.php?c=list")
    suspend fun listCategories(): Response<CategoryResponse>

    @GET("api/json/v1/1/list.php?a=list")
    suspend fun listAreas(): Response<MealResponse>

    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): Response<MealDetailResponse>
}