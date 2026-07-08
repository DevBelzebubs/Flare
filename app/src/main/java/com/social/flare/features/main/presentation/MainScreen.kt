package com.social.flare.features.main.presentation

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.work.WorkManager
import com.social.flare.FlareApp
import com.social.flare.core.data.SessionManager
import com.social.flare.core.navigation.Screen
import com.social.flare.core.ui.components.AuthDialog
import com.social.flare.core.ui.theme.FlareDarkBackground
import com.social.flare.core.ui.theme.FlareDarkSurface
import com.social.flare.core.ui.theme.FlareLightBackground
import com.social.flare.core.ui.theme.FlareLightSurface
import com.social.flare.features.auth.data.repository.AuthRepositoryImpl
import com.social.flare.features.auth.domain.usecase.ChangePasswordUseCase
import com.social.flare.features.auth.presentation.LoginScreen
import com.social.flare.features.auth.presentation.SignUpScreen
import com.social.flare.features.feed.presentation.FeedScreen
import com.social.flare.features.profile.presentation.ProfileScreen
import com.social.flare.features.profile.presentation.SettingsScreen
import com.social.flare.features.search.presentation.SearchScreen
import com.social.flare.features.search.presentation.SearchViewModel

import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import com.social.flare.features.feed.data.repository.MusicRepositoryImpl
import com.social.flare.features.feed.data.repository.StoryRepositoryImpl
import com.social.flare.features.feed.domain.usecase.GetFeedUseCase
import com.social.flare.features.feed.presentation.FeedViewModel
import com.social.flare.features.feed.presentation.MusicViewModel
import com.social.flare.features.feed.presentation.StoryViewModel
import com.social.flare.features.feed.presentation.components.AddStoryScreen
import com.social.flare.features.feed.presentation.components.CustomGalleryScreen
import com.social.flare.features.feed.presentation.components.PostDetailScreen
import com.social.flare.features.feed.presentation.components.stories.StoryViewerScreen
import com.social.flare.features.post.domain.usecase.CreatePostUseCase
import com.social.flare.features.post.domain.usecase.GetUserPostsUseCase
import com.social.flare.features.post.presentation.AddPostScreen
import com.social.flare.features.post.presentation.AddPostViewModel
import com.social.flare.features.post.presentation.PostDetailViewModel
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel
import com.social.flare.features.main.presentation.components.FlareTopBar
import com.social.flare.features.main.presentation.components.FlareBottomNavigation
import com.social.flare.features.notifications.data.repository.NotificationRepositoryImpl
import com.social.flare.features.notifications.domain.usecase.GetNotificationsUseCase
import com.social.flare.features.notifications.domain.usecase.ManageRealtimeNotificationsUseCase
import com.social.flare.features.notifications.domain.usecase.MarkNotificationReadUseCase
import com.social.flare.features.notifications.presentation.NotificationScreen
import com.social.flare.features.notifications.presentation.viewmodel.NotificationViewModel
import com.social.flare.features.post.domain.usecase.DeletePostUseCase
import com.social.flare.features.post.domain.usecase.UpdatePostUseCase
import com.social.flare.features.profile.data.repository.FollowRepositoryImpl
import com.social.flare.features.profile.data.repository.ProfileRepositoryImpl
import com.social.flare.features.profile.domain.usecase.GetFollowStatsUseCase
import com.social.flare.features.profile.domain.usecase.ToggleFollowUseCase
import com.social.flare.features.profile.presentation.EditProfileScreen
import com.social.flare.features.profile.presentation.ProfileViewModelFactory
import com.social.flare.features.profile.presentation.viewmodel.EditProfileViewModel
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import com.social.flare.features.admin.data.repository.AdminRepositoryImpl
import com.social.flare.features.admin.domain.usecase.CreateAiProfileUseCase
import com.social.flare.features.admin.presentation.AdminDashboardScreen
import com.social.flare.features.admin.presentation.AdminUsersScreen
import com.social.flare.features.admin.presentation.AdminPostsScreen
import com.social.flare.features.admin.presentation.AdminNewsScreen
import com.social.flare.features.admin.presentation.viewmodel.AdminViewModel
import com.social.flare.di.AiRepositoryEntryPoint
import com.social.flare.features.ai.domain.repository.AiAgentRepository
import com.social.flare.features.main.presentation.components.SplashScreen
import dagger.hilt.android.EntryPointAccessors
import com.social.flare.features.notifications.domain.usecase.GetSuggestedAccountsUseCase
import com.social.flare.features.profile.presentation.FollowListScreen
import com.social.flare.features.profile.presentation.HelpCenterScreen
import com.social.flare.features.profile.presentation.PrivacyPolicyScreen
import com.social.flare.features.profile.presentation.TermsOfServiceScreen
import com.social.flare.features.profile.presentation.viewmodel.FollowListViewModel
import com.social.flare.features.search.data.repository.SearchRepositoryImpl

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val isStoryViewerRoute = currentRoute?.startsWith(Screen.StoryViewer.route) == true
    val isAddStoryRoute = currentRoute?.startsWith(Screen.AddStory.route) == true
    val isCustomGalleryRoute = currentRoute == Screen.CustomGallery.route
    val isFullscreenStoryRoute = isStoryViewerRoute || isAddStoryRoute
    val isStoryFlowRoute = isFullscreenStoryRoute || isCustomGalleryRoute
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    var showAuthDialog by remember { mutableStateOf(false) }

    DisposableEffect(isFullscreenStoryRoute, isLightTheme, context) {
        val activity = context as? ComponentActivity
        if (activity != null) {
            if (isFullscreenStoryRoute) {
                activity.applyStoryFullscreenSystemBars()
            } else {
                activity.applyNormalSystemBars(isLightTheme)
            }
        }

        onDispose {
            activity?.applyNormalSystemBars(isLightTheme)
        }
    }

    val app = context.applicationContext as FlareApp
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val activeCitizenId by sessionManager.activeCitizenIdFlow.collectAsStateWithLifecycle(initialValue = null)

    val cloudinaryService = remember { CloudinaryService(context.applicationContext) }
    val followDao = remember { app.database.followDao() }
    val citizenDao = remember { app.database.citizenDao() }
    val authRepository = remember { AuthRepositoryImpl(citizenDao, app.supabase) }
    val feedRepository = remember {
        FeedRepositoryImpl(
            postDao = app.database.postDao(),
            citizenDao = citizenDao,
            followDao = followDao,
            supabase = app.supabase
        )
    }
    val profileRepository = remember { ProfileRepositoryImpl(app.database.citizenDao(), app.supabase) }
    val storyRepository = remember {
        StoryRepositoryImpl(
            storyDao = app.database.storyDao(),
            citizenDao = citizenDao,
            cloudinaryService = cloudinaryService,
            supabase = app.supabase
        )
    }
    val followRepository = remember { FollowRepositoryImpl(followDao, citizenDao, app.supabase) }
    val syncDao = remember { app.database.syncDao() }
    val workManager = remember { WorkManager.getInstance(context) }

    val notificationRepository = remember {
        NotificationRepositoryImpl(
            notificationDao = app.database.notificationDao(),
            supabase = app.supabase
        )
    }
    val toggleFollowUseCase = remember { ToggleFollowUseCase(followRepository) }
    val getFollowStatsUseCase = remember { GetFollowStatsUseCase(followRepository) }
    val getPostsUseCase = remember { GetUserPostsUseCase(feedRepository) }
    val getFeedUseCase = remember { GetFeedUseCase(feedRepository) }
    val deletePostUseCase = remember { DeletePostUseCase(feedRepository, cloudinaryService) }
    val updatePostUseCase = remember { UpdatePostUseCase(feedRepository) }
    val createPostUseCase = remember { CreatePostUseCase(feedRepository, cloudinaryService) }
    val changePasswordUseCase = remember { ChangePasswordUseCase(authRepository) }
    val getNotificationsUseCase = remember { GetNotificationsUseCase(notificationRepository) }
    val manageRealtimeNotificationsUseCase = remember {
        ManageRealtimeNotificationsUseCase(
            notificationRepository
        )
    }
    val markNotificationReadUseCase = remember { MarkNotificationReadUseCase(notificationRepository) }
    val adminRepository = remember {
        AdminRepositoryImpl(
            citizenDao = citizenDao,
            postDao = app.database.postDao(),
            newsDao = app.database.newsDao(),
            syncDao = syncDao,
            workManager = workManager,
            supabase = app.supabase,
            cloudinaryService = cloudinaryService
        )
    }
    val aiAgentRepository = remember {
        val entryPoint = EntryPointAccessors.fromApplication(context, AiRepositoryEntryPoint::class.java)
        entryPoint.aiAgentRepository()
    }

    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(activeCitizenId) {
        val id = activeCitizenId
        if (id != null) {
            notificationRepository.getNotifications(id).collect {}
        } else {
            unreadCount = 0
        }
    }

    DisposableEffect(activeCitizenId) {
        val id = activeCitizenId
        if (id != null) {
            notificationRepository.connectToRealtimeNotifications(id, scope)
            feedRepository.connectToRealtimeFeed(scope)
            followRepository.connectToRealtimeFollows(id, scope)
        }
        onDispose {
            notificationRepository.disconnectFromRealtimeNotifications()
            feedRepository.disconnectFromRealtimeFeed()
            followRepository.disconnectFromRealtimeFollows()
        }
    }

    LaunchedEffect(activeCitizenId) {
        val id = activeCitizenId
        if (id != null) {
            notificationRepository.getUnreadCount(id).collect { count ->
                unreadCount = count
            }
        }
    }

    Scaffold(
        topBar = {
            val hideTopBarRoutes = listOf(
                Screen.Splash.route,
                Screen.Login.route, Screen.SignUp.route,
                Screen.AdminDashboard.route, Screen.AdminUsers.route,
                Screen.AdminPosts.route, Screen.AdminNews.route
            )
            if (currentRoute !in hideTopBarRoutes && !isStoryFlowRoute) {
                FlareTopBar(onSettingsClick = { navController.navigate(Screen.Settings.route) })
            }
        },
        bottomBar = {
            val adminRoutes = listOf(
                Screen.AdminDashboard.route, Screen.AdminUsers.route,
                Screen.AdminPosts.route, Screen.AdminNews.route
            )
            val isMainTab = !isStoryFlowRoute && !adminRoutes.contains(currentRoute) && (
                listOf(
                    Screen.Feed.route, Screen.Search.route, Screen.AddPost.route, Screen.Notifications.route
                ).contains(currentRoute) || currentRoute?.startsWith(Screen.Profile.route) == true
            )

            if (isMainTab) {
                FlareBottomNavigation(
                    currentRoute = currentRoute ?: Screen.Feed.route,
                    isGuest = activeCitizenId == null,
                    unreadCount = unreadCount,
                    onRequireAuth = { showAuthDialog = true },
                    onNavigate = { route ->
                        val privateRoutes = listOf(Screen.AddPost.route, Screen.Profile.route, Screen.Notifications.route)

                        val targetRoute = if (route == Screen.Profile.route) "${Screen.Profile.route}/$activeCitizenId" else route

                        if (activeCitizenId == null && privateRoutes.contains(route)) {
                            showAuthDialog = true
                        } else {
                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        containerColor = if (isFullscreenStoryRoute) Color.Black else MaterialTheme.colorScheme.background,
        contentWindowInsets = if (isFullscreenStoryRoute) {
            WindowInsets(0, 0, 0, 0)
        } else {
            ScaffoldDefaults.contentWindowInsets
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isFullscreenStoryRoute) Color.Black else MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            NavHost(navController = navController, startDestination = Screen.Feed.route) {

                composable(Screen.Feed.route) {
                    val feedViewModel: FeedViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return FeedViewModel(getFeedUseCase, deletePostUseCase, updatePostUseCase, feedRepository, profileRepository, storyRepository) as T
                            }
                        }
                    )
                    LaunchedEffect(activeCitizenId) {
                        val id = activeCitizenId
                        if (id != null) {
                            feedViewModel.loadFeed(id)
                        } else {
                            feedViewModel.loadFeedGuest()
                        }
                    }

                    FeedScreen(
                        activeCitizenId = activeCitizenId, viewModel = feedViewModel,
                        onRequireAuth = { showAuthDialog = true },
                        onPostClick = { postId -> navController.navigate("${Screen.PostDetail.route}/$postId") },
                        onStoryClick = { username -> navController.navigate("${Screen.StoryViewer.route}/$username") },
                        onNavigateToAddStory = { navController.navigate(Screen.CustomGallery.route) },
                        onAuthorClick = { authorId -> navController.navigate("${Screen.Profile.route}/$authorId") }
                    )
                }

                composable("${Screen.StoryViewer.route}/{username}") { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    val storyViewerViewModel: StoryViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return StoryViewModel(storyRepository) as T
                            }
                        }
                    )

                    val activeStories by storyRepository.getActiveStories(activeCitizenId ?: "").collectAsStateWithLifecycle(initialValue = emptyList())
                    val userStories = activeStories.filter { it.authorUsername == username }

                    if (userStories.isNotEmpty()) {
                        StoryViewerScreen(userStories = userStories, activeCitizenId = activeCitizenId, viewModel = storyViewerViewModel, onClose = { navController.popBackStack() })
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                    }
                }

                composable(Screen.CustomGallery.route) {
                    CustomGalleryScreen(
                        onClose = { navController.popBackStack() },
                        onImageSelected = { uri ->
                            val encodedUri = Uri.encode(uri.toString())
                            navController.navigate("${Screen.AddStory.route}/$encodedUri") {
                                popUpTo(Screen.CustomGallery.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable("${Screen.AddStory.route}/{storyUri}") { backStackEntry ->
                    val storyUriString = backStackEntry.arguments?.getString("storyUri")
                    val storyUri = storyUriString?.let { Uri.parse(Uri.decode(it)) }

                    val storyViewModel: StoryViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T { return StoryViewModel(storyRepository) as T }
                        }
                    )
                    val musicRepository = remember { MusicRepositoryImpl() }
                    val musicViewModel: MusicViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T { return MusicViewModel(musicRepository) as T }
                        }
                    )
                    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context.applicationContext))
                    val storyUiState by storyViewModel.uiState.collectAsStateWithLifecycle()
                    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(activeCitizenId) { activeCitizenId?.let { profileViewModel.loadActiveUserProfile(it) } }

                    LaunchedEffect(storyUiState.isSuccess, storyUiState.errorMessage) {
                        if (storyUiState.isSuccess) {
                            Toast.makeText(context, "Story published!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        if (storyUiState.errorMessage != null) {
                            Toast.makeText(context, "Error: ${storyUiState.errorMessage}", Toast.LENGTH_LONG).show()
                            storyViewModel.clearError()
                        }
                    }

                    var avatarUrl: String? = null
                    val successState = profileState as? ProfileUiState.Success
                    if (successState != null) {
                        avatarUrl = successState.citizen?.avatar_url
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AddStoryScreen(
                            selectedImageUri = storyUri,
                            activeUserAvatarUrl = avatarUrl,
                            musicViewModel = musicViewModel,
                            onCancel = { navController.popBackStack() },
                            onShareToStory = { uri, musicUrl ->
                                activeCitizenId?.let { userId ->
                                    storyViewModel.createStory(authorId = userId, imageUri = uri, musicUrl = musicUrl)
                                }
                            }
                        )
                    }
                }

                composable("${Screen.PostDetail.route}/{postId}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                    val postDetailViewModel: PostDetailViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T { return PostDetailViewModel(feedRepository, createPostUseCase) as T }
                        }
                    )
                    PostDetailScreen(
                        postId = postId, activeCitizenId = activeCitizenId, viewModel = postDetailViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onCommentNavigate = { commentId -> navController.navigate("${Screen.PostDetail.route}/$commentId") },
                        onAuthorClick = { authorId -> navController.navigate("${Screen.Profile.route}/$authorId") }
                    )
                }
                composable(Screen.Search.route) {
                    val searchRepository = remember {
                        SearchRepositoryImpl(
                            postDao = app.database.postDao(),
                            citizenDao = citizenDao,
                            searchDao = app.database.searchDao(),
                            supabase = app.supabase
                        )
                    }
                    val searchViewModel: SearchViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return SearchViewModel(
                                    searchRepository = searchRepository,
                                    adminRepository = adminRepository,
                                    currentUserId = activeCitizenId ?: ""
                                ) as T
                            }
                        }
                    )
                    SearchScreen(
                        viewModel = searchViewModel,
                        onPostClick = { postId -> navController.navigate("${Screen.PostDetail.route}/$postId") },
                        onAuthorClick = { authorId -> navController.navigate("${Screen.Profile.route}/$authorId") }
                    )
                }

                composable(Screen.AddPost.route) {
                    val viewModel: AddPostViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T { return AddPostViewModel(createPostUseCase) as T }
                        }
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    var avatarUrl by remember { mutableStateOf<String?>(null) }
                    LaunchedEffect(activeCitizenId) {
                        activeCitizenId?.let { id ->
                            try {
                                profileRepository.getCitizenProfile(id).first { true }.let { citizen ->
                                    avatarUrl = citizen?.avatar_url
                                }
                            } catch (_: Exception) { }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AddPostScreen(
                            activeUserAvatarUrl = avatarUrl,
                            onNavigateBack = { navController.popBackStack() },
                            onPostClick = { content, uris, pollQuestion, pollOptions, pollExpiresAt, locationName, locationLat, locationLng ->
                                activeCitizenId?.let { userId ->
                                    viewModel.createPost(
                                        authorId = userId,
                                        content = content,
                                        mediaUris = uris,
                                        pollQuestion = pollQuestion,
                                        pollOptions = pollOptions,
                                        pollExpiresAt = pollExpiresAt,
                                        locationName = locationName,
                                        locationLat = locationLat,
                                        locationLng = locationLng
                                    )
                                }
                            },
                            isUploading = uiState.isUploading,
                            isSuccess = uiState.isSuccess,
                            onSuccessHandled = {
                                viewModel.clearState()
                                navController.navigate(Screen.Feed.route) { popUpTo(navController.graph.findStartDestination().id) }
                            }
                        )
                    }
                }
                composable(Screen.Notifications.route) {
                    val getSuggestedAccountsUseCase = remember {
                        GetSuggestedAccountsUseCase(
                            citizenDao = citizenDao,
                            followDao = followDao,
                            supabase = app.supabase
                        )
                    }
                    val notificationViewModel: NotificationViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return NotificationViewModel(
                                    getNotificationsUseCase = getNotificationsUseCase,
                                    manageRealtimeNotificationsUseCase = manageRealtimeNotificationsUseCase,
                                    markNotificationReadUseCase = markNotificationReadUseCase,
                                    toggleFollowUseCase = toggleFollowUseCase,
                                    getSuggestedAccountsUseCase = getSuggestedAccountsUseCase
                                ) as T
                            }
                        }
                    )
                    NotificationScreen(
                        activeCitizenId = activeCitizenId,
                        viewModel = notificationViewModel,
                        onNavigateToProfile = { citizenId ->
                            navController.navigate("${Screen.Profile.route}/$citizenId")
                        },
                        onNavigateToPost = { postId ->
                            navController.navigate("${Screen.PostDetail.route}/$postId")
                        }
                    )
                }

                composable("${Screen.Profile.route}/{citizenId}") { backStackEntry ->
                    val targetCitizenId = backStackEntry.arguments?.getString("citizenId") ?: activeCitizenId

                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return ProfileViewModel(
                                    repository = profileRepository,
                                    getUserPostsUseCase = getPostsUseCase,
                                    postDao = app.database.postDao(),
                                    feedRepository = feedRepository,
                                    toggleFollowUseCase = toggleFollowUseCase,
                                    getFollowStatsUseCase = getFollowStatsUseCase
                                ) as T
                            }
                        }
                    )

                    LaunchedEffect(targetCitizenId, activeCitizenId) {
                        targetCitizenId?.let { id -> profileViewModel.loadProfileData(id, activeCitizenId) }
                    }

                    ProfileScreen(
                        citizenId = targetCitizenId, activeCitizenId = activeCitizenId, viewModel = profileViewModel,
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onPostClick = { postId -> navController.navigate("${Screen.PostDetail.route}/$postId") },
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToFollowers = { userId ->
                            navController.navigate("${Screen.FollowList.route}/$userId/followers")
                        },
                        onNavigateToFollowing = { userId ->
                            navController.navigate("${Screen.FollowList.route}/$userId/following")
                        }
                    )
                }

                composable("${Screen.FollowList.route}/{userId}/{type}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    val type = backStackEntry.arguments?.getString("type") ?: return@composable

                    val followListViewModel: FollowListViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return FollowListViewModel(
                                    followRepository = followRepository,
                                    toggleFollowUseCase = toggleFollowUseCase
                                ) as T
                            }
                        }
                    )

                    FollowListScreen(
                        userId = userId,
                        type = type,
                        activeCitizenId = activeCitizenId,
                        viewModel = followListViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProfile = { profileId ->
                            navController.navigate("${Screen.Profile.route}/$profileId")
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onLoginSuccess = { tokenOrId -> scope.launch {
                            sessionManager.saveSession(tokenOrId)
                            withContext(Dispatchers.IO) { app.database.clearAllTables() }
                            navController.navigate(Screen.Feed.route) { popUpTo(0) }
                        } }
                    )
                }

                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onSignUpSuccess = { tokenOrId -> scope.launch {
                            sessionManager.saveSession(tokenOrId)
                            withContext(Dispatchers.IO) { app.database.clearAllTables() }
                            navController.navigate(Screen.Feed.route) { popUpTo(0) }
                        } }
                    )
                }

                composable(Screen.Settings.route) {
                    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context.applicationContext))
                    SettingsScreen(
                        activeCitizenId = activeCitizenId, profileViewModel = profileViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onLogout = { scope.launch { sessionManager.clearSession(); withContext(Dispatchers.IO) { app.database.clearAllTables() }; navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } } },
                        onLogin = { navController.navigate(Screen.Login.route) },
                        onChangePassword = { newPassword -> changePasswordUseCase(newPassword) },
                        onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                        onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                        onNavigateToTermsOfService = { navController.navigate(Screen.TermsOfService.route) },
                        onNavigateToHelpCenter = { navController.navigate(Screen.HelpCenter.route) }
                    )
                }

                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(Screen.TermsOfService.route) {
                    TermsOfServiceScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(Screen.HelpCenter.route) {
                    HelpCenterScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(Screen.EditProfile.route) {
                    val editViewModel: EditProfileViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T { return EditProfileViewModel(profileRepository, cloudinaryService) as T }
                        }
                    )
                    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context.applicationContext))
                    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(activeCitizenId) { activeCitizenId?.let { profileViewModel.loadActiveUserProfile(it) } }

                    when (val state = profileState) {
                        is ProfileUiState.Success -> {
                            val citizen = state.citizen
                            if (citizen != null) {
                                EditProfileScreen(citizen = citizen, viewModel = editViewModel, onNavigateBack = { navController.popBackStack() })
                            }
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                composable(Screen.AdminDashboard.route) {
                    val adminViewModel: AdminViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val createAiProfileUseCase = CreateAiProfileUseCase(adminRepository, aiAgentRepository)
                                return AdminViewModel(adminRepository, createAiProfileUseCase) as T
                            }
                        }
                    )
                    AdminDashboardScreen(
                        viewModel = adminViewModel,
                        onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                        onNavigateToPosts = { navController.navigate(Screen.AdminPosts.route) },
                        onNavigateToNews = { navController.navigate(Screen.AdminNews.route) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminUsers.route) {
                    val adminViewModel: AdminViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val createAiProfileUseCase = CreateAiProfileUseCase(adminRepository, aiAgentRepository)
                                return AdminViewModel(adminRepository, createAiProfileUseCase) as T
                            }
                        }
                    )
                    AdminUsersScreen(
                        viewModel = adminViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminPosts.route) {
                    val adminViewModel: AdminViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val createAiProfileUseCase = CreateAiProfileUseCase(adminRepository, aiAgentRepository)
                                return AdminViewModel(adminRepository, createAiProfileUseCase) as T
                            }
                        }
                    )
                    AdminPostsScreen(
                        viewModel = adminViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminNews.route) {
                    val adminViewModel: AdminViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val createAiProfileUseCase = CreateAiProfileUseCase(adminRepository, aiAgentRepository)
                                return AdminViewModel(adminRepository, createAiProfileUseCase) as T
                            }
                        }
                    )
                    AdminNewsScreen(
                        viewModel = adminViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            if (showAuthDialog) AuthDialog(
                onDismiss = { showAuthDialog = false },
                onLoginClick = { showAuthDialog = false; navController.navigate(Screen.Login.route) },
                onSignUpClick = { showAuthDialog = false; navController.navigate(Screen.SignUp.route) }
            )
        }
    }
}

private fun ComponentActivity.applyStoryFullscreenSystemBars() {
    val black = Color.Black.toArgb()
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(black),
        navigationBarStyle = SystemBarStyle.dark(black)
    )
    window.statusBarColor = black
    window.navigationBarColor = black
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
    }
}

private fun ComponentActivity.applyNormalSystemBars(isLightTheme: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = true
    }
    if (isLightTheme) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                FlareLightBackground.toArgb(),
                FlareDarkBackground.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                FlareLightSurface.toArgb(),
                FlareDarkSurface.toArgb()
            )
        )
    } else {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(FlareDarkBackground.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(FlareDarkSurface.toArgb())
        )
    }
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = isLightTheme
        isAppearanceLightNavigationBars = isLightTheme
    }
}
