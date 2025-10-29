package com.cpen321.squadup.ui.screens
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalClipboardManager
import com.cpen321.squadup.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    navController: NavController,
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    profileViewModel: ProfileViewModel,
) {
    val isGroupDeleted by groupViewModel.isGroupDeleted.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val midpoint by groupViewModel.midpoint.collectAsState()
    val isCalculatingMidpoint by groupViewModel.isCalculatingMidpoint.collectAsState()
    val currentUserId = profileUiState.user?._id

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        groupViewModel.resetMidpoint()
    }

    LaunchedEffect(isGroupDeleted) {
        if (isGroupDeleted) {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true } // Clear the back stack
            }
            groupViewModel.resetGroupDeletedState()
        }
    }

    LaunchedEffect(group.groupMemberIds?.size, group.expectedPeople) {
        val membersJoined = group.groupMemberIds?.size ?: 0
        if (membersJoined == group.expectedPeople) {
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
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = group.meetingTime ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )

                        }
                    }
                },
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isCalculatingMidpoint -> {
                            Text(
                                text = "Getting midpoint...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        midpoint != null -> {
                            Text(
                                text = "Midpoint: ${midpoint?.location}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        group.groupLeaderId?.id == currentUserId -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Waiting for members to join...",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Or you can calculate midpoint now",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { groupViewModel.getMidpoint(group.joinCode) }) {
                                    Text(text = "Find midpoint")
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "Waiting for members to join...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Join code + copy button
                val clipboardManager = LocalClipboardManager.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Join Code",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = group.joinCode,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    OutlinedButton(onClick = {
                        clipboardManager.setText(
                            AnnotatedString(group.joinCode ?: "")
                        )
                    }) {
                        Text("Copy")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Member status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Members",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${group.groupMemberIds?.size ?: 0}/${group.expectedPeople} members",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hosted by
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hosted By",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = group.groupLeaderId?.name ?: "Unknown Leader",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // See Details button
                Button(
                    onClick = { // Suppose you already have the group object
                        navController.navigate("${NavRoutes.GROUP_LIST}/${group.joinCode}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "See Group Details",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

            }
        }
    )
}