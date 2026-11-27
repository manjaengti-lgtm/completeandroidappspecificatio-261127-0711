package com.arin.tube18.ui

import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arin.tube18.AppContainer
import com.arin.tube18.TubeApp
import com.arin.tube18.model.Video
import com.arin.tube18.ui.screens.DetailScreen
import com.arin.tube18.ui.screens.HomeScreen
import com.arin.tube18.ui.screens.PlayerScreen
import com.arin.tube18.ui.screens.SavedScreen
import com.arin.tube18.ui.screens.SearchScreen
import com.arin.tube18.ui.screens.SettingsScreen
import com.arin.tube18.viewmodel.DetailViewModel
import com.arin.tube18.viewmodel.HomeViewModel
import com.arin.tube18.viewmodel.PlayerViewModel
import com.arin.tube18.viewmodel.SavedViewModel
import com.arin.tube18.viewmodel.SearchViewModel
import com.arin.tube18.viewmodel.SettingsViewModel
import com.arin.tube18.viewmodel.VmFactory

@Composable
fun appContainer(): AppContainer =
    (LocalContext.current.applicationContext as TubeApp).container

private data class BottomTab(val route: String, val pattern: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab("home", "home", "Home", Icons.Filled.Home),
    BottomTab("search", "search?q={q}", "Search", Icons.Filled.Search),
    BottomTab("saved", "saved", "Saved", Icons.Filled.Bookmarks),
    BottomTab("settings", "settings", "Settings", Icons.Filled.Settings)
)

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val container = appContainer()
    val backStack by nav.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val showBottomBar = destination?.route?.startsWith("player/") != true

    val openVideo: (Video) -> Unit = { video ->
        container.videos.remember(video)
        nav.navigate("detail/${video.id}")
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    bottomTabs.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.route == tab.pattern } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            composable("home") {
                val vm: HomeViewModel = viewModel(factory = VmFactory.home(container))
                HomeScreen(vm = vm, onVideoClick = openVideo)
            }
            composable(
                "search?q={q}",
                arguments = listOf(navArgument("q") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entry ->
                val vm: SearchViewModel = viewModel(factory = VmFactory.search(container))
                SearchScreen(
                    vm = vm,
                    initialQuery = entry.arguments?.getString("q"),
                    onVideoClick = openVideo
                )
            }
            composable("saved") {
                val vm: SavedViewModel = viewModel(factory = VmFactory.saved(container))
                SavedScreen(vm = vm, onVideoClick = openVideo)
            }
            composable("settings") {
                val vm: SettingsViewModel = viewModel(factory = VmFactory.settings(container))
                SettingsScreen(vm = vm)
            }
            composable("detail/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: DetailViewModel = viewModel(factory = VmFactory.detail(container, id))
                DetailScreen(
                    vm = vm,
                    onBack = { nav.popBackStack() },
                    onPlay = { nav.navigate("player/$id") },
                    onTagClick = { tag -> nav.navigate("search?q=${Uri.encode(tag)}") },
                    onVideoClick = openVideo
                )
            }
            composable("player/{id}") { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                val vm: PlayerViewModel = viewModel(factory = VmFactory.player(container, id))
                PlayerScreen(vm = vm, onBack = { nav.popBackStack() })
            }
        }
    }
}
