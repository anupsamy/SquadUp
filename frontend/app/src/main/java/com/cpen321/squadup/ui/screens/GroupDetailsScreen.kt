package com.cpen321.squadup.ui.screens
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.data.remote.dto.GroupData
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel, 
    profileViewModel: ProfileViewModel
) {
    val isGroupDeleted by groupViewModel.isGroupDeleted.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    val currentUserId = profileUiState.user?._id

    LaunchedEffect(isGroupDeleted) {
        if (isGroupDeleted) {
            navController.navigate("main") {
                popUpTo(0) { inclusive = true } // Clear the back stack
            }
            groupViewModel.resetGroupDeletedState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(text = "Group: ${group.groupName}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Meeting Time: ${group.meetingTime}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Join Code: ${group.joinCode}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Group Leader: ${group.groupLeaderId?.name ?: "Unknown Leader"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Expected People: ${group.expectedPeople}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Add the "Delete Group" button
                if (group.groupLeaderId?.id == currentUserId) {
                    Button(
                        onClick = {
                            groupViewModel.deleteGroup(group.joinCode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(text = "Delete Group")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { navController.navigateUp() }) {
                    Text(text = "Back to Groups")
                }
            }
        }
    )
}