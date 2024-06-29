package com.example.orientation_lab06_danp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)

        setContent {
            var orientation by remember { mutableStateOf(0f) }
            var fixedOrientation by remember { mutableStateOf<Float?>(null) }

            LaunchedEffect(Unit) {
                while (true) {
                    SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    if (fixedOrientation == null) {
                        orientation = orientationAngles[0] // Yaw angle
                    }
                    delay(16) // roughly 60fps
                }
            }

            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(modifier = Modifier.fillMaxSize()) {
                    DrawTriangle(fixedOrientation ?: orientation)
                    Button(
                        onClick = { fixedOrientation = if (fixedOrientation == null) orientation else null },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text(text = if (fixedOrientation == null) "Fijar" else "Liberar")
                    }
                }
            }
        }
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }
}

@Composable
fun DrawTriangle(orientation: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val trianglePath = Path().apply {
            moveTo(size.width / 2, size.height / 4)
            lineTo(size.width / 4, 3 * size.height / 4)
            lineTo(3 * size.width / 4, 3 * size.height / 4)
            close()
        }

        rotate(degrees = -orientation * 180 / Math.PI.toFloat()) {
            drawPath(path = trianglePath, color = Color.Cyan)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    var orientation by remember { mutableStateOf(0f) }
    var fixedOrientation by remember { mutableStateOf<Float?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            DrawTriangle(fixedOrientation ?: orientation)
            Button(
                onClick = { fixedOrientation = if (fixedOrientation == null) orientation else null },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text(text = if (fixedOrientation == null) "Fijar" else "Liberar")
            }
        }
    }
}
