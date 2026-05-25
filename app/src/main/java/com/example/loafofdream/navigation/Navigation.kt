package com.example.loafofdream.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.loafofdream.data.local.PreferencesManager
import com.example.loafofdream.data.local.SearchHistoryManager
import com.example.loafofdream.presentation.screens.*
import com.example.loafofdream.presentation.viewmodels.*

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PRODUCT_LIST = "product_list"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val PRODUCTION = "production"
    const val SALES = "sales"
    const val REMAINDERS = "remainders"
    const val REVENUE = "revenue"
    const val CALENDAR = "calendar"

    fun productDetail(id: Int) = "product_detail/$id"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    prefs: PreferencesManager,
    searchHistoryManager: SearchHistoryManager,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val startDestination = if (prefs.isLoggedIn()) Routes.PRODUCT_LIST else Routes.LOGIN
    val selectedDateViewModel: SelectedDateViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            val authViewModel = remember { AuthViewModel(prefs) }
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            val authViewModel = remember { AuthViewModel(prefs) }
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.PRODUCT_LIST) {
            val productViewModel: ProductViewModel = viewModel()
            ProductListScreen(
                viewModel = productViewModel,
                searchHistoryManager = searchHistoryManager,
                userRole = prefs.userRole ?: "",
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onNavigateToDetail = { id -> navController.navigate(Routes.productDetail(id)) },
                onNavigateToProduction = { navController.navigate(Routes.PRODUCTION) },
                onNavigateToSales = { navController.navigate(Routes.SALES) },
                onNavigateToRemainders = { navController.navigate(Routes.REMAINDERS) },
                onNavigateToRevenue = { navController.navigate(Routes.REVENUE) },
                onNavigateToCalendar = { navController.navigate(Routes.CALENDAR) },
                onLogout = {
                    prefs.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            val productViewModel: ProductViewModel = viewModel()
            ProductDetailScreen(
                productId = productId,
                viewModel = productViewModel,
                userRole = prefs.userRole ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PRODUCTION) {
            val productViewModel: ProductViewModel = viewModel()
            val productionViewModel: ProductionViewModel = viewModel()
            ProductionScreen(
                selectedDateViewModel = selectedDateViewModel,
                productViewModel = productViewModel,
                productionViewModel = productionViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SALES) {
            val productViewModel: ProductViewModel = viewModel()
            val salesViewModel: SalesViewModel = viewModel()
            SalesScreen(
                selectedDateViewModel = selectedDateViewModel,
                productViewModel = productViewModel,
                salesViewModel = salesViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REMAINDERS) {
            val statsViewModel: StatsViewModel = viewModel()
            RemaindersScreen(
                viewModel = statsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REVENUE) {
            val statsViewModel: StatsViewModel = viewModel()
            RevenueScreen(
                viewModel = statsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CALENDAR) {
            val calendarViewModel: CalendarViewModel = viewModel()
            CalendarScreen(
                viewModel = calendarViewModel,
                selectedDateViewModel = selectedDateViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
