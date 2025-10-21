package com.cpen321.squadup.ui.screens

import Icon
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect

import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.R
import com.cpen321.squadup.ui.components.MessageSnackbar
import com.cpen321.squadup.ui.components.MessageSnackbarState
import com.cpen321.squadup.ui.viewmodels.MainUiState
import com.cpen321.squadup.ui.viewmodels.MainViewModel
import com.cpen321.squadup.ui.theme.LocalFontSizes
import com.cpen321.squadup.ui.theme.LocalSpacing
import com.cpen321.squadup.ui.viewmodels.NewsViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp

import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupsDataAll
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.GroupLeaderUser


@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    newsViewModel: NewsViewModel,
    selectedHobbies: List<String>,
    onProfileClick: () -> Unit,
    navController: NavController 
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        mainViewModel.fetchGroups()
    }

    MainContent(
        uiState = uiState,
        newsViewModel = newsViewModel,
        selectedHobbies = selectedHobbies,
        groups = uiState.groups,
        snackBarHostState = snackBarHostState,
        onProfileClick = onProfileClick,
        onGroupClick = { groupId ->
            navController.navigate("group_details/$groupId")
        },
        onCreateGroupClick = {
            navController.navigate(NavRoutes.CREATE_GROUP)
        },
        onSuccessMessageShown = mainViewModel::clearSuccessMessage,
        modifier = Modifier
    )
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
    modifier: Modifier
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
            onGroupClick = onGroupClick
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Adjust layout to fit the button
    ) {
        // Existing content (e.g., NewsScreen)
        /*NewsScreen(
            newsViewModel = newsViewModel,
            selectedHobbies = selectedHobbies,
            modifier = Modifier.weight(1f) // Allow space for the button
        )

        Spacer(modifier = Modifier.height(16.dp))*/
        // Display the list of groups
        groups.forEach { group ->
            Button(
                onClick = { onGroupClick(group.joinCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = group.groupName)
                Text(
                    text = "Leader: ${group.groupLeaderId?.name ?: "Unknown Leader"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Add the "Create Group" button
        Button(
            onClick = onCreateGroupClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Create Group")
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