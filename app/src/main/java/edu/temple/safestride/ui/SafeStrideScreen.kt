package edu.temple.safestride.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import edu.temple.safestride.ui.components.RouteMapCard
import edu.temple.safestride.ui.components.SafetyDialogs
import edu.temple.safestride.ui.components.StatusCard
import edu.temple.safestride.ui.components.TrackingControlCard
import edu.temple.safestride.viewmodel.SafeStrideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeStrideScreen(viewModel: SafeStrideViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updatePermissionState(granted)
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.updatePermissionState(fineGranted || coarseGranted)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopTracking()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.9526, -75.1652), 14f)
    }

    LaunchedEffect(uiState.currentLocation) {
        uiState.currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SafeStride") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RouteMapCard(
                routePoints = uiState.routePoints,
                currentLocation = uiState.currentLocation,
                hasLocationPermission = uiState.hasLocationPermission,
                cameraPositionState = cameraPositionState
            )

            TrackingControlCard(
                isTracking = uiState.isTracking,
                onToggle = { shouldTrack ->
                    if (!uiState.hasLocationPermission) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    } else {
                        if (shouldTrack) viewModel.startTracking() else viewModel.stopTracking()
                    }
                }
            )

            StatusCard(uiState = uiState)
        }

        SafetyDialogs(
            uiState = uiState,
            onDismissAlert = viewModel::dismissSafetyAlert,
            onClearError = viewModel::clearError,
            onShareLocation = { viewModel.shareCurrentLocation(context) },
            onStopTracking = viewModel::stopTracking
        )
    }
}