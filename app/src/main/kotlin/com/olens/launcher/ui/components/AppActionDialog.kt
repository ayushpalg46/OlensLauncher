package com.olens.launcher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olens.launcher.R
import com.olens.launcher.model.AppItem

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
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(app.displayLabel) }

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
                        // Reset to original system app label
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
                Text(
                    text = app.displayLabel,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
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
