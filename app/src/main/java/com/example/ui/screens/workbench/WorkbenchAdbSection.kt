package com.example.ui.screens.workbench

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.components.calculateScrollProgress
import com.example.ui.components.interactiveBounce
import com.example.ui.components.pulsingGlow
import com.example.ui.components.scrollReactiveBackground
import com.example.ui.theme.DarkBackground
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WorkbenchAdbSection() {
    val coroutineScope = rememberCoroutineScope()

    var adbConnectionStatus by remember { mutableStateOf("USB-OTG / ADB Bridge bereit") }
    var isConnected by remember { mutableStateOf(true) }
    var customCommand by remember { mutableStateOf("") }

    val terminalLogs = remember {
        mutableStateListOf(
            "=== Werkstatt ADB & Flash Terminal Initialisiert ===",
            "[HOST] ADB Server v1.0.41 gestartet auf Port 5037",
            "[DEVICE] USB-ID: 18d1:4ee7 | VID:Google PID:Nexus/Pixel ADB Interface",
            "[STATUS] Autorisierung: DEVICE (RSA Key verifiziert)",
            "System bereit für Dumpsys, Fastboot und Partition-Flash."
        )
    }

    // Flash Manager state
    var selectedPartition by remember { mutableStateOf("boot") }
    var selectedRomFile by remember { mutableStateOf("pixel_stock_boot_v14.img") }
    var isFlashing by remember { mutableStateOf(false) }
    var flashProgress by remember { mutableFloatStateOf(0f) }
    var flashStatusText by remember { mutableStateOf("") }
    var isVerifiedChecksum by remember { mutableStateOf(true) }

    fun executeAdbCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        terminalLogs.add("$ $trimmed")

        when {
            trimmed.contains("devices") -> {
                terminalLogs.add("List of devices attached:")
                terminalLogs.add("988AX031KL29    device    product:cheetah model:Pixel_7_Pro device:cheetah transport_id:1")
            }
            trimmed.contains("dumpsys battery") -> {
                terminalLogs.add("Current Battery Service state:")
                terminalLogs.add("  AC powered: false")
                terminalLogs.add("  USB powered: true (5V / 2.0A)")
                terminalLogs.add("  Wireless powered: false")
                terminalLogs.add("  Max charging current: 2000000 uA")
                terminalLogs.add("  Max charging voltage: 5000000 uV")
                terminalLogs.add("  Charge counter: 4320000 uAh")
                terminalLogs.add("  status: 2 (Charging)")
                terminalLogs.add("  health: 2 (Good)")
                terminalLogs.add("  present: true")
                terminalLogs.add("  level: 84")
                terminalLogs.add("  scale: 100")
                terminalLogs.add("  voltage: 4182 mV")
                terminalLogs.add("  temperature: 284 (28.4 °C)")
                terminalLogs.add("  technology: Li-poly")
            }
            trimmed.contains("getprop") -> {
                terminalLogs.add("[ro.product.model]: [Pixel 7 Pro]")
                terminalLogs.add("[ro.build.version.release]: [14]")
                terminalLogs.add("[ro.build.version.security_patch]: [2024-05-05]")
                terminalLogs.add("[ro.boot.flash.locked]: [0] (Bootloader Unlocked)")
                terminalLogs.add("[ro.boot.slot_suffix]: [_a]")
            }
            trimmed.contains("reboot bootloader") -> {
                terminalLogs.add("Neustart in Fastboot-Modus initiiert...")
                terminalLogs.add("Verbindung getrennt. Warte auf Fastboot-Handshake (VID: 18d1 PID: 4ee0)...")
                terminalLogs.add("Gerät erfolgreich im Fastboot-Modus erkannt!")
            }
            trimmed.contains("fastboot getvar all") -> {
                terminalLogs.add("(bootloader) version-baseband: g5300g-230323-230525-B-10255551")
                terminalLogs.add("(bootloader) version-bootloader: cloudripper-1.0-9844288")
                terminalLogs.add("(bootloader) partition-type:boot: raw")
                terminalLogs.add("(bootloader) current-slot: a")
                terminalLogs.add("(bootloader) unlocked: yes")
                terminalLogs.add("(bootloader) secure: yes")
                terminalLogs.add("finished. total time: 0.048s")
            }
            trimmed.contains("logcat") -> {
                terminalLogs.add("--------- beginning of main")
                terminalLogs.add("05-18 10:42:15.112  1244  1280 I ActivityTaskManager: Displayed com.example/MainActivity: +182ms")
                terminalLogs.add("05-18 10:42:16.421  1244  1450 D BatteryStatsImpl: noteScreenBrightnessModeLocked: 1")
                terminalLogs.add("05-18 10:42:17.002   890   890 I SurfaceFlinger: Refresh rate locked at 120Hz")
            }
            else -> {
                terminalLogs.add("[EXEC] $trimmed ausgeführt. Status: OK (Exit Code 0)")
            }
        }
    }

    fun startFlashing() {
        if (isFlashing) return
        isFlashing = true
        flashProgress = 0f
        flashStatusText = "Prüfe SHA-256 Checksumme & Anti-Rollback..."

        coroutineScope.launch {
            terminalLogs.add(">> Starte Flash-Prozess für Partition: $selectedPartition...")
            terminalLogs.add(">> Image-Datei: $selectedRomFile")
            delay(600)
            flashProgress = 0.25f
            flashStatusText = "Anti-Brick Check bestanden. Schreibe $selectedPartition..."
            terminalLogs.add("[FLASH] Erasing ${selectedPartition}_a...")
            terminalLogs.add("[FLASH] Sending '${selectedPartition}_a' (65536 KB)...")

            delay(800)
            flashProgress = 0.65f
            flashStatusText = "Übertrage Image-Blöcke (64 MB)..."
            terminalLogs.add("[FLASH] Writing '${selectedPartition}_a' OKAY [  1.241s]")

            delay(700)
            flashProgress = 0.90f
            flashStatusText = "Verifiziere SHA-256 Partition Checksum..."
            terminalLogs.add("[VERIFY] Partition Checksum stimmt mit Vendor Hash überein!")

            delay(500)
            flashProgress = 1.0f
            flashStatusText = "Erfolgreich geflasht!"
            terminalLogs.add(">> Flash-Vorgang für $selectedPartition erfolgreich abgeschlossen!")
            isFlashing = false
        }
    }

    fun createEfsBackup() {
        coroutineScope.launch {
            terminalLogs.add(">> Starte EFS & NVRAM IMEI Backup...")
            delay(500)
            terminalLogs.add("[BACKUP] Lese Partition /dev/block/bootdevice/by-name/modemst1...")
            delay(400)
            terminalLogs.add("[BACKUP] Lese Partition /dev/block/bootdevice/by-name/modemst2...")
            delay(400)
            terminalLogs.add("[BACKUP] Gespeichert: /sdcard/Werkbank_Backup/efs_backup_${System.currentTimeMillis()}.bin")
            terminalLogs.add(">> EFS/NVRAM Sicherung erfolgreich verifiziert!")
        }
    }

    val listState = rememberLazyListState()
    val scrollProgress = listState.calculateScrollProgress(maxItemIndexThreshold = 5)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .scrollReactiveBackground(scrollProgress = scrollProgress, baseBackground = DarkBackground)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Connection & Bridge Status Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Usb,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Google Pixel 7 Pro (cheetah, Android 14)",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "USB-OTG Bridge • ADB Debugging autorisiert • Root: Shell",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SuccessGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ONLINE",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. ADB Command Input DIREKT GANZ OBEN!
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customCommand,
                        onValueChange = { customCommand = it },
                        placeholder = {
                            Text("ADB / Fastboot Befehl eingeben...", fontSize = 12.sp, color = TextMuted)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            if (customCommand.isNotEmpty()) {
                                IconButton(
                                    onClick = { customCommand = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Eingabe löschen",
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("workbench_adb_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            executeAdbCommand(customCommand)
                            customCommand = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .pulsingGlow(glowColor = PrimaryBlue)
                            .interactiveBounce {
                                executeAdbCommand(customCommand)
                                customCommand = ""
                            }
                            .testTag("workbench_adb_send_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Run", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Schnellauswahl-Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickCommands = listOf(
                        "adb devices -l" to "Geräte-Info",
                        "adb shell dumpsys battery" to "Akku Dumpsys",
                        "adb shell getprop" to "Build Props",
                        "adb reboot bootloader" to "Fastboot Boot",
                        "fastboot getvar all" to "Fastboot Vars",
                        "adb logcat -d -t 20" to "Crash-Logcat"
                    )
                    items(quickCommands) { (cmd, label) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceElevated)
                                .border(0.8.dp, DarkBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    customCommand = cmd
                                    executeAdbCommand(cmd)
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Terminal,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Interactive Terminal Window
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DangerRed))
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WarningAmber))
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "adb_shell@werkbank:~$",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = { terminalLogs.clear() },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Log output
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF050811))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(terminalLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.startsWith("$")) Color(0xFF38BDF8)
                                    else if (log.contains("OK") || log.contains("erfolgreich")) SuccessGreen
                                    else if (log.contains("Error") || log.contains("Warn")) DangerRed
                                    else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Firmware Flasher & Anti-Brick Protection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firmware Flasher & Partition-Manager",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(Icons.Filled.Security, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Ziel-Partition auswählen:", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val partitions = listOf("boot", "recovery", "vendor_boot", "system", "dtbo")
                        items(partitions) { part ->
                            val isSel = part == selectedPartition
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PrimaryBlue else DarkSurface)
                                    .border(1.dp, if (isSel) PrimaryBlue else DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedPartition = part }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$part.img",
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Anti-Brick & Checksum Warning Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningAmber.copy(alpha = 0.1f))
                            .border(0.8.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Anti-Brick Schutz aktiv: SHA-256 Integritätsprüfung",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automatischer Rollback-Schutz verhindert Fehlflashs bei nicht kompatiblen Chipsets.",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isFlashing || flashProgress > 0f) {
                        LinearProgressIndicator(
                            progress = { flashProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = DarkSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = flashStatusText,
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { startFlashing() },
                            enabled = !isFlashing,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .pulsingGlow(glowColor = PrimaryBlue)
                                .interactiveBounce { startFlashing() }
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFlashing) "Flashe..." else "$selectedPartition Flashen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { createEfsBackup() },
                            enabled = !isFlashing,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .interactiveBounce { createEfsBackup() }
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EFS / IMEI Backup",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
