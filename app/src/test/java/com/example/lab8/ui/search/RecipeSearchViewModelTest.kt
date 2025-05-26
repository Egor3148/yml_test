package com.example.lab8.ui.search

import app.cash.turbine.test
import com.example.lab8.data.network.RetrofitClient
import com.example.lab8.data.network.TheMealDbApiService
import com.example.lab8.data.MealResponse
import com.example.lab8.data.MealSummary
import com.example.lab8.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockkObject
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class RecipeSearchViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockMealDbService: TheMealDbApiService

    private lateinit var viewModel: RecipeSearchViewModel

    @Before
    fun setUp() {
        mockkObject(RetrofitClient)
        coEvery { RetrofitClient.instance } returns mockMealDbService

        viewModel = RecipeSearchViewModel()
    }

    @After
    fun tearDown() {
        io.mockk.unmockkObject(RetrofitClient)
    }


    @Test
    fun `searchRecipes - given valid query - when service success - then updates recipes and loading state`() = runTest(mainCoroutineRule.testDispatcher) {
        val query = "chicken"
        val fakeMealList = listOf(MealSummary("1", "Chicken Test", "url.com/chicken.jpg"))
        val fakeResponse = MealResponse(fakeMealList)
        coEvery { mockMealDbService.searchMealsByName(query) } returns Response.success(fakeResponse)

        viewModel.isLoading.test {
            assertEquals(false, awaitItem())

            viewModel.searchRecipes(query)

            assertEquals(true, awaitItem())
            assertEquals(false, awaitItem())
        }

        assertEquals(fakeMealList, viewModel.recipes.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `searchRecipes - given valid query - when service error - then updates error message`() = runTest(mainCoroutineRule.testDispatcher) {
        val query = "error_query"
        val errorCode = 500
        val errorResponseBody = okhttp3.ResponseBody.create(null, "Server Error")
        coEvery { mockMealDbService.searchMealsByName(query) } returns Response.error(errorCode, errorResponseBody)

        viewModel.errorMessage.test {
            assertEquals(null, awaitItem())

            viewModel.searchRecipes(query)

            val errorMsg = awaitItem()
            assertNotNull(errorMsg)
            assertTrue(errorMsg!!.contains("Ошибка сервера (поиск): $errorCode"))

            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.recipes.value.isEmpty())
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `WorkspaceRandomRecipes - when service success - then updates recipes`() = runTest(mainCoroutineRule.testDispatcher) {
        val fakeMealDetail = com.example.lab8.data.MealDetail(
            id = "123", name = "Random Dish", thumbnailUrl = "random.jpg", /* ... */
            drinkAlternate = null, category = null, area = null, instructions = null, tags = null, youtubeUrl = null,
            ingredient1 = null, ingredient2 = null, ingredient3 = null, ingredient4 = null, ingredient5 = null,
            ingredient6 = null, ingredient7 = null, ingredient8 = null, ingredient9 = null, ingredient10 = null,
            ingredient11 = null, ingredient12 = null, ingredient13 = null, ingredient14 = null, ingredient15 = null,
            ingredient16 = null, ingredient17 = null, ingredient18 = null, ingredient19 = null, ingredient20 = null,
            measure1 = null, measure2 = null, measure3 = null, measure4 = null, measure5 = null,
            measure6 = null, measure7 = null, measure8 = null, measure9 = null, measure10 = null,
            measure11 = null, measure12 = null, measure13 = null, measure14 = null, measure15 = null,
            measure16 = null, measure17 = null, measure18 = null, measure19 = null, measure20 = null,
            sourceUrl = null, imageSource = null, creativeCommonsConfirmed = null, dateModified = null
        )
        val fakeResponse = com.example.lab8.data.MealDetailResponse(listOf(fakeMealDetail))
        coEvery { mockMealDbService.getRandomMeal() } returns Response.success(fakeResponse)

        viewModel.recipes.test {
            assertEquals(emptyList<MealSummary>(), awaitItem())

            viewModel.fetchRandomRecipes(1)

            val expectedSummary = MealSummary(fakeMealDetail.id, fakeMealDetail.name, fakeMealDetail.thumbnailUrl)
            assertEquals(listOf(expectedSummary), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        assertNull(viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
    }
}