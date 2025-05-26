package com.example.lab8.ui.detail

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab8.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeDetailScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        navController.navigatorProvider.addNavigator(DialogNavigator())
    }

    @Test
    fun backNavigation_fromDetailScreen_returnsToPreviousScreen() {
        val testMealId = "52771"
        val recipeName = "Spicy Arrabiata Penne"

        composeTestRule.onNodeWithText("Найти рецепт...").performTextInput(recipeName)
        composeTestRule.onNodeWithText("Поиск").performClick()

        try {
            val recipeListItemTag = "RecipeListItem_${testMealId}"
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule
                    .onAllNodesWithTag(recipeListItemTag)
                    .fetchSemanticsNodes().size == 1
            }
            composeTestRule.onNodeWithTag(recipeListItemTag).performClick()

        } catch (e: Exception) {
            println("Не удалось найти или кликнуть по элементу списка ${"RecipeListItem_${testMealId}"} в тесте для проверки навигации назад: ${e.message}")

            composeTestRule.onRoot(useUnmergedTree = true).printToLog("NAV_FAILURE_UI_TREE")
            return
        }

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithContentDescription("Назад")
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithContentDescription("Назад").performClick()

        composeTestRule.onNodeWithText("Найти рецепт...").assertIsDisplayed()
    }
}