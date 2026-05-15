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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

import com.social.flare.core.media.CloudinaryService
import com.social.flare.features.feed.data.repository.FeedRepositoryImpl
import com.social.flare.features.feed.presentation.FeedViewModel
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
import com.social.flare.features.profile.data.repository.ProfileRepositoryImpl
import com.social.flare.features.profile.presentation.EditProfileScreen
import com.social.flare.features.profile.presentation.ProfileViewModelFactory
import com.social.flare.features.profile.presentation.viewmodel.EditProfileViewModel
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var showAuthDialog by remember { mutableStateOf(false) }
    val app = context.applicationContext as FlareApp
    val feedRepository = remember { FeedRepositoryImpl(app.database.postDao()) }
    val getPostsUseCase = remember { GetUserPostsUseCase(feedRepository) }
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()
    val activeCitizenId by sessionManager.activeCitizenIdFlow.collectAsState(initial = null)
    Scaffold(
        topBar = {
            if (currentRoute != Screen.Login.route && currentRoute != Screen.SignUp.route) {
                FlareTopBar(onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                })
            }
        },
        bottomBar = {
            val isMainTab = listOf(
                Screen.Feed.route, Screen.Search.route, Screen.AddPost.route,
                Screen.Notifications.route, Screen.Profile.route
            ).contains(currentRoute)

            if (isMainTab) {
                FlareBottomNavigation(
                    currentRoute = currentRoute ?: Screen.Feed.route,
                    isGuest = activeCitizenId == null,
                    onRequireAuth = { showAuthDialog = true },
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Feed.route
            ) {
                composable(Screen.Feed.route) {
                    val app = context.applicationContext as FlareApp
                    val repository = FeedRepositoryImpl(app.database.postDao())

                    val getFeedUseCase =
                        com.social.flare.features.feed.domain.usecase.GetFeedUseCase(repository)
                    val deletePostUseCase =
                        com.social.flare.features.post.domain.usecase.DeletePostUseCase(repository)
                    val updatePostUseCase =
                        com.social.flare.features.post.domain.usecase.UpdatePostUseCase(repository)
                    val profileRepository = remember {
                        ProfileRepositoryImpl(app.database.citizenDao())
                    }
                    val feedViewModel: FeedViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return FeedViewModel(
                                    getFeedUseCase,
                                    deletePostUseCase,
                                    updatePostUseCase,
                                    repository,
                                    profileRepository
                                ) as T
                            }
                        }
                    )
                    LaunchedEffect(activeCitizenId) {
                        activeCitizenId?.let { userId ->
                            feedViewModel.loadFeed(userId)
                        }
                    }
                    FeedScreen(
                        activeCitizenId = activeCitizenId,
                        viewModel = feedViewModel,
                        onRequireAuth = { showAuthDialog = true },
                        onPostClick = { postId ->
                            navController.navigate("${Screen.PostDetail.route}/$postId")
                        },
                        onStoryClick = { username ->
                            navController.navigate("${Screen.StoryViewer.route}/$username")
                        },
                        onNavigateToAddStory = {
                            navController.navigate(Screen.CustomGallery.route)
                        }
                    )
                }
                composable("${Screen.StoryViewer.route}/{username}") { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    StoryViewerScreen(
                        onClose = { navController.popBackStack() }
                    )
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

                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = ProfileViewModelFactory(context)
                    )
                    val profileState by profileViewModel.uiState.collectAsState()
                    LaunchedEffect(activeCitizenId) {
                        activeCitizenId?.let { profileViewModel.loadActiveUserProfile(it) }
                    }

                    var avatarUrl: String? = null
                    if (profileState is ProfileUiState.Success) {
                        val citizenFlow = (profileState as ProfileUiState.Success).citizen
                        val currentCitizen by citizenFlow.collectAsState(initial = null)
                        avatarUrl = currentCitizen?.avatar_url
                    }
                    AddStoryScreen(
                        selectedImageUri = storyUri,
                        activeUserAvatarUrl = avatarUrl,
                        onCancel = { navController.popBackStack() },
                        onShareToStory = { uri ->
                            Toast.makeText(context, "Subiendo historia a Flare...", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    )
                }
                composable("${Screen.PostDetail.route}/{postId}") { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                    val postDetailViewModel: PostDetailViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return PostDetailViewModel(feedRepository) as T
                            }
                        }
                    )
                    PostDetailScreen(
                        postId = postId,
                        activeCitizenId = activeCitizenId,
                        viewModel = postDetailViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen()
                }

                composable(Screen.AddPost.route) {
                    val app = context.applicationContext as FlareApp
                    val repository = FeedRepositoryImpl(app.database.postDao())
                    val cloudinaryService = CloudinaryService(context)
                    val useCase = CreatePostUseCase(repository, cloudinaryService)

                    val viewModel: AddPostViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return AddPostViewModel(useCase) as T
                            }
                        }
                    )

                    val uiState by viewModel.uiState.collectAsState()

                    LaunchedEffect(uiState.isSuccess, uiState.errorMessage) {
                        if (uiState.isSuccess) {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                            }
                        }
                        if (uiState.errorMessage != null) {
                            Toast.makeText(
                                context,
                                "Error: ${uiState.errorMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearError()
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        AddPostScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onPostClick = { content, uris ->
                                activeCitizenId?.let { userId ->
                                    viewModel.createPost(
                                        authorId = userId,
                                        content = content,
                                        mediaUris = uris
                                    )
                                }
                            }
                        )

                        if (uiState.isUploading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color(0xFFFF5722))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Uploading to Flare...", color = Color.White)
                                }
                            }
                        }
                    }
                }

                composable(Screen.Notifications.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Notifications Screen", color = Color.White)
                    }
                }

                composable(Screen.Profile.route) {
                    val profileRepository = remember {
                        com.social.flare.features.profile.data.repository.ProfileRepositoryImpl(app.database.citizenDao())
                    }
                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return ProfileViewModel(
                                    repository = profileRepository,
                                    getUserPostsUseCase = getPostsUseCase
                                ) as T
                            }
                        }
                    )
                    ProfileScreen(
                        citizenId = activeCitizenId,
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route)
                        },
                        onPostClick = { postId ->
                            navController.navigate("${Screen.PostDetail.route}/$postId")
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onLoginSuccess = { tokenOrId ->
                            scope.launch {
                                sessionManager.saveSession(tokenOrId)
                                navController.navigate(Screen.Feed.route) { popUpTo(0) }
                            }
                        }
                    )
                }

                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onSignUpSuccess = { tokenOrId ->
                            scope.launch {
                                sessionManager.saveSession(tokenOrId)
                                navController.navigate(Screen.Feed.route) { popUpTo(0) }
                            }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditProfile = {
                            navController.navigate(Screen.EditProfile.route)
                        }
                    )
                }
                composable(Screen.EditProfile.route) {
                    val profileRepository = remember {
                        com.social.flare.features.profile.data.repository.ProfileRepositoryImpl(app.database.citizenDao())
                    }
                    val cloudinaryService =
                        remember { com.social.flare.core.media.CloudinaryService(context) }
                    val editViewModel: EditProfileViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return EditProfileViewModel(
                                    profileRepository,
                                    cloudinaryService
                                ) as T
                            }
                        }
                    )
                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = ProfileViewModelFactory(context)
                    )

                    val profileState by profileViewModel.uiState.collectAsState()
                    LaunchedEffect(activeCitizenId) {
                        activeCitizenId?.let { profileViewModel.loadActiveUserProfile(it) }
                    }

                    if (profileState is ProfileUiState.Success) {
                        val citizenFlow = (profileState as ProfileUiState.Success).citizen
                        val currentCitizen by citizenFlow.collectAsState(initial = null)

                        if (currentCitizen != null) {
                            EditProfileScreen(
                                citizen = currentCitizen!!,
                                viewModel = editViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFFF5722))
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFFF5722))
                        }
                    }
                }
            }

            if (showAuthDialog) {
                AuthDialog(
                    onDismiss = { showAuthDialog = false },
                    onLoginClick = {
                        showAuthDialog = false
                        navController.navigate(Screen.Login.route)
                    },
                    onSignUpClick = {
                        showAuthDialog = false
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }
        }
    }
}