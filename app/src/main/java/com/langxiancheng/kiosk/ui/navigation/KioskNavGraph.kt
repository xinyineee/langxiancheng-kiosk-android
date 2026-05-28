package com.langxiancheng.kiosk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.langxiancheng.kiosk.ui.screen.idle.IdleScreen
import com.langxiancheng.kiosk.ui.screen.question.QuestionScreen
import com.langxiancheng.kiosk.ui.screen.result.ResultScreen
import com.langxiancheng.kiosk.ui.screen.welcome.WelcomeScreen

/**
 * Navigation route constants for the Kiosk application.
 */
object KioskRoutes {
    const val IDLE = "idle"
    const val WELCOME = "welcome"
    const val QUESTION = "question/{index}"
    const val RESULT = "result"

    /** Creates a question route with the given index (0-4). */
    fun questionRoute(index: Int): String = "question/$index"
}

/**
 * Main navigation graph for the Kiosk application.
 * Flow: idle → welcome → question/{index} (0-4) → result
 */
@Composable
fun KioskNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = KioskRoutes.IDLE,
        modifier = modifier
    ) {
        composable(KioskRoutes.IDLE) {
            IdleScreen(
                onStartClicked = {
                    navController.navigate(KioskRoutes.WELCOME) {
                        popUpTo(KioskRoutes.IDLE) { inclusive = true }
                    }
                }
            )
        }

        composable(KioskRoutes.WELCOME) {
            WelcomeScreen(
                onStartTest = {
                    navController.navigate(KioskRoutes.questionRoute(0)) {
                        popUpTo(KioskRoutes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = KioskRoutes.QUESTION,
            arguments = listOf(
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val questionIndex = backStackEntry.arguments?.getInt("index") ?: 0
            QuestionScreen(
                questionIndex = questionIndex,
                onNextQuestion = { nextIndex ->
                    navController.navigate(KioskRoutes.questionRoute(nextIndex)) {
                        popUpTo(KioskRoutes.questionRoute(questionIndex)) { inclusive = true }
                    }
                },
                onFinishTest = {
                    navController.navigate(KioskRoutes.RESULT) {
                        popUpTo(KioskRoutes.IDLE) { inclusive = true }
                    }
                }
            )
        }

        composable(KioskRoutes.RESULT) {
            ResultScreen(
                onReturnToIdle = {
                    navController.navigate(KioskRoutes.IDLE) {
                        popUpTo(KioskRoutes.IDLE) { inclusive = true }
                    }
                }
            )
        }
    }
}
