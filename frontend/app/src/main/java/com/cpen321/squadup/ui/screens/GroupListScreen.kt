package com.cpen321.squadup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    navController: NavController,
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    profileViewModel: ProfileViewModel,
) {
    var searchQuery by remember { mutableStateOf("") }
    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentUserId = profileUiState.user?._id
    val isGroupDeleted by groupViewModel.isGroupDeleted.collectAsState()
    val isGroupLeft by groupViewModel.isGroupLeft.collectAsState()

    LaunchedEffect(Unit) { profileViewModel.loadProfile() }
    HandleGroupNavigation(isGroupDeleted, isGroupLeft, navController, groupViewModel)

    Scaffold(
        topBar = { GroupListTopBar(navController, group, groupViewModel, currentUserId) },
        bottomBar = { GroupListBottomBar(navController, group) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                SearchBar(searchQuery) { searchQuery = it }
                Spacer(modifier = Modifier.height(16.dp))
                MemberList(group.groupMemberIds ?: emptyList(), searchQuery)
            }
        }
    )
}

@Composable
private fun HandleGroupNavigation(
    isGroupDeleted: Boolean,
    isGroupLeft: Boolean,
    navController: NavController,
    groupViewModel: GroupViewModel
) {
    LaunchedEffect(isGroupDeleted) {
        if (isGroupDeleted) {
            navController.navigate(NavRoutes.MAIN) { popUpTo(0) { inclusive = true } }
            groupViewModel.resetGroupDeletedState()
        }
    }
    LaunchedEffect(isGroupLeft) {
        if (isGroupLeft) {
            navController.navigate(NavRoutes.MAIN) { popUpTo(0) { inclusive = true } }
            groupViewModel.resetGroupLeftState()
        }
    }
}

@Composable
private fun GroupListTopBar(
    navController: NavController,
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    currentUserId: String?
) {
    TopAppBar(
        title = { Text("Members", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("${NavRoutes.GROUP_DETAILS}/${group.joinCode}") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            FilledTonalButton(
                onClick = { currentUserId?.let { groupViewModel.leaveGroup(group.joinCode, it) } },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) { Text("Leave Squad", style = MaterialTheme.typography.labelSmall) }

            Spacer(modifier = Modifier.width(8.dp))

            if (group.groupLeaderId?.id == currentUserId) {
                Button(
                    onClick = { groupViewModel.deleteGroup(group.joinCode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) { Text("Delete Squad", style = MaterialTheme.typography.labelSmall) }
            }
        }
    )
}

@Composable
private fun GroupListBottomBar(navController: NavController, group: GroupDataDetailed) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Squads") },
            label = { Text("Squads") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("${NavRoutes.MEMBER_SETTINGS}/${group.joinCode}") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}

@Composable
private fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search ...") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
    )
}

@Composable
private fun MemberList(members: List<GroupMember>, searchQuery: String) {
    val filteredMembers = members.filter { it.name?.contains(searchQuery, ignoreCase = true) == true }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filteredMembers) { member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Member", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(member.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
