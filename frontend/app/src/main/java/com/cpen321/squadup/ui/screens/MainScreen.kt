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
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.R
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.ui.components.MessageSnackbar
import com.cpen321.squadup.ui.components.MessageSnackbarState
import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.ui.theme.LocalFontSizes
import com.cpen321.squadup.ui.theme.LocalSpacing
import com.cpen321.squadup.ui.viewmodels.MainUiState
import com.cpen321.squadup.ui.viewmodels.MainViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.utils.WebSocketManager
import com.google.firebase.messaging.FirebaseMessaging


@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onProfileClick: () -> Unit,
    navController: NavController
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    val currentUserId = profileUiState.user?._id
    val filteredGroups = uiState.groups.filterUserGroups(currentUserId)

    LaunchedEffect(Unit) { mainViewModel.fetchGroups(); profileViewModel.loadProfile() }
    LaunchedEffect(currentUserId, filteredGroups) { subscribeToUserGroups(currentUserId, filteredGroups) }

    Scaffold(
        topBar = { MainTopBar(onProfileClick) },
        snackbarHost = { MainSnackbarHost(snackBarHostState, uiState.successMessage, mainViewModel::clearSuccessMessage) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MainBody(
                modifier = Modifier.weight(1f),
                paddingValues = PaddingValues(0.dp),
                onCreateGroupClick = { navController.navigate(NavRoutes.CREATE_GROUP) },
                groups = filteredGroups,
                onGroupClick = { navController.navigate("${NavRoutes.GROUP_DETAILS}/$it") },
                navController = navController
            )
            Spacer(modifier = Modifier.height(16.dp))
            BottomActionButtons(navController)
        }
    }
}

private fun List<GroupDataDetailed>.filterUserGroups(userId: String?) = filter { group ->
    group.groupLeaderId?.id == userId || group.groupMemberIds?.any { it.id == userId } == true
}

private fun subscribeToUserGroups(userId: String?, groups: List<GroupDataDetailed>) {
    userId ?: return
    groups.forEach { group ->
        WebSocketManager.subscribeToGroup(userId, group.joinCode)
        subscribeToGroupTopic(group.joinCode)
    }
}

@Composable
private fun BottomActionButtons(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Your squads",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Create or join a new squad",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = { navController.navigate(NavRoutes.CREATE_GROUP) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    "Create Group",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { navController.navigate(NavRoutes.JOIN_GROUP) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Filled.Group,
                    "Join Group",
                    tint = MaterialTheme.colorScheme.primary
                )
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

data class MainContentState(
    val uiState: MainUiState,
    val groups: List<GroupDataDetailed>,
    val snackBarHostState: SnackbarHostState,
    val navController: NavController
)

data class MainContentActions(
    val onProfileClick: () -> Unit,
    val onCreateGroupClick: () -> Unit,
    val onSuccessMessageShown: () -> Unit,
    val onGroupClick: (String) -> Unit
)

@Composable
private fun MainContent(
    state: MainContentState,
    actions: MainContentActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { MainTopBar(onProfileClick = actions.onProfileClick) },
        snackbarHost = {
            MainSnackbarHost(
                hostState = state.snackBarHostState,
                successMessage = state.uiState.successMessage,
                onSuccessMessageShown = actions.onSuccessMessageShown
            )
        }
    ) { paddingValues ->
        MainBody(
            paddingValues = paddingValues,
            onCreateGroupClick = actions.onCreateGroupClick,
            groups = state.groups,
            onGroupClick = actions.onGroupClick,
            navController = state.navController
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            groups.forEach { group ->
                Card(
                    onClick = { onGroupClick(group.joinCode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("groupButton"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = group.groupName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Leader • ${group.groupLeaderId?.name ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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

fun subscribeToGroupTopic(joinCode: String) {
    FirebaseMessaging.getInstance()
        .subscribeToTopic(joinCode)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM", "Subscribed to FCM group topic $joinCode")
            } else {
                Log.e("FCM", "Failed to subscribe to FCM group topic $joinCode", task.exception)
            }
        }
}