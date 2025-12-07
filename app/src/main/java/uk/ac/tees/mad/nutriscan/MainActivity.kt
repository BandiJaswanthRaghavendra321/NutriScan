package uk.ac.tees.mad.nutriscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import uk.ac.tees.mad.nutriscan.ui.screens.NutriScanApp
import uk.ac.tees.mad.nutriscan.ui.theme.NutriScanTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriScanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NutriScanApp(this)
                }
            }
        }
    }
}