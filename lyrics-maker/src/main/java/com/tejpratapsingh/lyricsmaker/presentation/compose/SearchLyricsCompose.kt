package com.tejpratapsingh.lyricsmaker.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tejpratapsingh.lyricsmaker.domain.TrimLyrics
import com.tejpratapsingh.lyricsmaker.domain.TrimUnit
import com.tejpratapsingh.lyricsmaker.presentation.activity.LyricsActivity
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel
) {
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val lyrics by viewModel.lyricsList.collectAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Search Lyrics",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(16.dp)
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search") },
            singleLine = true,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    coroutineScope.launch {
                        if (query.isNotBlank()) {
                            isLoading = true
                            viewModel.fetchLyrics(query)
                            isLoading = false
                        }
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(lyrics.size) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            LyricsActivity.start(
                                context = context,
                                lyrics = lyrics[item],
                                trimLyrics = TrimLyrics(
                                    start = 1000,
                                    end = 1500,
                                    unit = TrimUnit.FRAME
                                )
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "${lyrics[item].trackName} - ${lyrics[item].artistName}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 16.dp,
                            bottom = 2.dp
                        )
                    )
                    Text(
                        text = "Duration: ${lyrics[item].getReadableDuration()}",
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 2.dp,
                            end = 16.dp,
                            bottom = 2.dp
                        )
                    )
                    Text(
                        text = lyrics[item].getLyrics(),
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 2.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                    )
                }
            }
        }
    }
}