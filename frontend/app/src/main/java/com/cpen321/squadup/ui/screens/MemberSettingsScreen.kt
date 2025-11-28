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
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import com.cpen321.squadup.ui.viewmodels.AddressPickerViewModel
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.app.TimePickerDialog
import android.app.DatePickerDialog
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.cpen321.squadup.data.remote.dto.ActivityType
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
    val existingMemberInfo: GroupUser?,
    val autoMidpoint: Boolean,
    val activityType: String
)

data class MemberSettingsHandlers(
    val addressPickerViewModel: AddressPickerViewModel,
    val groupViewModel: GroupViewModel,
    val snackbarHostState: SnackbarHostState,
    val coroutineScope: CoroutineScope,
    val onAddressChange: (Address?) -> Unit,
    val onTransitTypeChange: (TransitType?) -> Unit,
    val onMeetingTimeChange: (String, String?) -> Unit,
    val onExpectedPeopleChange: (String) -> Unit,
    val onAutoMidpointChange: (Boolean) -> Unit,
    val onActivityChange: (ActivityType) -> Unit
)

@Composable
private fun MemberSettingsContent(
    state: MemberSettingsState,
    handlers: MemberSettingsHandlers,
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
        // Address picker
        AddressPickerSection(handlers.addressPickerViewModel, state.address) { selected ->
            handlers.onAddressChange(selected)
        }

        // Transit dropdown
        TransitTypeDropdown(selectedType = state.transitType, onTypeSelected = handlers.onTransitTypeChange)

        // Only group leader can edit meetingTime & expectedPeople
        if (state.group.groupLeaderId?.id == state.currentUserId) {
            ActivityDropdown(selected = ActivityType.getActivity(state.activityType), onSelect = handlers.onActivityChange)
            ExpectedPeopleField(
                value = state.expectedPeople,
                error = state.expectedPeopleError,
                onValueChange = handlers.onExpectedPeopleChange
            )

            MeetingTimePickerButton(
                context = context,
                meetingTime = state.meetingTime,
                meetingTimeError = state.meetingTimeError,
                onMeetingTimeSelected = handlers.onMeetingTimeChange
            )
            AutoMidpointToggle(checked = state.autoMidpoint, onCheckedChange = handlers.onAutoMidpointChange)
        }
        // Save button
        SaveSettingsButton(
            state = state,
            addressPickerViewModel = handlers.addressPickerViewModel,
            groupViewModel = handlers.groupViewModel,
            snackbarHostState = handlers.snackbarHostState,
            coroutineScope = handlers.coroutineScope
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSettingsScreen(
    navController: NavController,
    group: GroupDataDetailed,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    groupViewModel: GroupViewModel = hiltViewModel(),
    addressPickerViewModel: AddressPickerViewModel = hiltViewModel()
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val currentUserId = profileUiState.user?._id
    val existingMemberInfo = remember(group, currentUserId) {
        group.groupMemberIds?.find { it.id == currentUserId }
    }

    var address by remember { mutableStateOf(existingMemberInfo?.address) }
    var transitType by remember { mutableStateOf(existingMemberInfo?.transitType) }
    var meetingTime by remember { mutableStateOf(group.meetingTime) }
    var expectedPeople by remember { mutableStateOf(group.expectedPeople.toString()) }
    var meetingTimeError by remember { mutableStateOf<String?>(null) }
    var expectedPeopleError by remember { mutableStateOf<String?>(null) }
    var autoMidpoint by remember { mutableStateOf(group.autoMidpoint) }
    var activityType by remember { mutableStateOf<String>(group.activityType) }

    val state = MemberSettingsState(
        group = group,
        currentUserId = currentUserId,
        address = address,
        transitType = transitType,
        meetingTime = meetingTime,
        expectedPeople = expectedPeople,
        meetingTimeError = meetingTimeError,
        expectedPeopleError = expectedPeopleError,
        existingMemberInfo = existingMemberInfo,
        autoMidpoint = autoMidpoint,
        activityType = activityType
    )

    val handlers = MemberSettingsHandlers(
        addressPickerViewModel = addressPickerViewModel,
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
        onAutoMidpointChange = { autoMidpoint = it },
        onActivityChange = { activityType = it.storedValue }
    )

    Scaffold(
        topBar = { MemberSettingsTopBar(navController, group.joinCode) },
        bottomBar = { MemberSettingsBottomBar(navController, group.joinCode) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        MemberSettingsContent(
            state = state,
            handlers = handlers,
            modifier = Modifier.padding(padding)
        )
    }
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
    initialValue: Address?,
    onAddressSelected: (Address?) -> Unit
) {
    AddressPicker(viewModel, initialValue = initialValue, onAddressSelected = onAddressSelected)
}

private fun validateSettings(
    state: MemberSettingsState,
    addressPickerQuery: String,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
): Boolean {
    // query must match the selected address formatted string
    if (addressPickerQuery != (state.address as? Address)?.formatted) {
        coroutineScope.launch { snackbarHostState.showSnackbar("Please select a valid address") }
        return false;
    }
    // expected people must be a positive integer
    if (state.expectedPeople.toIntOrNull()?.let { it <= 0 } == true) {
        coroutineScope.launch { snackbarHostState.showSnackbar("Expected people must be a positive number") }
        return false;
    }
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
            if (!validateSettings(
                    state = state,
                    addressPickerQuery = addressPickerViewModel.query,
                    snackbarHostState = snackbarHostState,
                    coroutineScope = coroutineScope
            )) return@Button

            // perform update
            groupViewModel.updateMember(
                joinCode = state.group.joinCode,
                address = state.address,
                transitType = state.transitType,
                meetingTime = state.meetingTime,
                expectedPeople = state.expectedPeople.toInt(),
                autoMidpoint = state.autoMidpoint,
                activityType = state.activityType,
                onSuccess = {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Settings saved successfully!") }

                    // update midpoint if address/transit changed for the current user
                    if (state.autoMidpoint) {
                        val addressUpdate = (state.existingMemberInfo as? GroupUser)?.address != state.address
                        val transitUpdate = (state.existingMemberInfo as? GroupUser)?.transitType != state.transitType
                        val activityUpdate = state.group.activityType != state.activityType
                        if (addressUpdate || transitUpdate || activityUpdate) {
                            groupViewModel.updateMidpoint(joinCode = state.group.joinCode)
                        }
                    }
                },
                onError = { coroutineScope.launch { snackbarHostState.showSnackbar("Error saving!") } }
            )
        },
        enabled = state.address != null && state.transitType != null
    ) {
        Text("Save")
    }
}