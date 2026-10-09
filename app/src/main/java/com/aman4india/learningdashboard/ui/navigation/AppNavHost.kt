package com.aman4india.learningdashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aman4india.learningdashboard.ui.dashboard.DashboardRoute
import com.aman4india.learningdashboard.ui.details.CourseDetailsRoute
import com.aman4india.learningdashboard.ui.login.LoginRoute

@Composable
fun AppNavHost(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    // Decided once; later session changes (logout) are handled by the effect below.
    val startDestination: Any = remember { if (isLoggedIn) DashboardDestination else LoginDestination }

    LaunchedEffect(isLoggedIn) {
        val onLogin = navController.currentDestination?.hasRoute<LoginDestination>() == true
        if (!isLoggedIn && !onLogin) {
            navController.navigate(LoginDestination) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginDestination> {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(DashboardDestination) {
                        popUpTo<LoginDestination> { inclusive = true }
                    }
                },
            )
        }
        composable<DashboardDestination> {
            DashboardRoute(
                onCourseClick = { courseId -> navController.navigate(CourseDetailsDestination(courseId)) },
            )
        }
        composable<CourseDetailsDestination> {
            CourseDetailsRoute(onBack = { navController.popBackStack() })
        }
    }
}
