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
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.TransitType
import androidx.compose.ui.res.stringResource
import com.cpen321.squadup.R
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.ui.components.AddressPicker
import com.cpen321.squadup.ui.viewmodels.AddressPickerViewModel
import com.cpen321.squadup.ui.viewmodels.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinGroupScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    groupViewMode: GroupViewModel,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    var useDefaultSettings by remember { mutableStateOf(true) }
    var joinCode by remember { mutableStateOf("") }
    var joinGroupMessage by remember { mutableStateOf<String?>(null) }
    var groupExists by remember { mutableStateOf(false) }
    var groupMeetingTime by remember { mutableStateOf<String?>(null) }
    var groupName by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf(profileUiState.user?.address) }
    var transitType by remember { mutableStateOf(profileUiState.user?.transitType) }

    LaunchedEffect(Unit) {
        transitType = profileUiState.user?.transitType
    }

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
            // Join Code Input
            TextField(
                value = joinCode,
                onValueChange = { joinCode = it },
                label = { Text("Enter Join Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Check if the group exists
            Button(
                onClick = {
                    if (joinCode.length == 6) {
                        mainViewModel.checkGroupExists(
                            joinCode = joinCode,
                            onSuccess = { group ->
                                groupExists = true
                                groupName = group.groupName
                                groupMeetingTime = group.meetingTime // Fetch meeting time
                                joinGroupMessage = ""
                            },
                            onError = {
                                groupExists = false
                                joinGroupMessage = "Group not found. Please check the join code."
                            }
                        )
                    } else {
                        joinGroupMessage = "Invalid join code!"
                    }
                },
                enabled = joinCode.length == 6
            ) {
                Text("Check Group")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (groupExists) {
                groupName?.let {
                    Text(
                        text = "$it",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                groupMeetingTime?.let {
                    Text(
                        text = "Meeting Date & Time: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Please enter your location prior to event and your preferred mode of transit.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox for using default profile settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Checkbox(
                        checked = useDefaultSettings,
                        onCheckedChange = { checked ->
                            useDefaultSettings = checked
                            if (checked) {
                                address = profileUiState.user?.address
                                transitType = profileUiState.user?.transitType
                            } else {
                                address = null
                                transitType = null
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use default settings from my profile")
                }

                // Address Input
                val addressPickerViewModel: AddressPickerViewModel = hiltViewModel()
                AddressPicker(
                    viewModel = addressPickerViewModel,
                    initialValue = if (useDefaultSettings) profileUiState.user?.address else null,
                    onAddressSelected = { selectedAddressObject ->
                        if (!useDefaultSettings) address = selectedAddressObject
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Transit Type Drop-Down
                TransitTypeInputField(
                    transitType = transitType,
                    isEnabled = !useDefaultSettings,
                    onTransitTypeChange = { if (!useDefaultSettings) transitType = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Join Group Button
                Button(
                    onClick = {
                        val currentUser = GroupUser(
                            id = profileUiState.user!!._id,
                            name = profileUiState.user!!.name,
                            email = profileUiState.user!!.email,
                            address = address!!,
                            transitType = transitType
                        )
                        mainViewModel.joinGroup(
                            joinCode = joinCode,
                            currentUser = currentUser,
                            onSuccess = { message ->
                                joinGroupMessage = message
                                groupViewMode.updateMidpoint(joinCode)
                                mainViewModel.fetchGroups()     // Refresh groups after joining
                                navController.popBackStack()    // Navigate back after joining
                            },
                            onError = { error ->
                                joinGroupMessage = error
                            }
                        )
                    },
                    enabled = address != null && transitType != null
                ) {
                    Text("Join Group")
                }
            }

            // Display Join Group Message
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransitTypeInputField(
    transitType: TransitType?,
    isEnabled: Boolean,
    onTransitTypeChange: (TransitType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val transitOptions = TransitType.entries.toList() // Use enum
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && isEnabled,
        onExpandedChange = { if (isEnabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = transitType?.name?.replaceFirstChar { it.uppercase() } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.transit_type)) },
            placeholder = { Text(stringResource(R.string.transit_type_placeholder)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled
        )

        ExposedDropdownMenu(
            expanded = expanded && isEnabled,
            onDismissRequest = { expanded = false }
        ) {
            transitOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onTransitTypeChange(option) // pass enum, not string
                        expanded = false
                    }
                )
            }
        }
    }
}