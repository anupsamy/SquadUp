package com.cpen321.squadup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.remote.dto.GeoLocation
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.SquadGoal
import com.cpen321.squadup.data.remote.dto.User
import com.cpen321.squadup.ui.viewmodels.ActivityPickerViewModel
import com.cpen321.squadup.ui.viewmodels.GroupViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun MemberGroupView(
    user: User?,
    group: GroupDataDetailed,
    groupViewModel: GroupViewModel,
    midpoint: SquadGoal?,
    selectedActivity: Activity?,
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

                    val groupMemberInfo = group.groupMemberIds?.find { it.id == user?._id }

                    val userLoc = if (groupMemberInfo?.address?.lat != null && groupMemberInfo.address.lng != null) {
                        LatLng(groupMemberInfo.address.lat, groupMemberInfo.address.lng)
                    } else {
                        null
                    }


                    if (lat != null && lng != null) {
                        val mid = LatLng(lat, lng)
                        MemberActivityMapView(
                            midpoint = mid,
                            userLocation = userLoc,
                            group.selectedActivity,
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

        selectedActivity?.let { activity ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selected Activity:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ActivityCard( //TODO: just pass activity
                    name = activity.name,
                    address = activity.address,
                    rating = activity.rating ?: 0.0,
                    userRatingsTotal = activity.userRatingsTotal ?: 0,
                    priceLevel = activity.priceLevel ?: 0,
                    type = activity.type ?: "",
                    isSelected = true,
                    onClick = { /* optional click action */ },
                    modifier = Modifier.fillMaxWidth().testTag("SelectedActivityCard")
                )
            }
        }
    }
}