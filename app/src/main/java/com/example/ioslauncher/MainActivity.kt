package com.example.ioslauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: ImageBitmap
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LauncherScreen() }
    }
}

fun loadApps(pm: PackageManager, ownPackage: String): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .filter { it.activityInfo.packageName != ownPackage }
        .map {
            AppInfo(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(pm).toBitmap(128, 128).asImageBitmap()
            )
        }
        .sortedBy { it.label.lowercase() }
}

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    val apps = remember { loadApps(context.packageManager, context.packageName) }

    BackHandler { }

    fun open(app: AppInfo) {
        context.packageManager.getLaunchIntentForPackage(app.packageName)
            ?.let { context.startActivity(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(apps) { app -> AppIcon(app, onClick = { open(app) }) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                apps.take(4).forEach { app ->
                    Image(
                        bitmap = app.icon,
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { open(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppIcon(app: AppInfo, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            bitmap = app.icon,
            contentDescription = app.label,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(14.dp))
        )
        Text(
            text = app.label,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
