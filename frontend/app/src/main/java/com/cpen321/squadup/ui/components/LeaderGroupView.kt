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
import com.cpen321.squadup.data.remote.dto.GeoLocation
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.SquadGoal
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun LeaderGroupView(
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    midpoint: SquadGoal?,
    modifier: Modifier = Modifier
) {
    val isCalculatingMidpoint by groupViewModel.isCalculatingMidpoint.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Map/Status Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
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
                    val locations = midpoint.let { midpoint ->
                        val lat = midpoint.location.lat
                        val lng = midpoint.location.lng
                        if (lat != null && lng != null) listOf(LatLng(lat, lng)) else emptyList()
                    }

                    ActivityMapView(
                        locations = locations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)
                    )

                    // TODO: Add recalculate button when midpoint exists
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

        Spacer(modifier = Modifier.height(16.dp))

        // TODO: Add activity picker below map
        // TODO: Show selected activity details when available
    }
}