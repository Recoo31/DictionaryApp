package com.reco.ferhengakurdi.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.reco.ferhengakurdi.ElevatedCardResult
import com.reco.ferhengakurdi.R
import com.reco.ferhengakurdi.destinations.DetailScreenDestination
import com.reco.ferhengakurdi.model.AppDatabase
import com.reco.ferhengakurdi.model.GalleryInfo
import com.reco.ferhengakurdi.viewModelHome
import com.reco.ferhengakurdi.viewmodel.ViewModelArchive

lateinit var viewModelArchive: ViewModelArchive
val TAG = "ArchiveScreen"

@Destination
@Composable
fun ArchiveScreenHome(navigator: DestinationsNavigator) {
    val context = LocalContext.current

    val database by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java, "gallery_info"
        ).build()
    }

    viewModelArchive = remember { ViewModelArchive(database) }
    LaunchedEffect(Unit) {
        viewModelArchive.getGalleryList(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Arşîva Kurd",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.align(Alignment.Center)
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_random),
                contentDescription = "Random Icon",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
                    .align(Alignment.CenterEnd)
                    .clickable { viewModelArchive.parsedGallery = viewModelArchive.parsedGallery.shuffled() }
            )
        }

        LazyColumn {
            items(viewModelArchive.parsedGallery) {
                GalleryItem(it) {
                    viewModelArchive.getArchiveImages(it.galleryId) {
                        navigator.navigate(DetailScreenDestination())
                    }
                }
            }
        }

    }
}


//@Preview(showSystemUi = true, showBackground = true)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GalleryItem(item: GalleryInfo, onClick: () -> Unit) {
    ElevatedCardResult(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GlideImage(
                model = item.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(text = item.title, modifier = Modifier.padding(4.dp))
        }
    }
}
