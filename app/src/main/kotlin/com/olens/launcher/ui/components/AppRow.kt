package com.olens.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olens.launcher.model.AppItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRow(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(app.packageName) {
        app.icon?.let { drawableToBitmap(it) }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 24.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Icon
        Box(modifier = Modifier.size(38.dp)) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.displayLabel,
                    modifier = Modifier.size(38.dp)
                )
            }
            if (app.unreadNotifications > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "${app.unreadNotifications}",
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        // App Label
        Text(
            text = app.displayLabel,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

