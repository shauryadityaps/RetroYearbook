package com.yearbook.retro.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.yearbook.retro.AppContainer
import com.yearbook.retro.ui.components.RetroBottomNav
import com.yearbook.retro.ui.screens.auth.AuthViewModel
import com.yearbook.retro.ui.screens.auth.LoginScreen
import com.yearbook.retro.ui.screens.dashboard.DashboardScreen
import com.yearbook.retro.ui.screens.dashboard.DashboardViewModel
import com.yearbook.retro.ui.screens.detail.InsideYearbookScreen
import com.yearbook.retro.ui.screens.detail.InsideYearbookViewModel
import com.yearbook.retro.ui.screens.library.AllYearbooksScreen
import com.yearbook.retro.ui.screens.library.LibraryViewModel
import com.yearbook.retro.ui.screens.manage.AddJoinYearbookScreen
import com.yearbook.retro.ui.screens.manage.ManageViewModel
import com.yearbook.retro.ui.screens.recap.NostalgicSlideshowScreen
import com.yearbook.retro.ui.screens.recap.RecapViewModel
import com.yearbook.retro.ui.screens.recap.VideoExportScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    container: AppContainer,
    startDestination: String = Screen.Dashboard.route,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Library.route,
        Screen.AddJoin.route
    )

    val currentUser by container.authRepository.currentUserFlow.collectAsState(initial = container.authRepository.getCurrentUser())

    androidx.compose.runtime.LaunchedEffect(currentUser) {
        if (currentUser == null && currentRoute != null && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RetroBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Login Screen
            composable(Screen.Login.route) {
                val authVm: AuthViewModel = viewModel(factory = AuthViewModel.Factory(container.authRepository))
                LoginScreen(
                    viewModel = authVm,
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Section 1: Dashboard (Daily Action Hub)
            composable(Screen.Dashboard.route) {
                val dashboardVm: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(container.authRepository, container.yearbookRepository)
                )
                DashboardScreen(
                    viewModel = dashboardVm,
                    onOpenYearbook = { ybId ->
                        navController.navigate(Screen.InsideYearbook.createRoute(ybId))
                    },
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddJoin.route)
                    },
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Section 2: All Yearbooks (Library & Archive)
            composable(Screen.Library.route) {
                val libraryVm: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.Factory(container.authRepository, container.yearbookRepository)
                )
                AllYearbooksScreen(
                    viewModel = libraryVm,
                    onOpenYearbook = { ybId ->
                        navController.navigate(Screen.InsideYearbook.createRoute(ybId))
                    }
                )
            }

            // Section 3: Add / Join Hub
            composable(Screen.AddJoin.route) {
                val manageVm: ManageViewModel = viewModel(
                    factory = ManageViewModel.Factory(container.authRepository, container.yearbookRepository)
                )
                AddJoinYearbookScreen(
                    viewModel = manageVm,
                    onOpenYearbook = { ybId ->
                        navController.navigate(Screen.InsideYearbook.createRoute(ybId))
                    }
                )
            }

            // Section 4: Inside the Yearbook (Photo Stream)
            composable(
                route = Screen.InsideYearbook.route,
                arguments = listOf(navArgument("yearbookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val yearbookId = backStackEntry.arguments?.getString("yearbookId") ?: ""
                val insideVm: InsideYearbookViewModel = viewModel(
                    factory = InsideYearbookViewModel.Factory(
                        yearbookId,
                        container.authRepository,
                        container.yearbookRepository,
                        container.photoRepository
                    )
                )
                InsideYearbookScreen(
                    viewModel = insideVm,
                    onBack = { navController.popBackStack() },
                    onOpenSlideshow = { ybId ->
                        navController.navigate(Screen.Slideshow.createRoute(ybId))
                    },
                    onOpenVideoExport = { ybId ->
                        navController.navigate(Screen.VideoExport.createRoute(ybId))
                    }
                )
            }

            // Recap: Nostalgic Fullscreen Slideshow
            composable(
                route = Screen.Slideshow.route,
                arguments = listOf(navArgument("yearbookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val yearbookId = backStackEntry.arguments?.getString("yearbookId") ?: ""
                val recapVm: RecapViewModel = viewModel(
                    factory = RecapViewModel.Factory(
                        yearbookId,
                        container.yearbookRepository,
                        container.photoRepository
                    )
                )
                NostalgicSlideshowScreen(
                    viewModel = recapVm,
                    onClose = { navController.popBackStack() }
                )
            }

            // Recap: Media3 On-Device Video Reel Exporter
            composable(
                route = Screen.VideoExport.route,
                arguments = listOf(navArgument("yearbookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val yearbookId = backStackEntry.arguments?.getString("yearbookId") ?: ""
                val recapVm: RecapViewModel = viewModel(
                    factory = RecapViewModel.Factory(
                        yearbookId,
                        container.yearbookRepository,
                        container.photoRepository
                    )
                )
                VideoExportScreen(
                    viewModel = recapVm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
