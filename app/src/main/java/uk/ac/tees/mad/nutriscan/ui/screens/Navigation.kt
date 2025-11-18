package uk.ac.tees.mad.nutriscan.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutriscan.ui.splash.SplashScreen
import uk.ac.tees.mad.nutriscan.ui.viewmodels.AuthenticationVM


sealed class NutriNavigationComp(val route: String){
    object Splash : NutriNavigationComp("splash")
    object Home : NutriNavigationComp("home")
    object Auth : NutriNavigationComp("auth")

}

@Composable
fun NutriScanApp() {
    val navController = rememberNavController()
    val authenticationViewModel = hiltViewModel<AuthenticationVM>()
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
            HomeScreen(){}
        }
    }

}