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
import com.google.maps.android.compose.CameraPositionState

private fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
    return latitude != 0.0 && longitude != 0.0 &&
            !latitude.isNaN() && !longitude.isNaN() &&
            latitude.isFinite() && longitude.isFinite()
}

@Composable
fun LeaderActivityMapView(
    locations: List<LatLng>,
    activities: List<Activity>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    val allPoints = remember(locations, activities) {
        buildAllPoints(locations, activities)
    }

    // Move camera to fit all markers once map is ready
    LaunchedEffect(allPoints) {
        if (allPoints.isNotEmpty()) {
            animateCameraToFit(cameraPositionState, allPoints)
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            if (allPoints.isNotEmpty()) {
                onMapLoadedMoveCamera(cameraPositionState, allPoints)
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

private fun buildAllPoints(locations: List<LatLng>, activities: List<Activity>): List<LatLng> =
    (locations + activities.map { LatLng(it.latitude, it.longitude) })
        .filter { isValidCoordinate(it.latitude, it.longitude) }

private suspend fun animateCameraToFit(cameraState: CameraPositionState, points: List<LatLng>) {
    try {
        val boundsBuilder = LatLngBounds.builder()
        points.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()
        val padding = 150
        cameraState.animate(
            update = CameraUpdateFactory.newLatLngBounds(bounds, padding),
            durationMs = 1000
        )
    } catch (e: IllegalStateException) {
        // Fallback: center on first point
        if (points.isNotEmpty()) {
            val firstPoint = points.first()
            cameraState.animate(
                update = CameraUpdateFactory.newLatLngZoom(firstPoint, 14f),
                durationMs = 1000
            )
        }
    }
}

private fun onMapLoadedMoveCamera(cameraState: CameraPositionState, allPoints: List<LatLng>) {
    if (allPoints.size == 1) {
        val singlePoint = allPoints.first()
        cameraState.move(CameraUpdateFactory.newLatLngZoom(singlePoint, 14f))
    } else {
        try {
            val boundsBuilder = LatLngBounds.builder()
            allPoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            val padding = 150
            cameraState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        } catch (e: IllegalStateException) {
            val firstPoint = allPoints.first()
            cameraState.move(CameraUpdateFactory.newLatLngZoom(firstPoint, 14f))
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

    val allPoints = remember(midpoint, userLocation, selectedActivity) {
        buildList {
            midpoint?.takeIf { isValidCoordinate(it.latitude, it.longitude) }?.let(::add)
            userLocation?.takeIf { isValidCoordinate(it.latitude, it.longitude) }?.let(::add)
            selectedActivity?.takeIf { isValidCoordinate(it.latitude, it.longitude) }
                ?.let { add(LatLng(it.latitude, it.longitude)) }
        }
    }

    LaunchedEffect(allPoints) {
        if (allPoints.isNotEmpty()) animateTo(allPoints, cameraPositionState)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize().testTag("MidpointMap"),
        cameraPositionState = cameraPositionState,
        onMapLoaded = { if (allPoints.isNotEmpty()) moveTo(allPoints, cameraPositionState) }
    ) {
        midpoint?.let { placeMarkerIfValid(it.latitude, it.longitude, "Group Midpoint", BitmapDescriptorFactory.HUE_RED) }
        userLocation?.let { placeMarkerIfValid(it.latitude, it.longitude, "Your Location", BitmapDescriptorFactory.HUE_GREEN) }
        selectedActivity?.let { act ->
            placeMarkerIfValid(act.latitude, act.longitude, act.name, BitmapDescriptorFactory.HUE_AZURE, act.address)
        }
    }
}

private suspend fun animateTo(points: List<LatLng>, camera: CameraPositionState) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        val p = points.first()
        camera.animate(update = CameraUpdateFactory.newLatLngZoom(p, 14f), durationMs = 1000)
        return
    }
    try {
        val builder = LatLngBounds.builder()
        points.forEach { builder.include(it) }
        val bounds = builder.build()
        camera.animate(update = CameraUpdateFactory.newLatLngBounds(bounds, 150), durationMs = 1000)
    } catch (e: IllegalStateException) {
        val p = points.first()
        camera.animate(update = CameraUpdateFactory.newLatLngZoom(p, 14f), durationMs = 1000)
    }
}

private fun moveTo(points: List<LatLng>, camera: CameraPositionState) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        camera.move(CameraUpdateFactory.newLatLngZoom(points.first(), 14f))
        return
    }
    try {
        val builder = LatLngBounds.builder()
        points.forEach { builder.include(it) }
        camera.move(CameraUpdateFactory.newLatLngBounds(builder.build(), 150))
    } catch (e: IllegalStateException) {
        camera.move(CameraUpdateFactory.newLatLngZoom(points.first(), 14f))
    }
}

@Composable
private fun placeMarkerIfValid(lat: Double, lng: Double, title: String, hue: Float, snippet: String? = null) {
    if (lat == 0.0 && lng == 0.0) return
    Marker(
        state = MarkerState(position = LatLng(lat, lng)),
        title = title,
        snippet = snippet,
        icon = BitmapDescriptorFactory.defaultMarker(hue)
    )
}
