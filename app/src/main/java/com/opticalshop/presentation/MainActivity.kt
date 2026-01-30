package com.opticalshop.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.opticalshop.presentation.navigation.NavGraph
import com.opticalshop.presentation.theme.OpticalShopTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @javax.inject.Inject
    lateinit var dataSeeder: com.opticalshop.utils.DataSeeder

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        // Request notification permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }
        
        // Seeding database with 24 products once. 
        // You can comment this out after running the app once.
        //dataSeeder.seedProducts()

        setContent {
            val isDarkTheme = themeViewModel.isDarkTheme.collectAsState(initial = null).value
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            
            OpticalShopTheme(darkTheme = isDarkTheme ?: systemDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHostControllerWrapper(navController)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun NavHostControllerWrapper(navController: androidx.navigation.NavHostController) {
        com.opticalshop.presentation.navigation.MainContainer(navController = navController)
    }
}
