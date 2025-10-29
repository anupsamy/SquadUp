package com.cpen321.squadup.ui.screens
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.cpen321.squadup.ui.components.ActivityPicker
import com.cpen321.squadup.ui.viewmodels.ActivityPickerViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import androidx.compose.foundation.*

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.*
import com.cpen321.squadup.ui.components.ActivityMapView
import com.google.android.gms.maps.model.LatLng

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
    val activityPickerViewModel: ActivityPickerViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        // Load activities when screen opens
        activityPickerViewModel.loadActivities(group.joinCode)
        //get midpoints if available
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

                ActivityMapView(
                    locations = groupViewModel.midpoints.collectAsState().value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                )


                Spacer(modifier = Modifier.height(16.dp))

                ActivityPicker(
                    viewModel = activityPickerViewModel,
                    joinCode = group.joinCode,
                    modifier = Modifier
                        .weight(1f) // Changed from fillMaxSize
                )
            }
        }
    )
}