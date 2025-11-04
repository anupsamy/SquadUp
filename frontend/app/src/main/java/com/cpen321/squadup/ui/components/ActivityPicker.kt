package com.cpen321.squadup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.squadup.ui.viewmodels.ActivityPickerViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
@Composable
fun ActivityPicker(
    viewModel: ActivityPickerViewModel,
    joinCode: String,
    modifier: Modifier = Modifier
) {
    val activities by viewModel.activities.collectAsState()
    val selectedActivityId by viewModel.selectedActivityId.collectAsState()
    val sortedActivities = activities.sortedWith(
        compareByDescending { it.placeId == selectedActivityId })

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (activities.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No activities found within the radius. Try a group with a new activity type",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    items(sortedActivities) { activity ->
                        ActivityCard(
                            name = activity.name,
                            address = activity.address,
                            rating = activity.rating,
                            userRatingsTotal = activity.userRatingsTotal,
                            priceLevel = activity.priceLevel,
                            type = activity.type,
                            isSelected = activity.placeId == selectedActivityId,
                            onClick = { viewModel.selectActivity(activity.placeId) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.confirmSelection(joinCode)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Activity selected successfully! Group members have been notified.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .scale(if (isPressed) 0.95f else 1f),
                    enabled = selectedActivityId != null,
                    interactionSource = interactionSource
                ) {
                    Text("Select Activity")
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Position above the button
            )
        }
    }
}
@Composable
fun ActivityCard(
    name: String,
    address: String,
    rating: Double,
    userRatingsTotal: Int,
    priceLevel: Int,
    type: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

        Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rating.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    StarRating(rating = rating)
                }
                Text(
                    text = "($userRatingsTotal)",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$".repeat(priceLevel),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
@Composable
fun StarRating(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(5) { index ->
            val starValue = index + 1
            val icon = when {
                rating >= starValue -> "★"
                rating >= starValue - 0.5 -> "⯨" // Half star
                else -> "☆"
            }
            Text(
                text = icon,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFB300) // Amber color for stars
            )
        }
    }
}