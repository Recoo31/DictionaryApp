package com.reco.ferhengakurdi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reco.ferhengakurdi.ui.theme.FerhengaKurdiTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.reco.ferhengakurdi.util.ProcessElement
import com.reco.ferhengakurdi.util.copyToClipboard
import com.reco.ferhengakurdi.view.BottomBar
import com.reco.ferhengakurdi.view.TextFieldForHomeScreen
import com.reco.ferhengakurdi.viewmodel.ViewModelHome
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode


lateinit var viewModelHome: ViewModelHome
var TAG = "MainActivity"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelHome = ViewModelHome()
        setContent {
            FerhengaKurdiTheme {
                Scaffold(
                    topBar = {
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DestinationsNavHost(navGraph = NavGraphs.root)
                        }
                    },
                    bottomBar = {
                        BottomBar()
                    }
                )
            }
        }
    }
}

@RootNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        viewModelHome.navigator = navigator
        val context = LocalContext.current
        val image = painterResource(id = R.drawable.qazi)
        val contentDescription = "Qazi Mihammed, Rebere Kurd"

        Text(
            text = "Ferhenga Kurdi",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Image(
            painter = image,
            contentDescription = contentDescription,
            modifier = Modifier
                .padding(18.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .graphicsLayer(alpha = 0.4f)
        )

        ElevatedCardResult(modifier = Modifier.clickable { viewModelHome.swapLanguages() }) {
            Row(
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModelHome.lang1.let { if (it == "tr") "Tirki" else "Kurdi" },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Change Language Icon",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp)
                )
                Text(
                    text = viewModelHome.lang2.let { if (it == "ku") "Kurdi" else "Tirki" },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextFieldForHomeScreen()
        }


        LazyColumn(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(viewModelHome.otherTranslate) { translation ->
                        if (viewModelHome.otherTranslate.isNotEmpty()) {
                            Log.d(TAG, "HomeScreen: ${viewModelHome.otherTranslate}")
                            ElevatedCardResult(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = translation,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .clickable { copyToClipboard(context,translation) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                viewModelHome.translateExampleTop?.let { (firstText, secondText) ->
                    Text(
                        text = "Top Example",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    ElevatedCardResult(Modifier.padding(8.dp)) {
                        TextForExample(firstText)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextForExample(secondText)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (viewModelHome.translateExample.isNotEmpty()) {
                    Text(
                        text = "Other Example",
                        color = MaterialTheme.colorScheme.primary,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(horizontal = 8.dp)

                    )
                }
            }
            items(viewModelHome.translateExample) { (firstText, secondText) ->
                ElevatedCardResult(Modifier.padding(4.dp)) {
                    TextForExample(firstText)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextForExample(secondText)
                }
            }
        }
    }
}


@Composable
private fun TextForExample(html: String) {
    val context = LocalContext.current
    val document = Jsoup.parse(html)
    val elements = document.body().children()

    val annotatedString = buildAnnotatedString {
        elements.forEach { element ->
            ProcessElement(element)
        }
    }

    Text(
        text = annotatedString,
        fontSize = 10.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { copyToClipboard(context,annotatedString.text) }
            .padding(4.dp),
    )
}


@Composable
fun ElevatedCardResult(modifier: Modifier = Modifier, items: @Composable (() -> Unit)) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = modifier
    ) {
        items()
    }
}
