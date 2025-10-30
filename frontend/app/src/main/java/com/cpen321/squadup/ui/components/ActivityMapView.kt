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

    LaunchedEffect(allPoints) {
        if (allPoints.isEmpty()) {
            return@LaunchedEffect
        }

        // Case 1: There is only one point. Center on it and set a default zoom.
        // This correctly handles the "midpoint only" scenario.
        if (allPoints.size == 1) {
            val singlePoint = allPoints.first()
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(singlePoint, 14f), // 14f is a good default zoom
                durationMs = 800
            )
        }
        // Case 2: There are multiple points. Build bounds to fit them all.
        else {
            val boundsBuilder = LatLngBounds.builder()
            allPoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            val padding = 150 // pixels to prevent markers from being on the edge
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, padding),
                durationMs = 800
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
//        onMapLoaded = {
//            if (allPoints.isNotEmpty()) {
//                if (allPoints.size == 1) {
//                    val singlePoint = allPoints.first()
//                    cameraPositionState.move(
//                        CameraUpdateFactory.newLatLngZoom(singlePoint, 14f) // smaller number = zoomed out
//                    )
//                } else {
//                    val boundsBuilder = LatLngBounds.builder()
//                    allPoints.forEach { point -> boundsBuilder.include(point) }
//                    val bounds = boundsBuilder.build()
//                    val padding = 150
//                    cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
//                }
//            }
//        }
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
