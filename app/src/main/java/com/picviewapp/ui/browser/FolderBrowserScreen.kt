package com.picviewapp.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.picviewapp.data.model.ImageInfo
import com.picviewapp.data.model.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowserScreen(
    folderPath: String,
    onImageClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val images by viewModel.images.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showSortMenu by viewModel.showSortMenu.collectAsState()

    LaunchedEffect(folderPath) {
        viewModel.loadFolder(folderPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderPath.substringAfterLast("/")) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { viewModel.toggleSortMenu() }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { viewModel.dismissSortMenu() }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Name (A-Z)") },
                                onClick = { viewModel.setSortOrder(SortOrder.NAME_ASC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Name (Z-A)") },
                                onClick = { viewModel.setSortOrder(SortOrder.NAME_DESC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Date (Newest)") },
                                onClick = { viewModel.setSortOrder(SortOrder.DATE_DESC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Date (Oldest)") },
                                onClick = { viewModel.setSortOrder(SortOrder.DATE_ASC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Size (Largest)") },
                                onClick = { viewModel.setSortOrder(SortOrder.SIZE_DESC) }
                            )
                            DropdownMenuItem(
                                text = { Text("Size (Smallest)") },
                                onClick = { viewModel.setSortOrder(SortOrder.SIZE_ASC) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (images.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No images found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text(
                    text = "${images.size} images",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(images) { index, image ->
                        ImageThumbnail(
                            image = image,
                            onClick = { onImageClick(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageThumbnail(
    image: ImageInfo,
    onClick: () -> Unit
) {
    AsyncImage(
        model = image.path,
        contentDescription = image.name,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop
    )
}
