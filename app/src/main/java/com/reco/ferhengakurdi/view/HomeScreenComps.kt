package com.reco.ferhengakurdi.view

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reco.ferhengakurdi.ElevatedCardResult
import com.reco.ferhengakurdi.util.copyToClipboard
import com.reco.ferhengakurdi.viewModelHome

@Composable
fun TextFieldForHomeScreen() {
    var expanded by remember { mutableStateOf(false) }
    var height by remember { mutableIntStateOf(0) }
    val context = LocalContext.current


    OutlinedTextField(
        value = viewModelHome.query,
        onValueChange = {
            viewModelHome.updateQuery(it)
            viewModelHome.similarPhrases()
            expanded = true
        },
        placeholder = { Text(text = "Lêgerine hevala evindar...", fontSize = 16.sp) },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search Icon") },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                height = it.size.height
            },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = {
            Log.i("TAG", "HomeScreen: ${viewModelHome.query}")
            viewModelHome.getTranslateScore()
            viewModelHome.getQuery()
            expanded = false
        }),
    )

    if (viewModelHome.showSecond) {
        Spacer(modifier = Modifier.height(16.dp))
        Column {
            OutlinedTextField(
                value = viewModelHome.translationGlobse,
                onValueChange = { },
                readOnly = true,
                enabled = false,
                placeholder = { Text(text = "...") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { copyToClipboard(context,viewModelHome.translationGlobse) }
            )
            if (viewModelHome.translationGlobse != viewModelHome.translationGoogle){
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = viewModelHome.translationGoogle,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    placeholder = { Text(text = "...") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { copyToClipboard(context,viewModelHome.translationGoogle) }
                )
            }
        }
    }

    if (expanded) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
        ) {
            LazyColumn {
                items(viewModelHome.phrase) { post ->
                    ElevatedCardResult(Modifier.fillMaxWidth()) {
                        Text(
                            text = post.phrase,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModelHome.updateQuery(post.phrase)
                                    expanded = false
                                    viewModelHome.getQuery()
                                    viewModelHome.getTranslateScore()
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}