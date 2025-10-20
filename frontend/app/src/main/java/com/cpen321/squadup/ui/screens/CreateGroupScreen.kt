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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun CreateGroupScreen(
    navController: NavController,
    groupViewModel: GroupViewModel = hiltViewModel() // Use Hilt to inject the ViewModel
) {
    val uiState by groupViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State variables for input fields
    var groupName by remember { mutableStateOf("") }
    var groupLeaderId by remember { mutableStateOf("") }
    var meetingDate by remember { mutableStateOf("") }
    var meetingTime by remember { mutableStateOf("") }
    var expectedPeople by remember { mutableStateOf("") }

    // State for the combined date-time string
    var meetingDateTime by remember { mutableStateOf("") }

    Scaffold(
        topBar = { /* Add top bar if needed */ },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Input for Group Name
                TextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Input for Group Leader ID
                TextField(
                    value = groupLeaderId,
                    onValueChange = { groupLeaderId = it },
                    label = { Text("Group Leader ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker for Meeting Date
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                meetingDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (meetingDate.isEmpty()) "Select Meeting Date" else "Date: $meetingDate")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Time Picker for Meeting Time
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                meetingTime = String.format("%02d:%02d", hourOfDay, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true // Use 24-hour format
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (meetingTime.isEmpty()) "Select Meeting Time" else "Time: $meetingTime")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Combine Date and Time into ISO 8601 String
                Button(
                    onClick = {
                        if (meetingDate.isNotEmpty() && meetingTime.isNotEmpty()) {
                            meetingDateTime = "${meetingDate}T${meetingTime}:00Z"
                            Toast.makeText(context, "Meeting Date-Time: $meetingDateTime", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please select both date and time", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Confirm Date-Time")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Input for Expected People
                TextField(
                    value = expectedPeople,
                    onValueChange = { expectedPeople = it },
                    label = { Text("Expected People") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Create Group Button
                Button(
                    onClick = {
                        if (meetingDateTime.isNotEmpty()) {
                            groupViewModel.createGroup(
                                groupName = groupName,
                                meetingTime = meetingDateTime,
                                groupLeader = groupLeaderId,
                                expectedPeople = expectedPeople.toIntOrNull() ?: 0
                            )
                        } else {
                            Toast.makeText(context, "Please confirm the meeting date and time", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isCreatingGroup
                ) {
                    Text(text = if (uiState.isCreatingGroup) "Creating..." else "Create Group")
                }

                // Display success or error messages
                uiState.successMessage?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.primary)
                }
                uiState.errorMessage?.let { message ->
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}