package com.cpen321.squadup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.ui.viewmodels.ActivityPickerViewModel
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun LeaderGroupView(
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    activityPickerViewModel: ActivityPickerViewModel,
    midpoint: com.cpen321.squadup.data.remote.dto.SquadGoal?,
    modifier: Modifier = Modifier
) {
    val isCalculatingMidpoint by groupViewModel.isCalculatingMidpoint.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Map/Status Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when {
                // State 1: Calculating midpoint
                isCalculatingMidpoint -> {
                    Text(
                        text = "Getting midpoint...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // State 2: Midpoint exists, show map
                midpoint != null -> {
                    val locations = midpoint.location?.let { location ->
                        val lat = location.lat
                        val lng = location.lng
                        if (lat != null && lng != null) listOf(LatLng(lat, lng)) else emptyList()
                    } ?: emptyList()

                    ActivityMapView(
                        locations = locations,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // State 3: Waiting, show calculate button
                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Waiting for members to join...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Or you can calculate midpoint now",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { groupViewModel.getMidpoint(group.joinCode) }) {
                            Text(text = "Find midpoint")
                        }
                    }
                }
            }
        }

        // Recalculate button - only shown when midpoint exists
        if (midpoint != null && !isCalculatingMidpoint) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { groupViewModel.getMidpoint(group.joinCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Recalculate Midpoint")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text(text = "" + midpoint + isCalculatingMidpoint)
//        }
        if (midpoint != null && !isCalculatingMidpoint) {
            ActivityPicker(
                viewModel = activityPickerViewModel,
                joinCode = group.joinCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

    }
}