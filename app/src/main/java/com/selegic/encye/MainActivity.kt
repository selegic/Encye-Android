package com.selegic.encye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.selegic.encye.article.ArticleComposerRoute
import com.selegic.encye.article.ArticleDetailScreen
import com.selegic.encye.article.ArticleScreen
import com.selegic.encye.community.CommunityRoute
import com.selegic.encye.data.remote.dto.ArticleDto
import com.selegic.encye.home.Home
import com.selegic.encye.navigation.Navigator
import com.selegic.encye.navigation.rememberNavigationState
import com.selegic.encye.navigation.toEntries
import com.selegic.encye.onboarding.OnboardingScreen
import com.selegic.encye.training.TrainingDetailScreen
import com.selegic.encye.training.TrainingModuleDetailScreen
import com.selegic.encye.training.TrainingScreen
import com.selegic.encye.ui.theme.EncyeTheme
import com.selegic.encye.user.UserProfileRoute
import com.selegic.encye.util.SessionManager
import com.selegic.encye.video.VideoScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EncyeTheme {
                EncyeApp(sessionManager)
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun EncyeApp(sessionManager: SessionManager? = null) {
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(sessionManager) {
        if (sessionManager != null) {
            val token = sessionManager.getAuthToken()
            isLoggedIn = token != null
        } else {
            // For preview purposes, pretend we're logged in
            isLoggedIn = true
        }
    }

    when (isLoggedIn) {
        null -> {
            // Show a loading state while we check the DataStore
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        false -> {
            // User is not logged in, show Onboarding (without the bottom nav)
            OnboardingScreen(
                onNavigateToHome = {
                    isLoggedIn = true
                }
            )
        }
        true -> {
            // Main Application Flow
            val navigationState = rememberNavigationState(
                startRoute = AppDestinations.Home,
                topLevelRoutes = AppDestinations.entries().toSet()
            )
            val navigator = remember { Navigator(navigationState) }
            val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()
                ?: navigationState.topLevelRoute
            val showNavigationSuite = currentRoute == navigationState.topLevelRoute

            val appContent: @Composable () -> Unit = {
                SharedTransitionLayout {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
                            entry<AppDestinations.Home> {
                                Home(
                                    onProfileClick = { userId ->
                                        navigator.navigate(AppDestinations.UserProfile(userId))
                                    }
                                )
                            }
                            entry<AppDestinations.Video> {
                                VideoScreen()
                            }
                            entry<AppDestinations.Training> {
                                TrainingScreen(
                                    onNavigateToTrainingDetail = { trainingId ->
                                        navigator.navigate(AppDestinations.TrainingDetail(trainingId))
                                    }
                                )
                            }
                            entry<AppDestinations.Article> {
                                ArticleScreen(
                                    onNavigateToArticleDetail = { it: ArticleDto ->
                                        navigator.navigate(AppDestinations.ArticleDetail(it.id, it))
                                    },
                                    onCreateArticle = {
                                        navigator.navigate(AppDestinations.ArticleComposer)
                                    },
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedContentScope = LocalNavAnimatedContentScope.current,
                                )
                            }
                            entry<AppDestinations.Community> {
                                CommunityRoute()
                            }
                            entry<AppDestinations.ArticleDetail> {
                                ArticleDetailScreen(
                                    articleId = it.id,
                                    articleDto = it.dto,
                                    onBack = { navigator.goBack() },
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedContentScope = LocalNavAnimatedContentScope.current
                                )
                            }
                            entry<AppDestinations.ArticleComposer> {
                                ArticleComposerRoute(
                                    onBack = { navigator.goBack() }
                                )
                            }
                            entry<AppDestinations.TrainingDetail> {
                                TrainingDetailScreen(
                                    trainingId = it.id,
                                    onBack = { navigator.goBack() },
                                    onOpenModule = { trainingTitle, module ->
                                        navigator.navigate(
                                            AppDestinations.TrainingModuleDetail(
                                                trainingTitle = trainingTitle,
                                                module = module
                                            )
                                        )
                                    }
                                )
                            }
                            entry<AppDestinations.TrainingModuleDetail> {
                                TrainingModuleDetailScreen(
                                    trainingTitle = it.trainingTitle,
                                    module = it.module,
                                    onBack = { navigator.goBack() }
                                )
                            }
                            entry<AppDestinations.UserProfile> {
                                UserProfileRoute(
                                    userId = it.userId,
                                    onBack = { navigator.goBack() },
                                    onOpenArticle = { article ->
                                        navigator.navigate(AppDestinations.ArticleDetail(article.id, article))
                                    }
                                )
                            }
                        }

                        NavDisplay(
                            modifier = Modifier.fillMaxSize(),
                            entries = navigationState.toEntries(entryProvider),
                            onBack = { navigator.goBack() }
                        )
                    }
                }
            }

            if (showNavigationSuite) {
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
                    appContent()
                }
            } else {
                appContent()
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
    data object ArticleComposer : AppDestinations("Article")

    @Serializable
    data class ArticleDetail(val id: String, val dto: ArticleDto) : AppDestinations("Article")

    @Serializable
    data class TrainingDetail(val id: String) : AppDestinations("Training")

    @Serializable
    data class TrainingModuleDetail(
        val trainingTitle: String,
        val module: com.selegic.encye.data.remote.dto.TrainingModuleDto
    ) : AppDestinations("Training")

    @Serializable
    data class UserProfile(val userId: String? = null) : AppDestinations("Profile")

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
        else -> { error("Unknown top level route") }
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
