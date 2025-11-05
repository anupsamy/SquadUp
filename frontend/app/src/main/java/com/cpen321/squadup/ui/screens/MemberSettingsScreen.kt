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
    val context = LocalContext.current

    val existingMemberInfo = remember(group, currentUserId) {
        group.groupMemberIds?.find { it.id == currentUserId }
    }
    var address by remember { mutableStateOf(existingMemberInfo?.address) }
    var transitType by remember { mutableStateOf(existingMemberInfo?.transitType) }
    var meetingTime by remember { mutableStateOf(group.meetingTime ?: "") }
    var expectedPeople by remember { mutableStateOf(group.expectedPeople?.toString() ?: "") }

    // Error states:
    var addressError by remember { mutableStateOf<String?>(null) }
    var expectedPeopleError by remember { mutableStateOf<String?>(null) }
    var meetingTimeError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
                val addressPickerViewModel: AddressPickerViewModel = hiltViewModel()
                AddressPicker(
                    viewModel = addressPickerViewModel,
                    initialValue = address,
                    onAddressSelected = { picked ->
                        address = picked
                        addressError = null
                    }
                )
                if (addressError != null) {
                    Text(text = addressError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                TransitTypeDropdown(
                    selectedType = transitType,
                    onTypeSelected = { transitType = it }
                )

                if (group.groupLeaderId?.id == currentUserId) {
                    OutlinedTextField(
                        value = meetingTime,
                        onValueChange = {},
                        label = { Text("Meeting Time") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        isError = meetingTimeError != null,
                        supportingText = {
                            if (meetingTimeError != null) Text(meetingTimeError!!, color = MaterialTheme.colorScheme.error)
                        },
                    )

                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                                            val newMeetingTime = "$selectedDate$selectedTime:00Z"
                                            val now = Calendar.getInstance().timeInMillis
                                            val selectedCalendar = Calendar.getInstance().apply {
                                                set(year, month, dayOfMonth, hourOfDay, minute, 0)
                                            }
                                            val newMeetingTimeMillis = selectedCalendar.timeInMillis
                                            if (newMeetingTimeMillis < now) {
                                                meetingTimeError = "Meeting time must be in the future"
                                            } else {
                                                meetingTimeError = null
                                                meetingTime = newMeetingTime
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Meeting set to: $meetingTime")
                                                }
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
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Click to update Meeting Date & Time")
                    }

                    OutlinedTextField(
                        value = expectedPeople,
                        onValueChange = {
                            expectedPeople = it
                            expectedPeopleError = null
                        },
                        label = { Text("Expected People") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = expectedPeopleError != null,
                        supportingText = {
                            if (expectedPeopleError != null) Text(expectedPeopleError!!, color = MaterialTheme.colorScheme.error)
                        }
                    )
                }

                Button(
                    onClick = {
                        var isValid = true
                        val addressChanged = existingMemberInfo?.address != address
                        val transitChanged = existingMemberInfo?.transitType != transitType

                        if (addressPickerViewModel.query != address?.formatted || addressPickerViewModel.query.isBlank()) {
                            addressError = "Please select a valid address"
                            isValid = false
                        }
                        if (expectedPeople.toIntOrNull() == null || expectedPeople.toIntOrNull()!! <= 0) {
                            expectedPeopleError = "Expected people must be a positive number"
                            isValid = false
                        }
                        if (meetingTimeError != null) {
                            isValid = false
                        }
                        if (!isValid) return@Button

                        val updatedMembers = group.groupMemberIds?.map { member ->
                            if (member.id == currentUserId) {
                                member.copy(address = address, transitType = transitType)
                            } else member
                        } ?: emptyList()

                        groupViewModel.updateMember(
                            joinCode = group.joinCode,
                            updatedMembers = updatedMembers,
                            meetingTime = meetingTime,
                            expectedPeople = expectedPeople.toInt(),
                            onSuccess = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Settings saved successfully!")
                                }
                            },
                            onError = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error saving!")
                                }
                            }
                        )

                        if (addressChanged || transitChanged) {
                            groupViewModel.updateMidpoint(joinCode = group.joinCode)
                        }
                    },
                    enabled = address != null && transitType != null
                ) {
                    Text("Save")
                }
            }
        }
    )
}

