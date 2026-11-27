package com.arin.tube18.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat
import com.arin.tube18.data.SiteUrls
import com.arin.tube18.ui.components.AppTopBar
import com.arin.tube18.ui.openUrl
import com.arin.tube18.viewmodel.SettingsViewModel

private enum class ConfirmAction(val title: String, val text: String, val confirmLabel: String) {
    ReAccept(
        "Re-confirm your age?",
        "The 18+ age gate will be shown again and you'll need to confirm before anything loads.",
        "Show gate"
    ),
    ClearHistory("Clear watch history?", "All videos in your history will be removed.", "Clear"),
    ClearFavorites("Clear favourites?", "All saved favourites will be removed.", "Clear")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var confirm by remember { mutableStateOf<ConfirmAction?>(null) }
    val version = remember { appVersion(context) }

    LaunchedEffect(Unit) {
        vm.messages.collect { snackbar.showSnackbar(it) }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Settings") },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingsSection("Age confirmation") {
                ListItem(
                    headlineContent = { Text("18+ confirmed") },
                    supportingContent = { Text("Show the age gate again and re-accept it.") },
                    leadingContent = { Icon(Icons.Filled.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        OutlinedButton(onClick = { confirm = ConfirmAction.ReAccept }) { Text("Re-accept") }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            SettingsSection("Storage") {
                SettingsRow(Icons.Filled.CleaningServices, "Clear cache", "Thumbnails, player and temporary files") {
                    vm.clearCache(context)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(Icons.Filled.DeleteSweep, "Clear history", "Remove every video you've watched") {
                    confirm = ConfirmAction.ClearHistory
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(Icons.Filled.HeartBroken, "Clear favourites", "Remove all saved favourites") {
                    confirm = ConfirmAction.ClearFavorites
                }
            }
            SettingsSection("About") {
                ListItem(
                    headlineContent = { Text("App version") },
                    supportingContent = { Text(version) },
                    leadingContent = { Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingsRow(Icons.Filled.Language, "Source site", SiteUrls.BASE) {
                    openUrl(context, SiteUrls.BASE)
                }
            }
            Text(
                "Videos and pictures are loaded straight from the source site. This app stores only your age confirmation, favourites and history on this phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    confirm?.let { action ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(action.title) },
            text = { Text(action.text) },
            confirmButton = {
                TextButton(onClick = {
                    when (action) {
                        ConfirmAction.ReAccept -> vm.reacceptAge()
                        ConfirmAction.ClearHistory -> vm.clearHistory()
                        ConfirmAction.ClearFavorites -> vm.clearFavorites()
                    }
                    confirm = null
                }) { Text(action.confirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

private fun appVersion(context: Context): String = try {
    val pm = context.packageManager
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageInfo(context.packageName, 0)
    }
    "${info.versionName ?: "unknown"} (build ${PackageInfoCompat.getLongVersionCode(info)})"
} catch (e: Exception) {
    "unknown"
}
