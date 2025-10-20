package com.cpen321.squadup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSuccessScreen(
    navController: NavController,
    groupName: String,
    joinCode: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Created") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) { // Navigate back to home
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "New group \"$groupName\" created!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Join Code: $joinCode",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("main") }, // Navigate back to home
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Back to Home")
                }
            }
        }
    )
}