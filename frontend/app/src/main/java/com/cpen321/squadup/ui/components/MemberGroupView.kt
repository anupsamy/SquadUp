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
fun MemberGroupView(
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    midpoint: SquadGoal?,
    modifier: Modifier = Modifier
) {
    //val midpoint by groupViewModel.midpoint.collectAsState()
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
                        text = "Calculating midpoint...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // State 2: Midpoint exists, show map
                midpoint != null -> {
                    val lat = midpoint.location.lat
                    val lng = midpoint.location.lng

                    if (lat != null && lng != null) {
                        val locations = listOf(LatLng(lat, lng))
                        ActivityMapView(
                            locations = locations,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // State 3: Waiting for leader to calculate midpoint
                else -> {
                    Text(
                        text = "Waiting for group leader to calculate midpoint...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TODO: State 4 - Show selected activity details when available
        // This will be added when activity selection is implemented
    }
}