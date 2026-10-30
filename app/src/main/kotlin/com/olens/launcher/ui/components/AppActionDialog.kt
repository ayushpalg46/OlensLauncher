package com.olens.launcher.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.olens.launcher.R
import com.olens.launcher.model.AppItem
import com.olens.launcher.service.OlensNotificationListener
import com.olens.launcher.util.AppShortcutItem
import com.olens.launcher.util.AppShortcutsHelper

@Composable
fun AppActionDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleHide: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    onRename: (String?) -> Unit = {}
) {
    val context = LocalContext.current
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(app.displayLabel) }

    // Load static/dynamic app shortcuts (Niagara pop-up shortcut feature)
    val shortcuts = remember(app.packageName) {
        AppShortcutsHelper.getShortcutsForApp(context, app.packageName)
    }

    if (isEditingName) {
        AlertDialog(
            onDismissRequest = { isEditingName = false },
            title = {
                Text(
                    text = "Rename ${app.label}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        label = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRename(editedName.takeIf { it.isNotBlank() && it != app.label })
                        isEditingName = false
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onRename(null)
                        isEditingName = false
                        onDismiss()
                    }
                ) {
                    Text("Reset")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val appIconBmp = remember(app.packageName) {
                        app.icon?.toBitmap()
                    }
                    if (appIconBmp != null) {
                        Image(
                            bitmap = appIconBmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = app.displayLabel,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Deep Shortcuts (Niagara Pop-up shortcuts)
                    if (shortcuts.isNotEmpty()) {
                        Text(
                            text = "Shortcuts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        shortcuts.forEach { shortcut ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        AppShortcutsHelper.launchShortcut(context, app.packageName, shortcut.id)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (shortcut.icon != null) {
                                    val shortcutBmp = remember(shortcut.id) {
                                        shortcut.icon.toBitmap()
                                    }
                                    Image(
                                        bitmap = shortcutBmp.asImageBitmap(),
                                        contentDescription = shortcut.shortLabel,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.FlashOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = shortcut.shortLabel,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Favorite Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleFavorite(); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (app.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (app.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_add_favorite),
                            fontSize = 16.sp
                        )
                    }

                    // Rename App
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEditingName = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.action_rename),
                            fontSize = 16.sp
                        )
                    }

                    // Dismiss Notification (if active notification exists)
                    if (!app.notificationSummary.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    OlensNotificationListener.instance?.dismissNotificationsForPackage(app.packageName)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Dismiss Notification",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // View in Google Play
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${app.packageName}")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(playIntent)
                                } catch (e: Exception) {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(webIntent)
                                }
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "View in Google Play",
                            fontSize = 16.sp
                        )
                    }

                    // Hide / Unhide
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleHide(); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = if (app.isHidden) stringResource(R.string.action_unhide_app) else stringResource(R.string.action_hide_app),
                            fontSize = 16.sp
                        )
                    }

                    // App Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppInfo(); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.action_app_info),
                            fontSize = 16.sp
                        )
                    }

                    // Uninstall
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUninstall(); onDismiss() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.action_uninstall),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}
