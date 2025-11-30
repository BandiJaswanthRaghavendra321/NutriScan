package uk.ac.tees.mad.nutriscan.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nutriscan.ui.splash.SplashScreen
import uk.ac.tees.mad.nutriscan.ui.viewmodels.AuthenticationVM
import uk.ac.tees.mad.nutriscan.ui.viewmodels.MainVM


sealed class NutriNavigationComp(val route: String){
    object Splash : NutriNavigationComp("splash")
    object Home : NutriNavigationComp("home")
    object Auth : NutriNavigationComp("auth")
    object Details : NutriNavigationComp("details/{barcode}") {
        const val BARCODE_ARG = "barcode"

        fun withArgs(barcode: String): String {
            return route.replace("{$BARCODE_ARG}", barcode)
        }

        fun createRoute(): String = route
    }

}

@Composable
fun NutriScanApp() {
    val navController = rememberNavController()
    val authenticationViewModel = hiltViewModel<AuthenticationVM>()
    val homeVm = hiltViewModel<MainVM>()

    NavHost(navController = navController, startDestination = NutriNavigationComp.Splash.route){
        composable(NutriNavigationComp.Splash.route){
            SplashScreen() {
                if (authenticationViewModel.loggedIn) {
                    navController.navigate(NutriNavigationComp.Home.route) {
                        popUpTo(0)
                    }
                } else {
                    navController.navigate(NutriNavigationComp.Auth.route) {
                        popUpTo(0)
                    }
                }
            }
        }
        composable(NutriNavigationComp.Auth.route){
            AuthScreen(authenticationViewModel, navController)
        }
        composable(NutriNavigationComp.Home.route){
            HomeScreen(viewModel = homeVm, navController,onBarcodeScanned = {}, onNavigateToProfile = {}, onNavigateToHistory = {})
        }
        composable(
            route = NutriNavigationComp.Details.createRoute(),
            arguments = listOf(
                navArgument(NutriNavigationComp.Details.BARCODE_ARG) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString(NutriNavigationComp.Details.BARCODE_ARG)
                ?: error("Barcode is required for Details screen")
            DetailsScreen(homeVm, barcode)
        }
    }

}