package com.cpen321.squadup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.SquadGoal
import com.cpen321.squadup.ui.navigation.NavRoutes
import com.cpen321.squadup.ui.viewmodels.ActivityPickerViewModel
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.google.android.gms.maps.model.LatLng

private fun extractMidpointLocations(
    midpoint: com.cpen321.squadup.data.remote.dto.SquadGoal?
): List<LatLng> {
    return midpoint?.location?.let { location ->
        val lat = location.lat
        val lng = location.lng
        if (lat != null && lng != null) listOf(LatLng(lat, lng)) else emptyList()
    } ?: emptyList()
}

@Composable
private fun MapStatusBox(
    isCalculatingMidpoint: Boolean,
    midpoint: com.cpen321.squadup.data.remote.dto.SquadGoal?,
    activities: List<Activity>,
    onFindMidpoint: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCalculatingMidpoint -> {
                    Text(
                        text = "Getting midpoint...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                midpoint != null -> {
                    val locations = extractMidpointLocations(midpoint)
                    LeaderActivityMapView(
                        locations = locations,
                        activities = activities,
                        modifier = Modifier.fillMaxSize().testTag("LeaderMapView")
                    )
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Waiting for members to join...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Or you can calculate midpoint now",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onFindMidpoint) {
                            Text(
                                text = "Find midpoint",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecalculateButton(
    onRecalculate: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onRecalculate,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = "Recalculate Midpoint")
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun LeaderGroupView(
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    activityPickerViewModel: ActivityPickerViewModel,
    selectedActivity: Activity?,
    midpoint: SquadGoal?,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCalculatingMidpoint by groupViewModel.isCalculatingMidpoint.collectAsState()
    val activities by activityPickerViewModel.activities.collectAsState()

    LaunchedEffect(Unit) {
        activityPickerViewModel.loadActivities(group.joinCode)
    }

    val handleMidpointCalculation = {
        groupViewModel.updateMidpoint(group.joinCode)
        activityPickerViewModel.loadActivities(group.joinCode)
        onUpdate()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        MapStatusBox(
            isCalculatingMidpoint = isCalculatingMidpoint,
            midpoint = midpoint,
            activities = activities,
            onFindMidpoint = handleMidpointCalculation
        )

        if (midpoint != null && !isCalculatingMidpoint) {
            RecalculateButton(onRecalculate = handleMidpointCalculation)
        }

        if (midpoint != null && !isCalculatingMidpoint) {
            ActivityPicker(
                viewModel = activityPickerViewModel,
                joinCode = group.joinCode,
                onSelectActivity = { onUpdate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("ActivityPicker")
            )
        }
    }
}
