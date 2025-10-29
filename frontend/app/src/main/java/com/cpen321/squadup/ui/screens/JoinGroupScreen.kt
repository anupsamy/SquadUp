package com.cpen321.squadup.ui.screens
import Icon
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.cpen321.squadup.ui.viewmodels.MainViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.data.remote.dto.GroupUser
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.TextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var joinCode by remember { mutableStateOf("") }
    var joinGroupMessage by remember { mutableStateOf<String?>(null) }
    val profileUiState by profileViewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Group") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = joinCode,
                onValueChange = {joinCode = it},
                label = { Text("Enter Join Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // Handle join group logic here
                    if (joinCode.length == 6) {
                        
                        val currentUser = GroupUser(
                            id = profileUiState!!.user!!._id,
                            name = profileUiState!!.user!!.name,
                            email = profileUiState!!.user!!.email
                        )
                        val joinCodeString: String = joinCode
                        mainViewModel.joinGroup(
                            joinCode = joinCodeString,
                            currentUser = currentUser,
                            onSuccess = { message ->
                                joinGroupMessage= message
                                mainViewModel.fetchGroups() // Refresh groups after joining
                                joinGroupMessage = "Successfully joined group!"
                            },
                            onError = { error ->
                                joinGroupMessage = error
                                joinGroupMessage = "Error joining group."
                            }
                    )
                    } else {
                        joinGroupMessage = "Invalid join code!"
                    }
                },
                enabled = joinCode.length == 6
            ) {
                Text("Join Group")
            }
            joinGroupMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = if (it.contains("success", ignoreCase = true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}