package com.social.flare.core.navigation

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Search : Screen("search")
    object AddPost : Screen("add_post")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Settings : Screen("settings")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object HelpCenter : Screen("help_center")
    object PostDetail: Screen("post_details")
    object EditProfile : Screen("edit_profile")
    object StoryViewer : Screen("story_viewer")
    object AddStory : Screen("add_story")
    object CustomGallery : Screen("custom_gallery")

    object AdminDashboard : Screen("admin_dashboard")
    object AdminUsers : Screen("admin_users")
    object AdminPosts : Screen("admin_posts")
    object AdminNews : Screen("admin_news")
    object FollowList : Screen("follow_list")
    object Splash : Screen("splash")
}
