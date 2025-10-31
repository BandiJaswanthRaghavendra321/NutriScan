package uk.ac.tees.mad.nutriscan.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutriscan.ui.splash.SplashScreen


sealed class NutriNavigationComp(val route: String){
    object Splash : NutriNavigationComp("splash")
    object Home : NutriNavigationComp("home")
    object Auth : NutriNavigationComp("auth")

}

@Composable
fun NutriScanApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NutriNavigationComp.Splash.route){
        composable(NutriNavigationComp.Splash.route){
            SplashScreen(){
                navController.navigate(NutriNavigationComp.Auth.route){
                    popUpTo(0)
                }
            }
        }
        composable(NutriNavigationComp.Auth.route){
            AuthScreen(){
                navController.navigate(NutriNavigationComp.Home.route)
            }
        }
    }

}