package com.cpen321.squadup.ui.screens

import Icon
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.R
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.GroupUser
import com.cpen321.squadup.ui.components.MessageSnackbar
import com.cpen321.squadup.ui.components.MessageSnackbarState
import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.ui.theme.LocalFontSizes
import com.cpen321.squadup.ui.theme.LocalSpacing
import com.cpen321.squadup.ui.viewmodels.MainUiState
import com.cpen321.squadup.ui.viewmodels.MainViewModel
import com.cpen321.squadup.ui.viewmodels.NewsViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.utils.WebSocketManager




@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    newsViewModel: NewsViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    selectedHobbies: List<String>,
    onProfileClick: () -> Unit,
    navController: NavController 
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val joinCode = remember { mutableStateOf("") } // State for the join code input
    val joinGroupMessage = remember { mutableStateOf<String?>(null) } // State for success/error messages
    val profileUiState by profileViewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        mainViewModel.fetchGroups()
    }
     LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val currentUserId = profileUiState.user?._id
    
    val filteredGroups = uiState.groups.filter { group ->
        Log.d("MainScreen", "filteredGroups: ${group}")
    group.groupLeaderId?.id == currentUserId || group.groupMemberIds?.any { it.id == currentUserId } == true
    }

    // Keep WebSocket subscriptions in sync with groups the user belongs to
    LaunchedEffect(currentUserId, filteredGroups) {
        val userId = currentUserId ?: return@LaunchedEffect
        // Subscribe to all current groups
        filteredGroups.forEach { group ->
            WebSocketManager.subscribeToGroup(userId, group.joinCode)
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(onProfileClick = onProfileClick)
        },
        snackbarHost = {
            MainSnackbarHost(
                hostState = snackBarHostState,
                successMessage = uiState.successMessage,
                onSuccessMessageShown = mainViewModel::clearSuccessMessage
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                //.verticalScroll(rememberScrollState())
        ) {
            // Existing group list and create group button
            MainBody(
                modifier = Modifier.weight(1f),
                paddingValues = PaddingValues(0.dp),
                newsViewModel = newsViewModel,
                selectedHobbies = selectedHobbies,
                onCreateGroupClick = {
                    navController.navigate(NavRoutes.CREATE_GROUP)
                },
                groups = filteredGroups,
                onGroupClick = { groupId ->
                    navController.navigate("${NavRoutes.GROUP_DETAILS}/$groupId")
                },
                navController = navController

            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Create Group Button
                IconButton(
                    onClick = { navController.navigate(NavRoutes.CREATE_GROUP) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add, // Use Icons.Filled for Material icons
                        contentDescription = "Create Group",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Join Group Button
                IconButton(
                    onClick = { navController.navigate(NavRoutes.JOIN_GROUP) },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Group,
                        contentDescription = "Join Group",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun JoinGroupSection(
    joinCode: String,
    onJoinCodeChange: (String) -> Unit,
    onJoinGroupClick: () -> Unit,
    joinGroupMessage: String?
) {
    Log.d("MainScreen", "JoinGroupSection is being rendered")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = joinCode,
            onValueChange = onJoinCodeChange,
            label = { Text("Enter Join Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onJoinGroupClick,
            enabled = joinCode.length == 6 // Enable only if the join code is 6 characters
        ) {
            Text("Join Group")
        }
        joinGroupMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = if (it.contains("success", ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    newsViewModel: NewsViewModel,
    selectedHobbies: List<String>,
    groups: List<GroupDataDetailed>,
    snackBarHostState: SnackbarHostState,
    onProfileClick: () -> Unit,
    onCreateGroupClick: () -> Unit, 
    onSuccessMessageShown: () -> Unit,
    onGroupClick: (String) -> Unit,
    modifier: Modifier,
    navController: NavController
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MainTopBar(onProfileClick = onProfileClick)
        },
        snackbarHost = {
            MainSnackbarHost(
                hostState = snackBarHostState,
                successMessage = uiState.successMessage,
                onSuccessMessageShown = onSuccessMessageShown
            )
        }
    ) { paddingValues ->
        MainBody(
            paddingValues = paddingValues,
            newsViewModel = newsViewModel,
            selectedHobbies = selectedHobbies,
            onCreateGroupClick = onCreateGroupClick,
            groups = groups,
            onGroupClick = onGroupClick,
            navController = navController
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            AppTitle()
        },
        actions = {
            ProfileActionButton(onClick = onProfileClick)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun AppTitle(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
private fun ProfileActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    IconButton(
        onClick = onClick,
        modifier = modifier.size(spacing.extraLarge2)
    ) {
        ProfileIcon()
    }
}

@Composable
private fun ProfileIcon() {
    Icon(
        name = R.drawable.ic_account_circle,
    )
}

@Composable
private fun MainSnackbarHost(
    hostState: SnackbarHostState,
    successMessage: String?,
    onSuccessMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    MessageSnackbar(
        hostState = hostState,
        messageState = MessageSnackbarState(
            successMessage = successMessage,
            errorMessage = null,
            onSuccessMessageShown = onSuccessMessageShown,
            onErrorMessageShown = { }
        ),
        modifier = modifier
    )
}

@Composable
private fun MainBody(
    paddingValues: PaddingValues,
    newsViewModel: NewsViewModel,
    selectedHobbies: List<String>,
    onCreateGroupClick: () -> Unit,
    groups: List<GroupDataDetailed>,
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            groups.forEach { group ->
            
                Button(
                    onClick = { onGroupClick(group.joinCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column {
                        Text(text = group.groupName, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Leader: ${group.groupLeaderId?.name ?: "Unknown Leader"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun WelcomeMessage(
    modifier: Modifier = Modifier
) {
    val fontSizes = LocalFontSizes.current

    Text(
        text = stringResource(R.string.welcome),
        style = MaterialTheme.typography.bodyLarge,
        fontSize = fontSizes.extraLarge3,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}