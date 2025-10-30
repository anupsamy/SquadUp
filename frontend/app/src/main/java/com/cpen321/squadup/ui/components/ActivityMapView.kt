package com.cpen321.squadup.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cpen321.squadup.data.remote.dto.Activity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ActivityMapView(
    locations: List<LatLng>,
    activities: List<Activity>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    // Combine all coordinates
    val allPoints = remember(locations, activities) {
        (locations + activities.map { LatLng(it.latitude, it.longitude) })
            .filter { it.latitude != 0.0 && it.longitude != 0.0 } // avoid invalid coords
    }

    // Move camera to fit all markers once map is ready
    LaunchedEffect(allPoints) {
        if (allPoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            allPoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            val padding = 150 // pixels — adjust to taste
            // Move after map is fully ready
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, padding),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            // Optional: recenter again when the map tiles finish loading
            if (allPoints.isNotEmpty()) {
                val boundsBuilder = LatLngBounds.builder()
                allPoints.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                val padding = 150
                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            }
        }
    ) {
        // Location markers (red)
        locations.forEach { location ->
            Marker(
                state = MarkerState(position = location),
                title = "Group Midpoint",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }

        // Activity markers (blue)
        activities.forEach { activity ->
            Marker(
                state = MarkerState(position = LatLng(activity.latitude, activity.longitude)),
                title = activity.name,
                snippet = activity.address,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
        }
    }
}
