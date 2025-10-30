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
    val currentUser = profileUiState.user
    val currentUserId = profileUiState.user?._id
    val context = LocalContext.current

    val groupSpecificUser = remember(group, currentUserId) {
        group.groupMemberIds?.find { it.id == currentUserId }
    }

    var address by remember(groupSpecificUser) {
        mutableStateOf(groupSpecificUser?.address)
    }
    var transitType by remember(groupSpecificUser) {
        mutableStateOf(groupSpecificUser?.transitType)
    }

    var meetingTime by remember { mutableStateOf(group.meetingTime ?: "") }
    var expectedPeople by remember { mutableStateOf(group.expectedPeople?.toString() ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Member Settings",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = {navController.navigate("${NavRoutes.GROUP_LIST}/${group.joinCode}")},
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Squads") },
                    label = { Text("Squads") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {  },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
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
                    onAddressSelected = { address = it }
                )

                TransitTypeDropdown(
                    selectedType = transitType,
                    onTypeSelected = { transitType = it }
                )

                // Leader-only: allow updating meeting time and expected people
                if (group.groupLeaderId?.id == currentUserId) {
                    val context = LocalContext.current
                    val calendar = Calendar.getInstance()

                    // Display-only field for meeting time
                    OutlinedTextField(
                        value = meetingTime,
                        onValueChange = {},
                        label = { Text("Meeting Time") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )

                    // Button to open time picker
                    Button(
                        onClick = {
                            // Step 1: Pick Date
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

                                    // Step 2: Then pick Time
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                                            meetingTime = "${selectedDate}T${selectedTime}:00Z"
                                            Toast.makeText(context, "Meeting set to: $meetingTime", Toast.LENGTH_SHORT).show()
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
                        Text(
                            text = if (meetingTime.isEmpty())
                                "Select Meeting Date & Time"
                            else
                                "Meeting: $meetingTime"
                        )
                    }

                    OutlinedTextField(
                        value = expectedPeople,
                        onValueChange = { expectedPeople = it },
                        label = { Text("Expected People") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        val updatedMembers = group.groupMemberIds?.map { member ->
                            if (member.id == currentUserId) {
                                member.copy(address = address, transitType = transitType)
                            } else member
                        } ?: emptyList()

                        groupViewModel.updateMember(
                            joinCode = group.joinCode,
                            updatedMembers = updatedMembers,
                            meetingTime = meetingTime,
                            expectedPeople = expectedPeople.toIntOrNull() ?: 0,
                            onSuccess = { Toast.makeText(context, "Settings saved successfully!", Toast.LENGTH_SHORT).show() },
                            onError = { Toast.makeText(context, "Error saving!", Toast.LENGTH_SHORT).show()  }
                        )
                    },
                    enabled = address != null && transitType != null
                ) {
                    Text("Save")
                }
            }
        }
    )
}


