package com.binarystack01.xcamera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.binarystack01.xcamera.ui.theme.XCameraTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            XCameraTheme {

                var isCameraPermissionGranted by remember { mutableStateOf(false) }
                var isCameraPermissionDenied by remember { mutableStateOf(false) }


                val permissionResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        isCameraPermissionGranted = isGranted
                        isCameraPermissionDenied = !isGranted

                    })

                LaunchedEffect(Unit) {
                    when {
                        ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            isCameraPermissionGranted = true
                        }

                        else -> {
                            permissionResultLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }

                Box {
                    if (isCameraPermissionGranted) {
                        XCameraPreview()
                    }

                    if (isCameraPermissionDenied) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Permission Denied, Please Grant permission for X Camera ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun XCameraPreview() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current


    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            bindToLifecycle(lifecycleOwner)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    controller = cameraController
                }
            },
            onRelease = {
                cameraController.unbind()
            }
        )
    }
}
