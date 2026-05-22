package com.social.flare.features.main.presentation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

import com.social.flare.FlareApp
import com.social.flare.core.data.SessionManager
import com.social.flare.core.navigation.Screen
import com.social.flare.core.ui.components.AuthDialog
import com.social.flare.features.auth.presentation.LoginScreen
import com.social.flare.features.auth.presentation.SignUpScreen
import com.social.flare.features.feed.presentation.FeedScreen
import com.social.flare.features.profile.presentation.ProfileScreen
import com.social.flare.features.profile.presentation.SettingsScreen
import com.social.flare.features.search.presentation.SearchScreen
import com.social.flare.features.search.presentation.SearchViewModel

import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import com.social.flare.features.feed.data.repository.StoryRepositoryImpl
import com.social.flare.features.feed.domain.usecase.GetFeedUseCase
import com.social.flare.features.feed.presentation.FeedViewModel
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
import com.social.flare.features.admin.presentation.AdminDashboardScreen
import com.social.flare.features.admin.presentation.AdminUsersScreen
import com.social.flare.features.admin.presentation.AdminPostsScreen
import com.social.flare.features.admin.presentation.AdminNewsScreen
import com.social.flare.features.admin.presentation.viewmodel.AdminViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var showAuthDialog by remember { mutableStateOf(false) }

    val app = context.applicationContext as FlareApp
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()
    val activeCitizenId by sessionManager.activeCitizenIdFlow.collectAsStateWithLifecycle(initialValue = null)

    val cloudinaryService = remember { CloudinaryService(context) }
    val followDao = remember { app.database.followDao() }
    val feedRepository = remember { FeedRepositoryImpl(app.database.postDao(), followDao) }
    val profileRepository = remember { ProfileRepositoryImpl(app.database.citizenDao()) }
    val storyRepository = remember { StoryRepositoryImpl(app.database.storyDao(), cloudinaryService) }
    val followRepository = remember { FollowRepositoryImpl(followDao) }

    val okHttpClient = remember { okhttp3.OkHttpClient() }
    val notificationRepository = remember {
        NotificationRepositoryImpl(app.database.notificationDao(), okHttpClient)
    }
    val toggleFollowUseCase = remember { ToggleFollowUseCase(followRepository) }
    val getFollowStatsUseCase = remember { GetFollowStatsUseCase(followRepository) }
    val getPostsUseCase = remember { GetUserPostsUseCase(feedRepository) }
    val getFeedUseCase = remember { GetFeedUseCase(feedRepository) }
    val deletePostUseCase = remember { DeletePostUseCase(feedRepository, cloudinaryService) }
    val updatePostUseCase = remember { UpdatePostUseCase(feedRepository) }
    val createPostUseCase = remember { CreatePostUseCase(feedRepository, cloudinaryService) }
    val getNotificationsUseCase = remember { GetNotificationsUseCase(notificationRepository) }
    val manageRealtimeNotificationsUseCase = remember {
        ManageRealtimeNotificationsUseCase(
            notificationRepository
        )
    }
    val markNotificationReadUseCase = remember { MarkNotificationReadUseCase(notificationRepository) }
    val adminRepository = remember { AdminRepositoryImpl(app.database.citizenDao(), app.database.postDao(), app.database.newsDao()) }

    Scaffold(
        topBar = {
            val hideTopBarRoutes = listOf(
                Screen.Login.route, Screen.SignUp.route,
                Screen.AdminDashboard.route, Screen.AdminUsers.route,
                Screen.AdminPosts.route, Screen.AdminNews.route
            )
            if (currentRoute !in hideTopBarRoutes) {
                FlareTopBar(onSettingsClick = { navController.navigate(Screen.Settings.route) })
            }
        },
        bottomBar = {
            val adminRoutes = listOf(
                Screen.AdminDashboard.route, Screen.AdminUsers.route,
                Screen.AdminPosts.route, Screen.AdminNews.route
            )
            val isMainTab = !adminRoutes.contains(currentRoute) && (
                listOf(
                    Screen.Feed.route, Screen.Search.route, Screen.AddPost.route, Screen.Notifications.route
                ).contains(currentRoute) || currentRoute?.startsWith(Screen.Profile.route) == true
            )

            if (isMainTab) {
                FlareBottomNavigation(
                    currentRoute = currentRoute ?: Screen.Feed.route,
                    isGuest = activeCitizenId == null,
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
        containerColor = Color.Black
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                        if (activeCitizenId != null) {
                            feedViewModel.loadFeed(activeCitizenId!!)
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
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
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
                    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
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
                    if (profileState is ProfileUiState.Success) {
                        val citizenFlow = (profileState as ProfileUiState.Success).citizen
                        val currentCitizen by citizenFlow.collectAsStateWithLifecycle(initialValue = null)
                        avatarUrl = currentCitizen?.avatar_url
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AddStoryScreen(
                            selectedImageUri = storyUri, activeUserAvatarUrl = avatarUrl,
                            onCancel = { navController.popBackStack() },
                            onShareToStory = { uri -> activeCitizenId?.let { userId -> storyViewModel.createStory(authorId = userId, imageUri = uri) } }
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
                    val searchViewModel: SearchViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return SearchViewModel(adminRepository) as T
                            }
                        }
                    )
                    SearchScreen(viewModel = searchViewModel)
                }

                composable(Screen.AddPost.route) {
                    val viewModel: AddPostViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T { return AddPostViewModel(createPostUseCase) as T }
                        }
                    )
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        AddPostScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onPostClick = { content, uris -> activeCitizenId?.let { userId -> viewModel.createPost(userId, content, uris) } },
                            isSuccess = uiState.isSuccess,
                            onSuccessHandled = {
                                viewModel.clearState()
                                navController.navigate(Screen.Feed.route) { popUpTo(navController.graph.findStartDestination().id) }
                            }
                        )
                    }
                }
                composable(Screen.Notifications.route) {
                    val notificationViewModel: NotificationViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return NotificationViewModel(
                                    getNotificationsUseCase = getNotificationsUseCase,
                                    manageRealtimeNotificationsUseCase = manageRealtimeNotificationsUseCase,
                                    markNotificationReadUseCase = markNotificationReadUseCase,
                                    toggleFollowUseCase = toggleFollowUseCase
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
                        onPostClick = { postId -> navController.navigate("${Screen.PostDetail.route}/$postId") }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onLoginSuccess = { tokenOrId -> scope.launch { sessionManager.saveSession(tokenOrId); navController.navigate(Screen.Feed.route) { popUpTo(0) } } }
                    )
                }

                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onSignUpSuccess = { tokenOrId -> scope.launch { sessionManager.saveSession(tokenOrId); navController.navigate(Screen.Feed.route) { popUpTo(0) } } }
                    )
                }

                composable(Screen.Settings.route) {
                    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
                    SettingsScreen(
                        activeCitizenId = activeCitizenId, profileViewModel = profileViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                        onLogout = { scope.launch { sessionManager.clearSession(); navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } } },
                        onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) }
                    )
                }

                composable(Screen.EditProfile.route) {
                    val editViewModel: EditProfileViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T { return EditProfileViewModel(profileRepository, cloudinaryService) as T }
                        }
                    )
                    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(context))
                    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(activeCitizenId) { activeCitizenId?.let { profileViewModel.loadActiveUserProfile(it) } }

                    if (profileState is ProfileUiState.Success) {
                        val currentCitizen by (profileState as ProfileUiState.Success).citizen.collectAsStateWithLifecycle(initialValue = null)
                        if (currentCitizen != null) {
                            EditProfileScreen(citizen = currentCitizen!!, viewModel = editViewModel, onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }

                composable(Screen.AdminDashboard.route) {
                    val adminViewModel: AdminViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return AdminViewModel(adminRepository) as T
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
                                return AdminViewModel(adminRepository) as T
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
                                return AdminViewModel(adminRepository) as T
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
                                return AdminViewModel(adminRepository) as T
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