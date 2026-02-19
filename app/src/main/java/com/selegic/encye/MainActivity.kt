package com.selegic.encye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.selegic.encye.article.ArticleDetailScreen
import com.selegic.encye.article.ArticleScreen
import com.selegic.encye.data.remote.dto.ArticleDto
import com.selegic.encye.home.Home
import com.selegic.encye.navigation.Navigator
import com.selegic.encye.navigation.rememberNavigationState
import com.selegic.encye.navigation.toEntries
import com.selegic.encye.ui.theme.EncyeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EncyeTheme {
                EncyeApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun EncyeApp() {
    val navigationState = rememberNavigationState(
        startRoute = AppDestinations.Home,
        topLevelRoutes = AppDestinations.entries().toSet()
    )
    val navigator = remember { Navigator(navigationState) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries().forEach { destination ->
                item(
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label, fontSize = 10.sp) },
                    selected = destination == navigationState.topLevelRoute,
                    onClick = {
                        navigator.navigate(destination)
                    }
                )
            }
        }
    ) {
        SharedTransitionLayout {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
                entry<AppDestinations.Home> {
                    Home()
                }
                entry<AppDestinations.Video> {
                    Greeting(name = "Video")
                }
                entry<AppDestinations.Training> {
                    Greeting(name = "Training")
                }
                entry<AppDestinations.Article> {
                    ArticleScreen(
                        onNavigateToArticleDetail = { it: ArticleDto ->
                            navigator.navigate(AppDestinations.ArticleDetail(it.id,it))
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = LocalNavAnimatedContentScope.current,
                    )
                }
                entry<AppDestinations.Community> {
                    Greeting(name = "Community")
                }
                entry<AppDestinations.ArticleDetail> {
                    ArticleDetailScreen(
                        articleId = it.id,
                        articleDto = it.dto,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = LocalNavAnimatedContentScope.current
                    )
                }
            }

            NavDisplay(
                modifier = Modifier.padding(innerPadding),
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() }
            )
        }
        }
    }
}

@Serializable
sealed class AppDestinations(val label: String) : NavKey {
    @Serializable
    data object Home : AppDestinations("Home")

    @Serializable
    data object Video : AppDestinations("Video")

    @Serializable
    data object Training : AppDestinations("Training")

    @Serializable
    data object Article : AppDestinations("Article")

    @Serializable
    data class ArticleDetail(val id: String, val dto: ArticleDto) : AppDestinations("Article")

    @Serializable
    data object Community : AppDestinations("Community")

    companion object {
        fun entries() = listOf(Home, Video, Training, Article, Community)
    }
}

val AppDestinations.icon: ImageVector
    get() = when (this) {
        AppDestinations.Home -> Icons.Default.Home
        AppDestinations.Video -> Icons.Default.Videocam
        AppDestinations.Training -> Icons.Default.School
        AppDestinations.Article -> Icons.AutoMirrored.Filled.Article
        AppDestinations.Community -> Icons.Default.People
        else -> {error("Unknown top level route")}
    }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EncyeTheme {
        Greeting("From Preview")
    }
}
