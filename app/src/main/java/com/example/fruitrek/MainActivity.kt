package com.example.fruitrek

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fruitrek.ui.FruiTrekViewModel
import com.example.fruitrek.ui.screens.CameraScreen
import com.example.fruitrek.ui.screens.LandingScreen
import com.example.fruitrek.ui.screens.PassportScreen
import com.example.fruitrek.ui.screens.PermissionScreen
import com.example.fruitrek.ui.theme.FruiTrekTheme

class MainActivity : ComponentActivity() {
    private val viewModel: FruiTrekViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FruiTrekTheme {
                FruiTrekApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FruiTrekApp(viewModel: FruiTrekViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // When permission is granted, automatically navigate to camera
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            navController.navigate("camera") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "landing"
    ) {

        // ── 1. Landing page ───────────────────────────────────────────────────
        composable("landing") {
            LandingScreen(
                onStart = {
                    if (hasCameraPermission) {
                        navController.navigate("camera") {
                            popUpTo("landing") { inclusive = true }
                        }
                    } else {
                        navController.navigate("permission") {
                            popUpTo("landing") { inclusive = false }
                        }
                    }
                }
            )
        }

        // ── 2. Permission screen ──────────────────────────────────────────────
        composable("permission") {
            PermissionScreen(
                onRequestPermission = {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            )
        }

        // ── 3. Camera / scanner ───────────────────────────────────────────────
        composable("camera") {
            CameraScreen(
                viewModel = viewModel,
                onOpenPassport = { navController.navigate("passport") }
            )
        }

        // ── 4. Fruit Passport ─────────────────────────────────────────────────
        composable("passport") {
            PassportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}