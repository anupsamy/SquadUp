package com.cpen321.squadup.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import java.util.*
import androidx.compose.ui.text.input.KeyboardType
import android.util.Log
import androidx.compose.foundation.clickable
import com.cpen321.squadup.data.remote.dto.GroupUser
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.cpen321.squadup.data.remote.dto.ActivityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavController,
    groupViewModel: GroupViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by groupViewModel.uiState.collectAsState()
    val profileUiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { profileViewModel.loadProfile() }

    var groupName by remember { mutableStateOf("") }
    var meetingDateTime by remember { mutableStateOf("") }
    var expectedPeople by remember { mutableStateOf("") }
    var selectedActivity by remember { mutableStateOf<ActivityType?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Create Group") }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        })},
        content = { paddingValues ->
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
                    modifier = Modifier.fillMaxWidth()
                )

                DateTimePickerSection(context) { dateTime -> meetingDateTime = dateTime }

                ExpectedPeopleInput(expectedPeople) { expectedPeople = it }

                ActivityDropdown(selectedActivity) { selectedActivity = it }

                CreateGroupButton(
                    groupName, meetingDateTime, expectedPeople, selectedActivity,
                    profileUiState.user, groupViewModel, uiState.isCreatingGroup, navController
                )

                uiState.successMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.primary) }
                uiState.errorMessage?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
            }
        }
    )
}

@Composable
fun DateTimePickerSection(context: Context, onDateTimeSelected: (String) -> Unit) {
    var meetingDate by remember { mutableStateOf("") }
    var meetingTime by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    Button(onClick = {
        DatePickerDialog(context, { _, y, m, d -> meetingDate = String.format("%04d-%02d-%02d", y, m+1, d) },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }, modifier = Modifier.fillMaxWidth()) { Text(if (meetingDate.isEmpty()) "Select Meeting Date" else "Date: $meetingDate") }

    Button(onClick = {
        TimePickerDialog(context, { _, h, min -> meetingTime = String.format("%02d:%02d", h, min) },
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        ).show()
    }, modifier = Modifier.fillMaxWidth()) { Text(if (meetingTime.isEmpty()) "Select Meeting Time" else "Time: $meetingTime") }

    Button(onClick = {
        if (meetingDate.isNotEmpty() && meetingTime.isNotEmpty()) {
            val dt = "${meetingDate}T${meetingTime}:00Z"
            onDateTimeSelected(dt)
            Toast.makeText(context, "Meeting Date-Time: $dt", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(context, "Please select both date and time", Toast.LENGTH_SHORT).show()
    }, modifier = Modifier.fillMaxWidth()) { Text("Confirm Date-Time") }
}

@Composable
fun ExpectedPeopleInput(value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Expected People") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun ActivityDropdown(selected: ActivityType?, onSelect: (ActivityType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected?.displayName ?: "Select Activity Type",
            onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ActivityType.values().forEach { type ->
                DropdownMenuItem(text = { Text(type.displayName) }, onClick = { onSelect(type); expanded = false })
            }
        }
    }
}

data class GroupForm(
    val name: String,
    val meetingDateTime: String,
    val expectedPeople: String,
    val selectedActivity: ActivityType?
)

data class UserInfo(
    val user: User?
)

@Composable
fun CreateGroupButton(
    groupForm: GroupForm,
    userInfo: UserInfo,
    groupViewModel: GroupViewModel,
    isCreating: Boolean,
    navController: NavController
) {
    val context = LocalContext.current
    Button(
        onClick = {
            if (groupForm.meetingDateTime.isEmpty() || userInfo.user == null) {
                Toast.makeText(context, "Please confirm the meeting date and time", Toast.LENGTH_SHORT).show()
                return@Button
            }
            val groupLeader = GroupUser(
                userInfo.user._id,
                userInfo.user.name,
                userInfo.user.email,
                userInfo.user.address,
                userInfo.user.transitType
            )
            groupViewModel.createGroup(
                groupForm.name,
                groupForm.meetingDateTime,
                groupLeader,
                groupForm.expectedPeople.toIntOrNull() ?: 0,
                groupForm.selectedActivity?.storedValue ?: ""
            )
        },
        modifier = Modifier.fillMaxWidth().testTag("createGroupButton"),
        enabled = !isCreating
    ) { Text(if (isCreating) "Creating..." else "Create Group") }
}
