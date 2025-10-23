package com.cpen321.squadup.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cpen321.squadup.R
import com.cpen321.squadup.ui.screens.AuthScreen
import com.cpen321.squadup.ui.screens.LoadingScreen
import com.cpen321.squadup.ui.screens.CreateGroupScreen
import com.cpen321.squadup.ui.screens.GroupSuccessScreen
import com.cpen321.squadup.ui.screens.GroupDetailsScreen
import com.cpen321.squadup.ui.screens.MainScreen
import com.cpen321.squadup.ui.screens.ManageHobbiesScreen
import com.cpen321.squadup.ui.screens.ManageProfileScreen
import com.cpen321.squadup.ui.screens.ProfileScreenActions
import com.cpen321.squadup.ui.screens.ProfileCompletionScreen
import com.cpen321.squadup.ui.screens.ProfileScreen
import com.cpen321.squadup.ui.viewmodels.AuthViewModel
import com.cpen321.squadup.ui.viewmodels.MainViewModel
import com.cpen321.squadup.ui.viewmodels.NavigationViewModel
import com.cpen321.squadup.ui.viewmodels.NewsViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupsDataAll
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.GroupLeaderUser

object NavRoutes {
    const val LOADING = "loading"
    const val AUTH = "auth"
    const val MAIN = "main"
    const val PROFILE = "profile"
    const val CREATE_GROUP = "group"
    const val MANAGE_PROFILE = "manage_profile"
    const val MANAGE_HOBBIES = "manage_hobbies"
    const val PROFILE_COMPLETION = "profile_completion"
    const val GROUP_DETAIL = "group_detail"
    const val GROUP_LIST = "group_list"

}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    val navigationStateManager = navigationViewModel.navigationStateManager
    val navigationEvent by navigationStateManager.navigationEvent.collectAsState()

    // Initialize view models required for navigation-level scope
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val mainViewModel: MainViewModel = hiltViewModel()
    val newsViewModel: NewsViewModel = hiltViewModel()

    // Handle navigation events from NavigationStateManager
    LaunchedEffect(navigationEvent) {
        handleNavigationEvent(
            navigationEvent,
            navController,
            navigationStateManager,
            authViewModel,
            mainViewModel
        )
    }

    AppNavHost(
        navController = navController,
        authViewModel = authViewModel,
        profileViewModel = profileViewModel,
        mainViewModel = mainViewModel,
        navigationStateManager = navigationStateManager,
        newsViewModel = newsViewModel
    )
}

private fun handleNavigationEvent(
    navigationEvent: NavigationEvent,
    navController: NavHostController,
    navigationStateManager: NavigationStateManager,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    when (navigationEvent) {
        is NavigationEvent.NavigateToAuth -> {
            navController.navigate(NavRoutes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToAuthWithMessage -> {
            authViewModel.setSuccessMessage(navigationEvent.message)
            navController.navigate(NavRoutes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToCreateGroup -> {
            navController.navigate(NavRoutes.CREATE_GROUP)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMain -> {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToMainWithMessage -> {
            mainViewModel.setSuccessMessage(navigationEvent.message)
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToProfileCompletion -> {
            navController.navigate(NavRoutes.PROFILE_COMPLETION) {
                popUpTo(0) { inclusive = true }
            }
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToProfile -> {
            navController.navigate(NavRoutes.PROFILE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToManageProfile -> {
            navController.navigate(NavRoutes.MANAGE_PROFILE)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateToManageHobbies -> {
            navController.navigate(NavRoutes.MANAGE_HOBBIES)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NavigateBack -> {
            navController.popBackStack()
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.ClearBackStack -> {
            navController.popBackStack(navController.graph.startDestinationId, false)
            navigationStateManager.clearNavigationEvent()
        }

        is NavigationEvent.NoNavigation -> {
            // Do nothing
        }
    }
}
@Composable
private fun MainScreenWithHobbies(
    navController: NavHostController, 
    mainViewModel: MainViewModel,
    newsViewModel: NewsViewModel,
    profileViewModel: ProfileViewModel,
    onProfileClick: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val selectedHobbies = uiState.selectedHobbies.toList()

    LaunchedEffect(Unit) {
        profileViewModel.uiState.collect { profileState ->
            if (profileState.selectedHobbies.isEmpty()) {
                newsViewModel.clearData()
            }
        }
    }
    MainScreen(
        mainViewModel = mainViewModel,
        newsViewModel = newsViewModel,
        selectedHobbies = selectedHobbies,
        onProfileClick = onProfileClick,
        navController = navController
    )
}
@Composable
private fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    mainViewModel: MainViewModel,
    newsViewModel: NewsViewModel,
    navigationStateManager: NavigationStateManager
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOADING
    ) {
        composable(NavRoutes.LOADING) {
            LoadingScreen(message = stringResource(R.string.checking_authentication))
        }

        composable(NavRoutes.AUTH) {
            AuthScreen(authViewModel = authViewModel, profileViewModel = profileViewModel)
        }

        composable(NavRoutes.PROFILE_COMPLETION) {
            ProfileCompletionScreen(
                profileViewModel = profileViewModel,
                onProfileCompleted = { navigationStateManager.handleProfileCompletion() },
                onProfileCompletedWithMessage = { message ->
                    Log.d("AppNavigation", "Profile completed with message: $message")
                    navigationStateManager.handleProfileCompletionWithMessage(message)
                }
            )
        }

        composable(NavRoutes.MAIN) {
            MainScreenWithHobbies(
                navController = navController,
                mainViewModel = mainViewModel,
                newsViewModel = newsViewModel,
                profileViewModel = profileViewModel,
                onProfileClick = { navigationStateManager.navigateToProfile() }
            )
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                profileViewModel = profileViewModel,
                actions = ProfileScreenActions(
                    onBackClick = { navigationStateManager.navigateBack() },
                    onManageProfileClick = { navigationStateManager.navigateToManageProfile() },
                    onManageHobbiesClick = { navigationStateManager.navigateToManageHobbies() },
                    onAccountDeleted = { navigationStateManager.handleAccountDeletion() }
                )
            )
        }

        composable(NavRoutes.MANAGE_PROFILE) {
            ManageProfileScreen(
                profileViewModel = profileViewModel,
                onBackClick = { navigationStateManager.navigateBack() }
            )
        }

        composable(NavRoutes.MANAGE_HOBBIES) {
            ManageHobbiesScreen(
                profileViewModel = profileViewModel,
                onBackClick = { navigationStateManager.navigateBack() }
            )
        }

        composable(NavRoutes.CREATE_GROUP) {
            CreateGroupScreen(navController = navController)
        }

        composable("group_success/{groupName}/{joinCode}") { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: ""
            val joinCode = backStackEntry.arguments?.getString("joinCode") ?: ""
        
            GroupSuccessScreen(
                navController = navController,
                groupName = groupName,
                joinCode = joinCode
            )
        }

        composable("group_details/{groupId}") { backStackEntry ->
            val joinCode = backStackEntry.arguments?.getString("joinCode") ?: ""
            val group = mainViewModel.getGroupById(joinCode) // Replace with getGroupById API call
            
            group?.let {
                GroupDetailsScreen(
                    navController = navController,
                    group = it
                )
            }
        }

        composable("group_details/{joinCode}") { backStackEntry ->
            val joinCode = backStackEntry.arguments?.getString("joinCode") ?: ""
            val mainViewModel: MainViewModel = hiltViewModel()
        
            LaunchedEffect(joinCode) {
                mainViewModel.fetchGroupByJoinCode(
                    joinCode = joinCode,
                    onSuccess = { group ->
                        navController.navigate("group_details_screen/${group.joinCode}")
                    },
                    onError = { errorMessage ->
                        Log.e("Navigation", "Error fetching group: $errorMessage")
                    }
                )
            }
        }
        
        // Define a new route for the GroupDetailsScreen
        composable("group_details_screen/{joinCode}") { backStackEntry ->
            val joinCode = backStackEntry.arguments?.getString("joinCode") ?: ""
            val group = mainViewModel.getGroupById(joinCode)
        
            group?.let {
                GroupDetailsScreen(
                    navController = navController,
                    group = it
                )
            }
        }

    }
}
