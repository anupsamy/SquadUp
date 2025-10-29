package com.cpen321.squadup.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ActivityMapView(
    locations: List<LatLng>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    // Calculate bounds to show all markers
    LaunchedEffect(locations) {
        if (locations.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            locations.forEach { location ->
                boundsBuilder.include(location)
            }
            val bounds = boundsBuilder.build()
            val padding = 100 // Padding in pixels
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngBounds(bounds, padding)
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        locations.forEach { location ->
            Marker(
                state = MarkerState(position = location)
            )
        }
    }
}