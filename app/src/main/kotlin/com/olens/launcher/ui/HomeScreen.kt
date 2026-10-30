package com.olens.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olens.launcher.data.AppRepository
import com.olens.launcher.model.AppItem
import com.olens.launcher.ui.components.ALPHABET_ITEMS
import com.olens.launcher.ui.components.AlphabetScrubber
import com.olens.launcher.ui.components.AppActionDialog
import com.olens.launcher.ui.components.AppRow
import com.olens.launcher.ui.components.HomeHeader
import com.olens.launcher.ui.components.SearchBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    repository: AppRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val installedApps by repository.installedApps.collectAsState()
    val favoriteApps by repository.favoriteApps.collectAsState()
    val enableHaptics by repository.preferences.enableHaptics.collectAsState(initial = true)
    val scrubberOnRight by repository.preferences.scrubberOnRight.collectAsState(initial = true)

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForAction by remember { mutableStateOf<AppItem?>(null) }
    var isScrubbing by remember { mutableStateOf(false) }
    var currentScrubLetter by remember { mutableStateOf("") }

    // Intercept back button when searching
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    // Compute search filtered list
    val searchResults = remember(searchQuery, installedApps) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            repository.searchApps(searchQuery)
        }
    }

    // Map each alphabet letter to index in LazyColumn
    val letterIndexMap = remember(installedApps, favoriteApps) {
        val map = mutableMapOf<String, Int>()
        // Offset 0 is HomeHeader
        var currentIndex = 1 // After Header
        if (favoriteApps.isNotEmpty()) {
            currentIndex += 1 // Favorites label
            currentIndex += favoriteApps.size
        }

        var lastHeader = ""
        for ((idx, app) in installedApps.withIndex()) {
            val header = app.sectionHeader
            if (header != lastHeader) {
                map[header] = currentIndex + idx
                lastHeader = header
            }
        }
        map
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onDismiss = {
                        isSearchActive = false
                        searchQuery = ""
                    }
                )

                // Search Results
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(
                        items = searchResults,
                        key = { it.packageName }
                    ) { app ->
                        AppRow(
                            app = app,
                            onClick = { repository.launchApp(app) },
                            onLongClick = { selectedAppForAction = app }
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Clock & Glanceable info
                    item(key = "header") {
                        HomeHeader(
                            onSearchClick = { isSearchActive = true }
                        )
                    }

                    // Favorites Section
                    if (favoriteApps.isNotEmpty()) {
                        item(key = "favorites_title") {
                            Text(
                                text = "Favorites",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(
                            items = favoriteApps,
                            key = { "fav_${it.packageName}" }
                        ) { app ->
                            AppRow(
                                app = app,
                                onClick = { repository.launchApp(app) },
                                onLongClick = { selectedAppForAction = app }
                            )
                        }
                        item(key = "favorites_spacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // All Apps alphabetized list
                    items(
                        items = installedApps,
                        key = { it.packageName }
                    ) { app ->
                        AppRow(
                            app = app,
                            onClick = { repository.launchApp(app) },
                            onLongClick = { selectedAppForAction = app }
                        )
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Alphabet Scrubber (Only visible on main screen)
        if (!isSearchActive) {
            AlphabetScrubber(
                onLetterSelected = { letter ->
                    currentScrubLetter = letter
                    val targetIndex = letterIndexMap[letter]
                    if (targetIndex != null) {
                        coroutineScope.launch {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                },
                onScrubStateChange = { scrubbing ->
                    isScrubbing = scrubbing
                },
                enableHaptics = enableHaptics,
                isRightAligned = scrubberOnRight,
                modifier = Modifier.align(
                    if (scrubberOnRight) Alignment.CenterEnd else Alignment.CenterStart
                )
            )
        }

        // Scrubbing Letter Bubble Preview overlay
        AnimatedVisibility(
            visible = isScrubbing && currentScrubLetter.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentScrubLetter,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Long press dialog
        selectedAppForAction?.let { app ->
            AppActionDialog(
                app = app,
                onDismiss = { selectedAppForAction = null },
                onToggleFavorite = {
                    coroutineScope.launch { repository.toggleFavorite(app) }
                },
                onToggleHide = {
                    coroutineScope.launch { repository.toggleHide(app) }
                },
                onAppInfo = { repository.openAppInfo(app) },
                onUninstall = { repository.uninstallApp(app) }
            )
        }
    }
}
