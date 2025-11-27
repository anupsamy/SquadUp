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
import com.cpen321.squadup.data.remote.dto.Activity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
private fun EmptyActivitiesState(modifier: Modifier = Modifier) {
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
}

@Composable
private fun ActivitiesList(
    activities: List<Activity>,
    selectedActivityId: String?,
    onActivityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            //.fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        items(activities) { activity ->
            val activityInfo = ActivityInfo(
                name = activity.name,
                address = activity.address,
                rating = activity.rating,
                userRatingsTotal = activity.userRatingsTotal,
                priceLevel = activity.priceLevel,
                type = activity.type
            )
            ActivityCard(
                activity = activityInfo,
                isSelected = activity.placeId == selectedActivityId,
                onClick = { onActivityClick(activity.placeId)},
                modifier = Modifier.testTag("activityCard")
            )
        }
    }
}

@Composable
private fun SelectActivityButton(
    enabled: Boolean,
    onSelectClick: () -> Unit,
    interactionSource: MutableInteractionSource
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    Button(
        onClick = onSelectClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(if (isPressed) 0.95f else 1f),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Text("Select Activity")
    }
}

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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val handleSelectClick: () -> Unit = {
        viewModel.confirmSelection(joinCode)
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = "Activity selected successfully! Group members have been notified.",
                duration = SnackbarDuration.Short
            )
        }
        Unit
    }

    // WRAP EVERYTHING IN A BOX
    Box(modifier = modifier.fillMaxSize()) {
        if (activities.isEmpty()) {
            EmptyActivitiesState()
        } else {
            // The Column contains the List and the Button
            Column(modifier = Modifier.fillMaxSize()) {

                // 1. The List takes up all available space
                ActivitiesList(
                    activities = sortedActivities,
                    selectedActivityId = selectedActivityId,
                    onActivityClick = { viewModel.selectActivity(it) },
                    modifier = Modifier
                        .weight(1f) // This pushes the button down
                        .fillMaxWidth()
                )

                // 2. The Button sits at the bottom of the Column
                SelectActivityButton(
                    enabled = selectedActivityId != null,
                    onSelectClick = handleSelectClick,
                    interactionSource = interactionSource
                )
            }
        }

        // The Snackbar sits on top of the Column, aligned to bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


data class ActivityInfo(
    val name: String,
    val address: String,
    val rating: Double,
    val userRatingsTotal: Int,
    val priceLevel: Int,
    val type: String
)

@Composable
fun ActivityCard(
    activity: ActivityInfo,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = activity.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActivityMetaRow(
                rating = activity.rating,
                userRatingsTotal = activity.userRatingsTotal,
                priceLevel = activity.priceLevel,
                type = activity.type
            )
        }
    }
}

@Composable
private fun ActivityMetaRow(
    rating: Double,
    userRatingsTotal: Int,
    priceLevel: Int,
    type: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RatingBlock(rating = rating)
        Text(
            text = "($userRatingsTotal)",
            style = MaterialTheme.typography.bodySmall
        )
        PriceLevelText(priceLevel = priceLevel)
        Text(
            text = type,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RatingBlock(rating: Double) {
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
}

@Composable
private fun PriceLevelText(priceLevel: Int) {
    Text(
        text = "$".repeat(priceLevel.coerceAtLeast(0)),
        style = MaterialTheme.typography.bodySmall
    )
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