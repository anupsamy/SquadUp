package com.cpen321.squadup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.TransitType
import com.cpen321.squadup.data.remote.dto.GroupUser
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.components.AddressPicker
import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.ui.viewmodels.MainViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.ui.viewmodels.AddressPickerViewModel
import androidx.compose.ui.platform.LocalContext
import android.app.TimePickerDialog
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSettingsScreen(
    navController: NavController,
    group: GroupDataDetailed,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    groupViewModel: GroupViewModel,
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    val currentUserId = profileUiState.user?._id
    val existingMemberInfo = remember(group, currentUserId) {
        group.groupMemberIds?.find { it.id == currentUserId }
    }

    var address by remember { mutableStateOf(existingMemberInfo?.address) }
    var transitType by remember { mutableStateOf(existingMemberInfo?.transitType) }
    var meetingTime by remember { mutableStateOf(group.meetingTime ?: "") }
    var expectedPeople by remember { mutableStateOf(group.expectedPeople?.toString() ?: "") }

    var addressError by remember { mutableStateOf<String?>(null) }
    var expectedPeopleError by remember { mutableStateOf<String?>(null) }
    var meetingTimeError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val addressPickerViewModel: AddressPickerViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("${NavRoutes.GROUP_DETAILS}/${group.joinCode}") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("${NavRoutes.GROUP_LIST}/${group.joinCode}") },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Squads") },
                    label = { Text("Squads") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AddressPickerSection(addressPickerViewModel, address) { selected ->
                    address = selected
                    addressError = null
                }
                TransitTypeDropdown(selectedType = transitType) { transitType = it }
                if (group.groupLeaderId?.id == currentUserId) {
                    MeetingTimePickerButton(
                        context, meetingTime, meetingTimeError
                    ) { newTime, error ->
                        meetingTime = newTime
                        meetingTimeError = error
                    }
                    ExpectedPeopleField(expectedPeople, expectedPeopleError) { expectedPeople = it; expectedPeopleError = null }
                }
                SaveSettingsButton(
                    address, transitType, meetingTime, expectedPeople, meetingTimeError,
                    existingMemberInfo, addressPickerViewModel, group, currentUserId,
                    groupViewModel, snackbarHostState, coroutineScope
                )
            }
        }
    )
}

@Composable
fun MeetingTimePickerButton(
    context: Context,
    meetingTime: String,
    meetingTimeError: String?,
    onMeetingTimeSelected: (String, String?) -> Unit
) {
    val calendar = Calendar.getInstance()
    Column {
        OutlinedTextField(
            value = meetingTime,
            onValueChange = {},
            label = { Text("Meeting Time") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            isError = meetingTimeError != null,
            supportingText = { if (meetingTimeError != null) Text(meetingTimeError, color = MaterialTheme.colorScheme.error) }
        )
        Button(onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val selectedCalendar = Calendar.getInstance().apply { set(year, month, day, hour, minute, 0) }
                            val now = Calendar.getInstance().timeInMillis
                            if (selectedCalendar.timeInMillis < now) {
                                onMeetingTimeSelected(meetingTime, "Meeting time must be in the future")
                            } else {
                                val newTime = String.format("%04d-%02d-%02d%02d:%02d:00Z", year, month + 1, day, hour, minute)
                                onMeetingTimeSelected(newTime, null)
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }, modifier = Modifier.fillMaxWidth()) { Text("Click to update Meeting Date & Time") }
    }
}

@Composable
fun ExpectedPeopleField(value: String, error: String?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Expected People") },
        modifier = Modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = { if (error != null) Text(error, color = MaterialTheme.colorScheme.error) }
    )
}

@Composable
fun AddressPickerSection(
    viewModel: AddressPickerViewModel,
    initialValue: Any?,
    onAddressSelected: (Any?) -> Unit
) {
    AddressPicker(viewModel, initialValue, onAddressSelected)
}

@Composable
fun SaveSettingsButton(
    address: Any?, transitType: Any?, meetingTime: String, expectedPeople: String, meetingTimeError: String?,
    existingMemberInfo: Any?, addressPickerViewModel: AddressPickerViewModel,
    group: GroupDataDetailed, currentUserId: String?,
    groupViewModel: GroupViewModel, snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope
) {
    Button(
        onClick = {
            var isValid = true
            if (addressPickerViewModel.query != (address as? Address)?.formatted || addressPickerViewModel.query.isBlank()) {
                isValid = false
            }
            if (expectedPeople.toIntOrNull()?.let { it <= 0 } == true) isValid = false
            if (meetingTimeError != null) isValid = false
            if (!isValid) return@Button

            val updatedMembers = group.groupMemberIds?.map { member ->
                if (member.id == currentUserId) member.copy(address = address, transitType = transitType) else member
            } ?: emptyList()

            groupViewModel.updateMember(
                joinCode = group.joinCode,
                updatedMembers = updatedMembers,
                meetingTime = meetingTime,
                expectedPeople = expectedPeople.toInt(),
                onSuccess = { coroutineScope.launch { snackbarHostState.showSnackbar("Settings saved successfully!") } },
                onError = { coroutineScope.launch { snackbarHostState.showSnackbar("Error saving!") } }
            )
            if ((existingMemberInfo as? GroupMember)?.address != address || (existingMemberInfo as? GroupMember)?.transitType != transitType) {
                groupViewModel.updateMidpoint(joinCode = group.joinCode)
            }
        },
        enabled = address != null && transitType != null
    ) { Text("Save") }
}


