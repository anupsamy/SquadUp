package com.cpen321.squadup.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.ui.components.ActivityMapView
import com.cpen321.squadup.ui.components.ActivityPicker
import com.cpen321.squadup.ui.viewmodels.ActivityPickerViewModel
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
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
    val isGroupLeft by groupViewModel.isGroupLeft.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val activityPickerViewModel: ActivityPickerViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        activityPickerViewModel.loadActivities(group.joinCode)
        groupViewModel.getMidpoints(group.joinCode)
    }

    val currentUserId = profileUiState.user?._id

    LaunchedEffect(isGroupDeleted) {
        if (isGroupDeleted) {
            navController.navigate("main") {
                popUpTo(0) { inclusive = true }
            }
            groupViewModel.resetGroupDeletedState()
        }
    }

    LaunchedEffect(isGroupLeft) {
        if (isGroupLeft) {
            navController.navigate("main") {
                popUpTo(0) { inclusive = true }
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
            ) {
                // Group info section - compact
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = group.groupName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Meeting: ${group.meetingTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Code: ${group.joinCode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Leader: ${group.groupLeaderId?.name ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Members: ${group.expectedPeople}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Display group members
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• ${group.groupLeaderId?.name ?: "Unknown Leader"} (Leader)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    group.groupMemberIds?.forEach { member ->
                        Text(
                            text = "• ${member.name}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Check if current user is a member of the group (leader or regular member)
                    val isGroupMember = currentUserId != null && (
                        group.groupLeaderId?.id == currentUserId || 
                        group.groupMemberIds?.any { it.id == currentUserId } == true
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (group.groupLeaderId?.id == currentUserId) {
                            Button(
                                onClick = { groupViewModel.deleteGroup(group.joinCode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(text = "Delete", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        if (isGroupMember) {
                            Button(
                                onClick = {
                                    currentUserId?.let { userId ->
                                        groupViewModel.leaveGroup(group.joinCode, userId)
                                    }
                                },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(text = "Leave", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(text = "Back", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Map section
                ActivityMapView(
                    locations = groupViewModel.midpoints.collectAsState().value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.5f)
                )

                // Activity picker section
                ActivityPicker(
                    viewModel = activityPickerViewModel,
                    joinCode = group.joinCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    )
}