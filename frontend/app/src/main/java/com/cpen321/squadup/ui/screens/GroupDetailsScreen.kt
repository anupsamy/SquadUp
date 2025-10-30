package com.cpen321.squadup.ui.screens

import android.util.Log
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.parseMidpointString
import com.cpen321.squadup.ui.components.LeaderGroupView
import com.cpen321.squadup.ui.components.MemberGroupView
import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.ui.viewmodels.ActivityPickerViewModel
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.utils.WebSocketManager
import com.google.firebase.messaging.FirebaseMessaging


fun unsubscribeFromGroupTopic(joinCode: String) {
    FirebaseMessaging.getInstance()
        .unsubscribeFromTopic(joinCode)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM", "Unsubscribed from FCM group topic $joinCode")
            } else {
                Log.e("FCM", "Failed to unsubscribe from FCM group topic $joinCode", task.exception)
            }
        }
}

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

    val vmMidpoint by groupViewModel.midpoint.collectAsState()
    val staticMidpoint = parseMidpointString(group.midpoint)
    val midpoint = vmMidpoint ?: staticMidpoint //take from static unless updated from v,

    val vmSelectedActivity by activityPickerViewModel.selectedActivity.collectAsState()
    val staticSelectedActivity = group.selectedActivity
    val selectedActivity = vmSelectedActivity ?: staticSelectedActivity

    val currentUserId = profileUiState.user?._id
    val isLeader = group.groupLeaderId?.id == currentUserId

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    LaunchedEffect(staticSelectedActivity) {
        activityPickerViewModel.setInitialSelectedActivity(staticSelectedActivity)
    }

    LaunchedEffect(midpoint) {
        // Only load activities if the midpoint is actually available
        if (midpoint != null) {
            activityPickerViewModel.loadActivities(group.joinCode)
        }
    }


    // Subscribe to WebSocket notifications when viewing a group
    LaunchedEffect(group.joinCode, currentUserId) {
        currentUserId?.let { userId ->
            Log.d("GroupDetails", "Subscribing to group ${group.joinCode} for user $userId")
            // Wait a bit for WebSocket to be connected
            kotlinx.coroutines.delay(500)
            WebSocketManager.subscribeToGroup(userId, group.joinCode)
        }
    }

    // Unsubscribe when leaving the screen (WebSocket ONLY)
    DisposableEffect(group.joinCode, currentUserId) {
        onDispose {
            currentUserId?.let { userId ->
                Log.d("GroupDetails", "Unsubscribing from group ${group.joinCode} for user $userId (WebSocket only)")
                WebSocketManager.unsubscribeFromGroup(userId, group.joinCode)
            }
        }
    }

    LaunchedEffect(isGroupDeleted) {
        if (isGroupDeleted) {
            unsubscribeFromGroupTopic(group.joinCode)
            navController.navigate(NavRoutes.MAIN) {
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

    LaunchedEffect(Unit) {
        val membersJoined = group.groupMemberIds?.size ?: 0
        if (membersJoined == group.expectedPeople.toInt()) {
            groupViewModel.getMidpoint(group.joinCode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = group.groupName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = group.meetingTime,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(NavRoutes.MAIN) }){
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
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Condensed map + group section
                if (isLeader) {
                    LeaderGroupView(
                        group = group,
                        groupViewModel = groupViewModel,
                        midpoint = midpoint,
                        activityPickerViewModel = activityPickerViewModel,
                        selectedActivity = selectedActivity,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MemberGroupView(
                        group = group,
                        groupViewModel = groupViewModel,
                        midpoint = midpoint,
                        selectedActivity = selectedActivity,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Compact join code section
                val clipboardManager = LocalClipboardManager.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Join Code",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = group.joinCode,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(
                        onClick = { clipboardManager.setText(AnnotatedString(group.joinCode ?: "")) },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Copy", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Members + Hosted by condensed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Members",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            "${group.groupMemberIds?.size ?: 0}/${group.expectedPeople}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Host",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            group.groupLeaderId?.name ?: "Unknown",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Compact See Details button
                Button(
                    onClick = {
                        navController.navigate("${NavRoutes.GROUP_LIST}/${group.joinCode}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text("See Details", fontSize = 14.sp)
                }
            }
        }
    )
}