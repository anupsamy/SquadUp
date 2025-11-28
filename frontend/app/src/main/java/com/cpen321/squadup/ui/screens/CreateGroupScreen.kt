package com.cpen321.squadup.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.ActivityType
import com.cpen321.squadup.data.remote.dto.GroupUser
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    groupViewModel: GroupViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by groupViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val user = profileUiState.user

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) { profileViewModel.loadProfile() }

    var groupName by remember { mutableStateOf("") }
    var meetingDateTime by remember { mutableStateOf("") }
    var expectedPeople by remember { mutableStateOf("") }
    var selectedActivity by remember { mutableStateOf<ActivityType?>(null) }
    var autoMidpoint by remember { mutableStateOf(false) }

    // show snackbar on backend error coming from ViewModel
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                groupViewModel.clearMessages()
            }
        }
    }

    // Observe the success message and navigate to the success screen
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            uiState.responseData?.let { respData ->
                val joinCode = respData["joinCode"] as? String ?: ""
                val gName = respData["groupName"] as? String ?: ""
                
                if (joinCode.isNotEmpty() && gName.isNotEmpty()) {
                    navController.navigate("group_success/$gName/$joinCode")
                    groupViewModel.clearMessages() // Clear the success message after navigation
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Group") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            DateTimePickerSection(context) { meetingDateTime = it }
            val parsed = try {
                OffsetDateTime.parse(meetingDateTime).toLocalDateTime()
            } catch (e: DateTimeParseException) {
                null
            }

            AutoMidpointToggle(
                checked = autoMidpoint,
                onCheckedChange = { autoMidpoint = it }
            )

            TextField(
                value = expectedPeople,
                onValueChange = { expectedPeople = it },
                label = { Text("Expected People") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            ActivityDropdown(selected = selectedActivity) { selectedActivity = it }

            Button(
                onClick = {
                    // Input validation and display corresponding error
                    when {
                        groupName.isBlank() -> snackbarHostState.showMessage(coroutineScope, "Group name is required")
                        meetingDateTime.isBlank() -> snackbarHostState.showMessage(coroutineScope, "Meeting date and time are required")
                        parsed != null && parsed.isBefore(LocalDateTime.now()) -> snackbarHostState.showMessage(coroutineScope, "Meeting time must be in the future")
                        expectedPeople.isBlank() -> snackbarHostState.showMessage(coroutineScope, "Expected people is required")
                        expectedPeople.toIntOrNull() == null || expectedPeople.toInt() <= 0 ->
                            snackbarHostState.showMessage(coroutineScope, "Expected people must be a positive number")
                        selectedActivity == null -> snackbarHostState.showMessage(coroutineScope, "Select an activity")
                        user == null -> snackbarHostState.showMessage(coroutineScope, "User not loaded")
                        else -> {
                            val groupLeader = GroupUser(
                                id = user._id,
                                name = user.name,
                                email = user.email,
                                address = user.address,
                                transitType = user.transitType
                            )

                            groupViewModel.createGroup(
                                groupName = groupName,
                                meetingTime = meetingDateTime,
                                groupLeaderId = groupLeader,
                                expectedPeople = expectedPeople.toInt(),
                                activityType = selectedActivity!!.storedValue,
                                autoMidpoint = autoMidpoint
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("createGroupButton"),
                enabled = !uiState.isCreatingGroup
            ) {
                Text(if (uiState.isCreatingGroup) "Creating..." else "Create Group")
            }

            uiState.successMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun SnackbarHostState.showMessage(scope: CoroutineScope, msg: String) {
    scope.launch {
        this@showMessage.showSnackbar(msg)
    }
}

@Composable
fun DateTimePickerSection(context: Context, onDateTimeSelected: (String) -> Unit) {
    var meetingDate by remember { mutableStateOf("") }
    var meetingTime by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()

    Button(
        onClick = {
            DatePickerDialog(
                context,
                { _, y, m, d -> meetingDate = "%04d-%02d-%02d".format(y, m + 1, d) },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        modifier = Modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(if (meetingDate.isEmpty()) "Select Meeting Date" else "Date: $meetingDate")
    }

    Button(
        onClick = {
            TimePickerDialog(
                context,
                { _, h, min -> meetingTime = "%02d:%02d".format(h, min) },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        modifier = Modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(if (meetingTime.isEmpty()) "Select Meeting Time" else "Time: $meetingTime")
    }

    if (meetingDate.isNotEmpty() && meetingTime.isNotEmpty()) {
        onDateTimeSelected("${meetingDate}T${meetingTime}:00Z")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDropdown(selected: ActivityType?, onSelect: (ActivityType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selected?.displayName ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Activity Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ActivityType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AutoMidpointToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            "Automatic Midpoint Update",
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

