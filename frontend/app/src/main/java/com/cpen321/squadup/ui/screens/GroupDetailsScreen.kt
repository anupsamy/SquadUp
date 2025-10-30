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
import androidx.compose.runtime.DisposableEffect
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.utils.WebSocketManager
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel, 
    profileViewModel: ProfileViewModel
) {
    val isGroupDeleted by groupViewModel.isGroupDeleted.collectAsState()
    val isGroupLeft by groupViewModel.isGroupLeft.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    val currentUserId = profileUiState.user?._id
    
    // Subscribe to WebSocket notifications when viewing a group
    LaunchedEffect(group.joinCode, currentUserId) {
        currentUserId?.let { userId ->
            Log.d("GroupDetails", "Subscribing to group ${group.joinCode} for user $userId")
            // Wait a bit for WebSocket to be connected
            kotlinx.coroutines.delay(500)
            WebSocketManager.subscribeToGroup(userId, group.joinCode)
        }
    }
    
    // Unsubscribe when leaving the screen
    DisposableEffect(group.joinCode, currentUserId) {
        onDispose {
            currentUserId?.let { userId ->
                Log.d("GroupDetails", "Unsubscribing from group ${group.joinCode} for user $userId")
                WebSocketManager.unsubscribeFromGroup(userId, group.joinCode)
            }
        }
    }

    LaunchedEffect(isGroupDeleted) {
        if (isGroupDeleted) {
            navController.navigate("main") {
                popUpTo(0) { inclusive = true } // Clear the back stack
            }
            groupViewModel.resetGroupDeletedState()
        }
    }

    LaunchedEffect(isGroupLeft) {
        if (isGroupLeft) {
            navController.navigate("main") {
                popUpTo(0) { inclusive = true } // Clear the back stack
            }
            groupViewModel.resetGroupLeftState()
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
                Spacer(modifier = Modifier.height(8.dp))
                
                // Display group members
                Text(text = "Group Members:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                
                // Show leader
                Text(
                    text = "• ${group.groupLeaderId?.name ?: "Unknown Leader"} (Leader)",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                // Show other members
                group.groupMemberIds?.forEach { member ->
                    Text(
                        text = "• ${member.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Check if current user is a member of the group (leader or regular member)
                val isGroupMember = currentUserId != null && (
                    group.groupLeaderId?.id == currentUserId || 
                    group.groupMemberIds?.any { it.id == currentUserId } == true
                )

                // Add the "Delete Group" button (only for group leader)
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

                // Add the "Leave Group" button (for all members)
                if (isGroupMember) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            currentUserId?.let { userId ->
                                groupViewModel.leaveGroup(group.joinCode, userId)
                            }
                        }
                    ) {
                        Text(text = "Leave Group")
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