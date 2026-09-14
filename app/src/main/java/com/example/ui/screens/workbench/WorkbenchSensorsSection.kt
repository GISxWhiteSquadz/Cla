package com.example.ui.screens.workbench

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.BatteryManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.sin

@Composable
fun WorkbenchSensorsSection() {
    val context = LocalContext.current
    var showPixelTest by remember { mutableStateOf(false) }
    var showTouchTest by remember { mutableStateOf(false) }

    // Audio test state
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioTrack by remember { mutableStateOf<AudioTrack?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 1. Live Battery Telemetry
    var batteryLevel by remember { mutableIntStateOf(100) }
    var batteryVoltageMv by remember { mutableIntStateOf(4000) }
    var batteryTempCelsius by remember { mutableFloatStateOf(25.0f) }
    var batteryStatus by remember { mutableStateOf("Unbekannt") }
    var batteryHealth by remember { mutableStateOf("Gut") }
    var batteryPlugged by remember { mutableStateOf("Kein Kabel") }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (rawLevel >= 0 && scale > 0) {
                        batteryLevel = (rawLevel * 100) / scale
                    }
                    batteryVoltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)
                    val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
                    batteryTempCelsius = rawTemp / 10.0f

                    val statusInt = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    batteryStatus = when (statusInt) {
                        BatteryManager.BATTERY_STATUS_CHARGING -> "Wird geladen"
                        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Entlädt"
                        BatteryManager.BATTERY_STATUS_FULL -> "Vollgeladen"
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Lädt nicht"
                        else -> "Standby"
                    }

                    val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    batteryHealth = when (healthInt) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Normal / Gut"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Überhitzt!"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Defekt"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Überspannung"
                        else -> "In Ordnung"
                    }

                    val pluggedInt = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    batteryPlugged = when (pluggedInt) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "Netzteil (AC)"
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB-C Port"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Induktiv (Qi)"
                        else -> "Akkubetrieb"
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {}
        }
    }

    // 2. Hardware Sensors Telemetry
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(9.81f) }

    var gyroX by remember { mutableFloatStateOf(0f) }
    var gyroY by remember { mutableFloatStateOf(0f) }
    var gyroZ by remember { mutableFloatStateOf(0f) }

    var lightLux by remember { mutableFloatStateOf(150f) }
    var hasLightSensor by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val light = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        hasLightSensor = light != null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelX = event.values.getOrNull(0) ?: 0f
                        accelY = event.values.getOrNull(1) ?: 0f
                        accelZ = event.values.getOrNull(2) ?: 0f
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        gyroX = event.values.getOrNull(0) ?: 0f
                        gyroY = event.values.getOrNull(1) ?: 0f
                        gyroZ = event.values.getOrNull(2) ?: 0f
                    }
                    Sensor.TYPE_LIGHT -> {
                        lightLux = event.values.getOrNull(0) ?: 0f
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accel?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        gyro?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        light?.let { sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose {
            sensorManager?.unregisterListener(listener)
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
        }
    }

    // Function to test audio tone
    fun toggleAudioTone() {
        if (isAudioPlaying) {
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (_: Exception) {}
            audioTrack = null
            isAudioPlaying = false
        } else {
            coroutineScope.launch {
                withContext(Dispatchers.Default) {
                    val sampleRate = 44100
                    val durationSeconds = 10
                    val numSamples = sampleRate * durationSeconds
                    val samples = ShortArray(numSamples)
                    val freqOfTone = 440.0 // Standard A4 tone

                    for (i in 0 until numSamples) {
                        val angle = 2.0 * Math.PI * i / (sampleRate / freqOfTone)
                        samples[i] = (sin(angle) * Short.MAX_VALUE * 0.4).toInt().toShort()
                    }

                    val bufferSize = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize.coerceAtLeast(samples.size * 2))
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    track.write(samples, 0, samples.size)
                    track.play()
                    audioTrack = track
                    isAudioPlaying = true
                }
            }
        }
    }

    fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Manuelle Komponenten-Funktionstests DIREKT OBEN!
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Sensors,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Komponenten-Schnelltests",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Live-Hardware",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 1: Display Tests
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPixelTest = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Tv, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Pixelfehler", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showTouchTest = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ElectricMeter, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Touch-Matrix", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Audio & Haptic Tests
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { toggleAudioTone() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAudioPlaying) DangerRed else Color(0xFF0284C7)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isAudioPlaying) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isAudioPlaying) "Stoppen" else "440Hz Sinus",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { triggerVibration() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Vibration, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Vibration", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Live Akku Telemetrie Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.BatteryChargingFull,
                                contentDescription = null,
                                tint = if (batteryLevel > 20) SuccessGreen else DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Akku- & Power-Subsystem",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$batteryLevel%",
                            color = if (batteryLevel > 20) SuccessGreen else DangerRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (batteryLevel / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (batteryLevel > 20) SuccessGreen else DangerRed,
                        trackColor = DarkSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Zellspannung:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "${String.format(Locale.GERMANY, "%.3f", batteryVoltageMv / 1000.0)} V",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text("Temperatur:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = "${String.format(Locale.GERMANY, "%.1f", batteryTempCelsius)} °C",
                                color = if (batteryTempCelsius > 42) DangerRed else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text("Ladestatus:", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = batteryStatus,
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Gesundheit:", color = TextMuted, fontSize = 10.sp)
                            Text(batteryHealth, color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text("Quelle:", color = TextMuted, fontSize = 10.sp)
                            Text(batteryPlugged, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // 3. Hardware Sensors (Gyro, Accel, Light)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Bewegungs- & Umgebungs-Sensoren",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Beschleunigungsmesser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Beschleunigung (m/s²):", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "X: ${String.format(Locale.GERMANY, "%.1f", accelX)} | Y: ${String.format(Locale.GERMANY, "%.1f", accelY)} | Z: ${String.format(Locale.GERMANY, "%.1f", accelZ)}",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Gyroskop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gyroskop (rad/s):", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "X: ${String.format(Locale.GERMANY, "%.2f", gyroX)} | Y: ${String.format(Locale.GERMANY, "%.2f", gyroY)} | Z: ${String.format(Locale.GERMANY, "%.2f", gyroZ)}",
                            color = PrimaryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Lichtsensor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Umgebungslicht:", color = TextSecondary, fontSize = 11.sp)
                        }
                        Text(
                            text = "${lightLux.toInt()} Lux",
                            color = WarningAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Fullscreen Pixel Test Dialog
    if (showPixelTest) {
        Dialog(
            onDismissRequest = { showPixelTest = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
            var colorIndex by remember { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors[colorIndex])
                    .clickable {
                        if (colorIndex < colors.size - 1) {
                            colorIndex++
                        } else {
                            showPixelTest = false
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Farbe ${colorIndex + 1}/${colors.size} (Tippen für nächste Farbe, am Ende schließt Test)",
                    color = if (colors[colorIndex] == Color.White) Color.Black else Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }

    // Touchscreen Grid Calibration Dialog
    if (showTouchTest) {
        Dialog(
            onDismissRequest = { showTouchTest = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val touchedPoints = remember { mutableStateListOf<Offset>() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            touchedPoints.add(change.position)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw grid lines
                    val step = 60.dp.toPx()
                    var x = step
                    while (x < size.width) {
                        drawLine(Color(0xFF222222), Offset(x, 0f), Offset(x, size.height), 1f)
                        x += step
                    }
                    var y = step
                    while (y < size.height) {
                        drawLine(Color(0xFF222222), Offset(0f, y), Offset(size.width, y), 1f)
                        y += step
                    }

                    // Draw touch trail
                    for (i in 0 until touchedPoints.size - 1) {
                        drawLine(
                            color = Color(0xFF00E5FF),
                            start = touchedPoints[i],
                            end = touchedPoints[i + 1],
                            strokeWidth = 10f
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wische über den Bildschirm, um Touch-Sensoren zu testen",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = { showTouchTest = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Beenden", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
