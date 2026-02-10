package com.tejpratapsingh.lyricsmaker.presentation.compose

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.tejpratapsingh.lyricsmaker.presentation.activity.SearchActivity
import com.tejpratapsingh.lyricsmaker.presentation.viewmodel.LyricsViewModel
import com.tejpratapsingh.motion.imageloader.ImageLoader
import com.tejpratapsingh.motion.ongoing.domain.CurrentProject

@Composable
fun DashBoardScreen(lyricsViewModel: LyricsViewModel, navController: NavController) {

    val projects by lyricsViewModel.dashboardData.collectAsState()
    val context = LocalContext.current
    var fabExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lyricsViewModel.loadDashboardData()
    }

    Scaffold(
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { fabExpanded = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }

                DropdownMenu(
                    expanded = fabExpanded,
                    onDismissRequest = { fabExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Create New Project") },
                        onClick = {
                            fabExpanded = false
                            SearchActivity.startActivity(context)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = null
                            )
                        }
                    )
                }

            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(projects) { item ->
                ProjectCard(item, modifier = Modifier.padding(8.dp)) {
                    lyricsViewModel.currentSelectedProject = item
                    navController.navigate(Screen.ProjectDetails.route)
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: CurrentProject,
    modifier: Modifier = Modifier,
    onClick: (CurrentProject) -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    LaunchedEffect(project.url) {
        project.image?.let {
            bitmap = ImageLoader.loadBitmap(context, it)
        }
    }

    Card(
        modifier = modifier
            .padding(8.dp)
            .height(180.dp)
            .clickable {
                onClick(project)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Background Image
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = project.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Gradient overlay (for text readability)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Text(
                text = project.title ?: "",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}
