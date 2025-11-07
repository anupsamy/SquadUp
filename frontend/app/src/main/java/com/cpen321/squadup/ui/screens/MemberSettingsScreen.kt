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
import android.content.Context
import android.app.TimePickerDialog
import android.app.DatePickerDialog
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberSettingsTopBar(
    navController: NavController,
    joinCode: String
) {
    TopAppBar(
        title = { 
            Text(
                "Member Settings", 
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            ) 
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("${NavRoutes.GROUP_DETAILS}/$joinCode") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun MemberSettingsBottomBar(
    navController: NavController,
    joinCode: String
) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("${NavRoutes.GROUP_LIST}/$joinCode") },
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
}

data class MemberSettingsState(
    val group: GroupDataDetailed,
    val currentUserId: String?,
    val address: Address?,
    val transitType: TransitType?,
    val meetingTime: String,
    val expectedPeople: String,
    val meetingTimeError: String?,
    val expectedPeopleError: String?,
    val existingMemberInfo: GroupUser?
)

// --- Shorter, safer composable signature ---
@Composable
private fun MemberSettingsContent(
    state: MemberSettingsState,
    addressPickerViewModel: AddressPickerViewModel,
    groupViewModel: GroupViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    onAddressChange: (Address?) -> Unit,
    onTransitTypeChange: (TransitType?) -> Unit,
    onMeetingTimeChange: (String, String?) -> Unit,
    onExpectedPeopleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Address picker section (unchanged behavior)
        AddressPickerSection(addressPickerViewModel, state.address) { selected ->
            onAddressChange(selected)
        }

        // Transit dropdown (unchanged behavior)
        TransitTypeDropdown(selectedType = state.transitType, onTypeSelected = onTransitTypeChange)

        // Only the group leader can edit meeting time & expected people
        if (state.group.groupLeaderId?.id == state.currentUserId) {
            MeetingTimePickerButton(
                context = context,
                meetingTime = state.meetingTime,
                meetingTimeError = state.meetingTimeError,
                onMeetingTimeChange = onMeetingTimeChange
            )

            ExpectedPeopleField(
                value = state.expectedPeople,
                error = state.expectedPeopleError,
                onValueChange = onExpectedPeopleChange
            )
        }

        // Save button (keeps same dependencies)
        SaveSettingsButton(
            address = state.address,
            transitType = state.transitType,
            meetingTime = state.meetingTime,
            expectedPeople = state.expectedPeople,
            meetingTimeError = state.meetingTimeError,
            existingMemberInfo = state.existingMemberInfo,
            addressPickerViewModel = addressPickerViewModel,
            group = state.group,
            currentUserId = state.currentUserId,
            groupViewModel = groupViewModel,
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope
        )
    }
}

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
    var expectedPeopleError by remember { mutableStateOf<String?>(null) }
    var meetingTimeError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val addressPickerViewModel: AddressPickerViewModel = hiltViewModel()

    Scaffold(
        topBar = { MemberSettingsTopBar(navController, group.joinCode) },
        bottomBar = { MemberSettingsBottomBar(navController, group.joinCode) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            MemberSettingsContent(
                group = group,
                currentUserId = currentUserId,
                address = address,
                transitType = transitType,
                meetingTime = meetingTime,
                expectedPeople = expectedPeople,
                meetingTimeError = meetingTimeError,
                expectedPeopleError = expectedPeopleError,
                addressPickerViewModel = addressPickerViewModel,
                existingMemberInfo = existingMemberInfo,
                groupViewModel = groupViewModel,
                snackbarHostState = snackbarHostState,
                coroutineScope = coroutineScope,
                onAddressChange = { address = it },
                onTransitTypeChange = { transitType = it },
                onMeetingTimeChange = { newTime, error ->
                    meetingTime = newTime
                    meetingTimeError = error
                },
                onExpectedPeopleChange = { 
                    expectedPeople = it
                    expectedPeopleError = null
                },
                modifier = Modifier.padding(paddingValues)
            )
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

private fun validateSettings(
    state: MemberSettingsState,
    addressPickerQuery: String,
): Boolean {
    // query must match the selected address formatted string
    if (addressPickerQuery != (state.address as? Address)?.formatted) return false
    // expected people must be a positive integer
    if (state.expectedPeople.toIntOrNull()?.let { it <= 0 } == true) return false
    // meetingTimeError blocks saving
    if (state.meetingTimeError != null) return false
    return true
}

@Composable
fun SaveSettingsButton(
    state: MemberSettingsState,
    addressPickerViewModel: AddressPickerViewModel,
    groupViewModel: GroupViewModel,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    Button(
        onClick = {
            // validate form
            if (!validateSettings(state, addressPickerViewModel.query)) return@Button

            // build updated members list
            val updatedMembers = state.group.groupMemberIds?.map { member ->
                if (member.id == state.currentUserId) {
                    member.copy(address = state.address, transitType = state.transitType)
                } else member
            } ?: emptyList()

            // perform update
            groupViewModel.updateMember(
                joinCode = state.group.joinCode,
                updatedMembers = updatedMembers,
                meetingTime = state.meetingTime,
                expectedPeople = state.expectedPeople.toInt(),
                onSuccess = { coroutineScope.launch { snackbarHostState.showSnackbar("Settings saved successfully!") } },
                onError = { coroutineScope.launch { snackbarHostState.showSnackbar("Error saving!") } }
            )

            // update midpoint if address/transit changed for the current user
            val existingAddr = (state.existingMemberInfo as? GroupMember)?.address
            val existingTransit = (state.existingMemberInfo as? GroupMember)?.transitType
            if (existingAddr != state.address || existingTransit != state.transitType) {
                groupViewModel.updateMidpoint(joinCode = state.group.joinCode)
            }
        },
        enabled = state.address != null && state.transitType != null
    ) {
        Text("Save")
    }
}



