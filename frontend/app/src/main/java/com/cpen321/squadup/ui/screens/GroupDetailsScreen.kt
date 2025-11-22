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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
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
    val isDeleted by groupViewModel.isGroupDeleted.collectAsState()
    val isLeft by groupViewModel.isGroupLeft.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val activityPickerVM: ActivityPickerViewModel = hiltViewModel()
    
    val midpoint = groupViewModel.midpoint.collectAsState().value ?: parseMidpointString(group.midpoint)
    val selectedActivity = activityPickerVM.selectedActivity.collectAsState().value ?: group.selectedActivity
    val currentUserId = profileUiState.user?._id
    val isLeader = group.groupLeaderId?.id == currentUserId

    fun refresh() {
        navController.navigate(NavRoutes.MAIN) { popUpTo(NavRoutes.MAIN) { inclusive = false } }
        navController.navigate("${NavRoutes.GROUP_DETAILS}/${group.joinCode}")
    }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        activityPickerVM.setInitialSelectedActivity(group.selectedActivity)
        if ((group.groupMemberIds?.size ?: 0) == group.expectedPeople.toInt()) groupViewModel.getMidpoint(group.joinCode)
        if (midpoint != null) activityPickerVM.loadActivities(group.joinCode)
    }

    LaunchedEffect(group.joinCode, currentUserId) { currentUserId?.let { WebSocketManager.subscribeToGroup(it, group.joinCode) } }
    DisposableEffect(group.joinCode, currentUserId) { onDispose { currentUserId?.let { WebSocketManager.unsubscribeFromGroup(it, group.joinCode) } } }

    LaunchedEffect(isDeleted) { if (isDeleted) { unsubscribeFromGroupTopic(group.joinCode); navController.navigate(NavRoutes.MAIN) { popUpTo(0) { inclusive = true } }; groupViewModel.resetGroupDeletedState() } }
    LaunchedEffect(isLeft) { if (isLeft) { navController.navigate(NavRoutes.MAIN) { popUpTo(0) { inclusive = true } }; groupViewModel.resetGroupLeftState() } }

    Scaffold(
        topBar = { GroupDetailsTopBar(navController, group.joinCode, group.groupName, group.meetingTime, ::refresh) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLeader) LeaderGroupView(group, groupViewModel, midpoint = midpoint, activityPickerViewModel = activityPickerVM, selectedActivity = selectedActivity, modifier = Modifier.weight(1f))
            else MemberGroupView(profileUiState.user, group, groupViewModel, midpoint, selectedActivity, Modifier.weight(1f))

            Spacer(Modifier.height(16.dp))
            JoinCodeSection(group.joinCode)
            Spacer(Modifier.height(8.dp))
            MembersHostSection(group)
            Spacer(Modifier.height(16.dp))
            SeeDetailsButton(navController, group.joinCode)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailsTopBar(nav: NavController, joinCode: String, name: String, time: String, refresh: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = { IconButton({ nav.navigate(NavRoutes.MAIN) }) { Icon(Icons.Default.ArrowBack, "Back") } },
        actions = { IconButton(onClick = refresh) { Icon(Icons.Default.Refresh, "Refresh") } }
    )
}

@Composable private fun JoinCodeSection(joinCode: String) {
    val clipboard = LocalClipboardManager.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Join Code",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                joinCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(
            onClick = { clipboard.setText(AnnotatedString(joinCode)) },
            modifier = Modifier.height(32.dp)
        ) {
            Text("Copy", fontSize = 12.sp)
        }
    }
}

@Composable private fun MembersHostSection(group: GroupDataDetailed) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Members",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${group.groupMemberIds?.size ?: 0}/${group.expectedPeople}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "Host",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                group.groupLeaderId?.name ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SeeDetailsButton(nav: NavController, joinCode: String) {
    Button(
        onClick = { nav.navigate("${NavRoutes.GROUP_LIST}/$joinCode") },
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("Group details", fontSize = 14.sp)
    }
}
