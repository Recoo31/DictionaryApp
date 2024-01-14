package com.reco.ferhengakurdi.view

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ramcosta.composedestinations.annotation.Destination
import com.reco.ferhengakurdi.ElevatedCardResult
import com.reco.ferhengakurdi.R
import java.io.File

@Destination
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailScreen() {
    var clicked by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf("") }

    LazyColumn {
        items(viewModelArchive.detailArchive) { detailModel ->
            ElevatedCardResult(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        clicked = true
                        selectedImage = detailModel.link
                    }
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    GlideImage(
                        model = detailModel.link,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape = RoundedCornerShape(8.dp))
                    )
                    Text(
                        text = detailModel.display_caption,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    if (clicked) {
        DetailImage(selectedImage) {
            clicked = false
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailImage(selectedImage: String, onPop: () -> Unit) {
    var isDownloading by remember { mutableStateOf(false) }

    val alpha = remember { Animatable(0f) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformationState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        alpha.animateTo(1f)
    }

    BackHandler(onBack = {
        onPop.invoke()
    })

    ElevatedCardResult {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            GlideImage(
                model = "$selectedImage/fit=2040x2040/",
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer(
                        alpha = alpha.value,
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformationState)
                    .fillMaxSize()
            )
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isDownloading) {
                    IconButton(
                        onClick = {
                            downloadImage("$selectedImage/fit=2040x2040/", context) {
                                isDownloading = false
                            }
                            isDownloading = true
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_download), contentDescription = null)
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

private fun downloadImage(url: String, context: Context, onDownloadComplete: () -> Unit) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val uri = Uri.parse(url)
    val request = DownloadManager.Request(uri)

    request.setTitle("Image Download")
    request.setDescription("Downloading")

    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "downloaded_image.jpg")
    request.setDestinationUri(Uri.fromFile(file))

    val downloadId = downloadManager.enqueue(request)

    val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                onDownloadComplete.invoke()
                context?.unregisterReceiver(this)
            }
        }
    }

    val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    context.registerReceiver(downloadCompleteReceiver, filter)
}
