package com.reco.ferhengakurdi.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.reco.ferhengakurdi.R
import com.reco.ferhengakurdi.destinations.ArchiveScreenHomeDestination
import com.reco.ferhengakurdi.destinations.HomeScreenDestination
import com.reco.ferhengakurdi.viewModelHome

@Composable
fun BottomBar() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    BottomAppBar(
        actions = {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                TranslateButton(
                    onClick = {
                        if (selectedTabIndex != 0) {
                            selectedTabIndex = 0
                            viewModelHome.navigator.navigate(HomeScreenDestination())
                        }
                    },
                    selected = selectedTabIndex == 0
                )
                ArchiveButton(
                    onClick = {
                        if (selectedTabIndex != 1) {
                            selectedTabIndex = 1
                            viewModelHome.navigator.navigate(ArchiveScreenHomeDestination())
                        }
                    },
                    selected = selectedTabIndex == 1
                )
            }
        },
        modifier = Modifier.wrapContentSize()
    )
}

@Composable
fun TranslateButton(onClick: () -> Unit, selected: Boolean) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified

    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_g_translate_24),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp),
            tint = tint
        )
    }
}

@Composable
fun ArchiveButton(onClick: () -> Unit, selected: Boolean) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified

    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.ic_image),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp),
            tint = tint
        )
    }
}
