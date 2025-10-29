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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalClipboardManager
import com.cpen321.squadup.data.remote.dto.GroupUser
import com.cpen321.squadup.ui.navigation.NavRoutes
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupViewScreen(
    navController: NavController,
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    //groupViewModel: GroupViewModelPlaceholder,
    profileViewModel: ProfileViewModel,
    //profileViewModel: ProfileViewModelDummy
) {
    val isGroupDeleted by groupViewModel.isGroupDeleted.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }
    val currentUserId = profileUiState.user?._id

    LaunchedEffect(isGroupDeleted) {
        if (isGroupDeleted) {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(0) { inclusive = true } // Clear the back stack
            }
            groupViewModel.resetGroupDeletedState()
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
                // Map placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (group.groupLeaderId?.id != currentUserId) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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

                            Button(onClick = {
                                /* handle find midpoint action */
                            }) {
                                Text(text = "Find midpoint")
                            }
                        }
                    } else {
                        Text(
                            text = "Waiting for members to join...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
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
		// Add the "Delete Group" button
//                if (group.groupLeaderId?.id == currentUserId) {
//                    Button(
//                        onClick = {
//                            groupViewModel.deleteGroup(group.joinCode)
//                        },
//                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//                    ) {
//                        Text(text = "Delete Group")

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
                        navController.currentBackStackEntry?.savedStateHandle?.set("group", group)
                        navController.navigate(NavRoutes.GROUP_DETAILS)
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


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGroupDetailsScreen() {
    val navController = rememberNavController()

// Mock data
    val leader = GroupUser(
        id = "1",
        name = "Alice",
        email = "alice@example.com"
    )

// Group members
    val members = listOf(
        GroupUser(id = "2", name = "Bob", email = "bob@example.com"),
        GroupUser(id = "3", name = "Charlie", email = "charlie@example.com"),
        GroupUser(id = "4", name = "Diana", email = "diana@example.com"),
        GroupUser(id = "5", name = "Ethan", email = "ethan@example.com")
    )

    val fakeGroup = GroupDataDetailed(
        groupName = "Study Buddies",
        meetingTime = "Fridays 5 PM",
        joinCode = "ABC123",
        groupLeaderId = leader,
        expectedPeople = 5,
        groupMemberIds = members
    )


    // Dummy ViewModels for preview
    val dummyGroupViewModel = object : GroupViewModelDummy() {}
    val dummyProfileViewModel = object : ProfileViewModelDummy() {}

//    GroupDetailsScreen(navController, fakeGroup, dummyGroupViewModel, dummyProfileViewModel)
}

// --- Dummy viewmodels for preview ---
open class GroupViewModelDummy : GroupViewModelPlaceholder() {
    override val isGroupDeleted: StateFlow<Boolean> = MutableStateFlow(false)
}

open class ProfileViewModelDummy : ProfileViewModelPlaceholder() {
    override val uiState: StateFlow<ProfileUiStatePlaceholder> =
        MutableStateFlow(ProfileUiStatePlaceholder())
}

// --- Simple placeholders so preview compiles ---
open class GroupViewModelPlaceholder {
    open val isGroupDeleted: StateFlow<Boolean> = MutableStateFlow(false)
    open fun resetGroupDeletedState() {}
}

open class ProfileViewModelPlaceholder {
    open val uiState: StateFlow<ProfileUiStatePlaceholder> =
        MutableStateFlow(ProfileUiStatePlaceholder())
    open fun loadProfile() {}
}

data class ProfileUiStatePlaceholder(val user: DummyUser? = null)
data class DummyUser(val _id: String? = null)