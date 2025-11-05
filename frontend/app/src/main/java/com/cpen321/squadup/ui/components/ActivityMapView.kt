package com.cpen321.squadup.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
fun LeaderActivityMapView(
    locations: List<LatLng>,
    activities: List<Activity>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    // Combine all coordinates
    val allPoints = remember(locations, activities) {
        (locations + activities.map { LatLng(it.latitude, it.longitude) })
            .filter { 
                it.latitude != 0.0 && it.longitude != 0.0 && 
                !it.latitude.isNaN() && !it.longitude.isNaN() &&
                it.latitude.isFinite() && it.longitude.isFinite()
            } // avoid invalid coords
    }

    // Move camera to fit all markers once map is ready
    LaunchedEffect(allPoints) {
        if (allPoints.isNotEmpty()) {
            try {
                val boundsBuilder = LatLngBounds.builder()
                allPoints.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                val padding = 150 // pixels — adjust to taste
                // Move after map is fully ready
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(bounds, padding),
                    durationMs = 1000
                )
            } catch (e: IllegalStateException) {
                // If bounds can't be built (no valid points), use default zoom
                if (allPoints.isNotEmpty()) {
                    val firstPoint = allPoints.first()
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(firstPoint, 14f),
                        durationMs = 1000
                    )
                }
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            if (allPoints.isNotEmpty()) {
                if (allPoints.size == 1) {
                    // ✅ Single marker: center and zoom out manually
                    val singlePoint = allPoints.first()
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(singlePoint, 14f) // smaller number = zoomed out
                    )
                } else {
                    // ✅ Multiple markers: fit bounds normally
                    try {
                        val boundsBuilder = LatLngBounds.builder()
                        allPoints.forEach { point -> boundsBuilder.include(point) }
                        val bounds = boundsBuilder.build()
                        val padding = 150
                        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    } catch (e: IllegalStateException) {
                        // Fallback to single point zoom if bounds can't be built
                        val firstPoint = allPoints.first()
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(firstPoint, 14f))
                    }
                }
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
@Composable
fun MemberActivityMapView(
    midpoint: LatLng?,
    userLocation: LatLng?,
    selectedActivity: Activity?,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    // Build list of valid coordinates
    val allPoints = remember(midpoint, userLocation, selectedActivity) {
        buildList {
            midpoint?.let {
                if (it.latitude != 0.0 && it.longitude != 0.0 && 
                    !it.latitude.isNaN() && !it.longitude.isNaN() &&
                    it.latitude.isFinite() && it.longitude.isFinite()) {
                    add(it)
                }
            }
            userLocation?.let {
                if (it.latitude != 0.0 && it.longitude != 0.0 && 
                    !it.latitude.isNaN() && !it.longitude.isNaN() &&
                    it.latitude.isFinite() && it.longitude.isFinite()) {
                    add(it)
                }
            }
            selectedActivity?.let { activity ->
                if (activity.latitude != 0.0 && activity.longitude != 0.0 &&
                    !activity.latitude.isNaN() && !activity.longitude.isNaN() &&
                    activity.latitude.isFinite() && activity.longitude.isFinite()) {
                    add(LatLng(activity.latitude, activity.longitude))
                }
            }
        }
    }

    // Move camera to fit all markers once map is ready
    LaunchedEffect(allPoints) {
        if (allPoints.isNotEmpty()) {
            if (allPoints.size == 1) {
                val singlePoint = allPoints.first()
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(singlePoint, 14f),
                    durationMs = 1000
                )
            } else {
                try {
                    val boundsBuilder = LatLngBounds.builder()
                    allPoints.forEach { boundsBuilder.include(it) }
                    val bounds = boundsBuilder.build()
                    val padding = 150
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngBounds(bounds, padding),
                        durationMs = 1000
                    )
                } catch (e: IllegalStateException) {
                    // Fallback to single point zoom if bounds can't be built
                    if (allPoints.isNotEmpty()) {
                        val firstPoint = allPoints.first()
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(firstPoint, 14f),
                            durationMs = 1000
                        )
                    }
                }
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize().testTag("MidpointMap"),
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            if (allPoints.isNotEmpty()) {
                if (allPoints.size == 1) {
                    val singlePoint = allPoints.first()
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(singlePoint, 14f)
                    )
                } else {
                    try {
                        val boundsBuilder = LatLngBounds.builder()
                        allPoints.forEach { point -> boundsBuilder.include(point) }
                        val bounds = boundsBuilder.build()
                        val padding = 150
                        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    } catch (e: IllegalStateException) {
                        // Fallback to single point zoom if bounds can't be built
                        val firstPoint = allPoints.first()
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(firstPoint, 14f))
                    }
                }
            }
        },
    ) {
        // Midpoint marker (red)
        midpoint?.let { point ->
            if (point.latitude != 0.0 && point.longitude != 0.0) {
                Marker(
                    state = MarkerState(position = point),
                    title = "Group Midpoint",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }
        }

        // User location marker (green)
        userLocation?.let { point ->
            if (point.latitude != 0.0 && point.longitude != 0.0) {
                Marker(
                    state = MarkerState(position = point),
                    title = "Your Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }
        }

        // Selected activity marker (blue)
        selectedActivity?.let { activity ->
            if (activity.latitude != 0.0 && activity.longitude != 0.0) {
                Marker(
                    state = MarkerState(position = LatLng(activity.latitude, activity.longitude)),
                    title = activity.name,
                    snippet = activity.address,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }
        }
    }
}