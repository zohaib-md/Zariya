package com.project.zariya.core.navigation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.project.zariya.core.ui.components.ZariyaBottomBar
import com.project.zariya.core.ui.theme.ZariyaBackground
import com.project.zariya.feature.auth.presentation.AuthViewModel

@Composable
fun ZariyaNavigation(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine start destination based on auth state
    val startDestination = if (authState.isAuthenticated) {
        Route.Home.route
    } else {
        Route.Auth.route
    }

    // Auth screens where bottom bar should NOT be shown
    val authRoutes = listOf(Route.Auth.route)

    // Show bottom bar only on main screens (not auth screens)
    val showBottomBar = currentRoute in listOf(
        Route.Home.route,
        Route.MedicineList.route,
        Route.Analytics.route,
        Route.ProfileList.route
    )

    Scaffold(
        containerColor = ZariyaBackground,
        bottomBar = {
            if (showBottomBar) {
                ZariyaBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        ZariyaNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            startDestination = startDestination,
            authViewModel = authViewModel
        )
    }
}
