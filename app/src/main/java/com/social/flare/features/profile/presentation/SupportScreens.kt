package com.social.flare.features.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class SupportSection(
    val title: String,
    val content: String,
    val highlighted: Boolean = false
)

@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    SupportDocumentScreen(
        title = "Privacy Policy",
        onNavigateBack = onNavigateBack,
        sections = privacyPolicySections()
    )
}

@Composable
fun TermsOfServiceScreen(onNavigateBack: () -> Unit) {
    SupportDocumentScreen(
        title = "Terms of Service",
        onNavigateBack = onNavigateBack,
        sections = termsOfServiceSections()
    )
}

@Composable
fun HelpCenterScreen(onNavigateBack: () -> Unit) {
    SupportDocumentScreen(
        title = "Help Center",
        onNavigateBack = onNavigateBack,
        sections = helpCenterSections()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportDocumentScreen(
    title: String,
    onNavigateBack: () -> Unit,
    sections: List<SupportSection>
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = title,
                    color = colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(sections) { section ->
                SupportSectionCard(section = section)
            }
        }
    }
}

@Composable
private fun SupportSectionCard(section: SupportSection) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (section.highlighted) colorScheme.surfaceVariant else colorScheme.surface
    val bodyColor = if (section.highlighted) colorScheme.onSurfaceVariant else colorScheme.onSurface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = bodyColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (section.highlighted) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = section.title,
                color = bodyColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.35f))
            Text(
                text = section.content,
                color = bodyColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun privacyPolicySections(): List<SupportSection> = listOf(
    SupportSection(
        title = "Introduction",
        content = "This Privacy Policy explains how Flare handles information used to provide profiles, posts, Stories, comments, search, notifications, and settings. It is intended to describe the app behavior in clear language and does not create features that are not currently implemented.",
        highlighted = true
    ),
    SupportSection(
        title = "Account Information",
        content = "Flare may use account details such as display name, username, email address, authentication identifiers, avatar, profile biography, and profile metadata needed to identify your account and show your profile inside the app."
    ),
    SupportSection(
        title = "Profiles, Posts, Comments, Followers, and Interactions",
        content = "The app stores and displays information you choose to create or interact with, including profile details, posts, comments, replies, likes, saved or shared content, followers, following relationships, and related timestamps. These items may be visible to other users depending on the screen and feature where they appear."
    ),
    SupportSection(
        title = "Images and Multimedia",
        content = "When you upload profile photos, post media, or Stories, the selected files may be processed and stored so they can be displayed in the app. Media previews, thumbnails, and links may be used to load that content efficiently."
    ),
    SupportSection(
        title = "Location",
        content = "Location is used only when you choose to add a location to a post or another supported item. Flare should not describe location as active tracking unless a feature explicitly asks for permission and you provide location information."
    ),
    SupportSection(
        title = "Local Preferences",
        content = "Some preferences are saved locally on this device, including theme, text size, push notification preference, email notification preference, and privacy preferences. Current privacy preferences are local settings and may not enforce remote visibility rules until remote privacy fields and backend enforcement are added.",
        highlighted = true
    ),
    SupportSection(
        title = "How Information Is Used",
        content = "Information is used to sign you in, show your profile, publish content, load feeds, support comments and interactions, search for users or content, display notifications, and keep your selected app preferences."
    ),
    SupportSection(
        title = "Local Storage and Remote Sync",
        content = "Flare may keep some data on the device for app settings and local app behavior. Account, profile, post, media, notification, and interaction data may also be synchronized with remote services when those features require it."
    ),
    SupportSection(
        title = "Services Used by the App",
        content = "The app uses services such as Supabase for authentication, data storage, and remote app data, and Cloudinary for media upload and delivery. These services are used to support app functionality rather than to sell personal information."
    ),
    SupportSection(
        title = "Security",
        content = "Flare relies on platform security, authentication, and service-level protections to help protect data. You should keep your account credentials private, use a strong password, and sign out on shared devices."
    ),
    SupportSection(
        title = "Retention and Deletion",
        content = "Information may remain available while it is needed to provide app features or until it is removed through available app actions. Some records may remain in local storage or remote systems until synchronization, cleanup, or deletion workflows are completed."
    ),
    SupportSection(
        title = "User Controls",
        content = "You can edit supported profile fields, change your password, adjust local notification preferences, change theme and text size, and manage available privacy preferences. Some settings are local only and do not imply remote backend enforcement."
    ),
    SupportSection(
        title = "Minors",
        content = "Flare should be used responsibly and in accordance with any rules that apply to the environment where the app is deployed. The app does not define a separate verified parental consent workflow in this document."
    ),
    SupportSection(
        title = "Changes to This Policy",
        content = "This policy may be updated as the app changes. New or changed features should be reflected here when they affect how information is handled."
    ),
    SupportSection(
        title = "Help and Clarifications",
        content = "If you need help or clarification, use the available support or project communication channels provided by the team maintaining this app. This document does not invent a legal address, registered company, phone number, or support email."
    )
)

private fun termsOfServiceSections(): List<SupportSection> = listOf(
    SupportSection(
        title = "Acceptance of Terms",
        content = "By using Flare, you agree to use the app responsibly and follow these terms. These terms are an internal informational document for the app and are not professional legal advice.",
        highlighted = true
    ),
    SupportSection(
        title = "Requirements to Use the App",
        content = "You should use Flare only if you can follow the app rules, respect other users, and provide accurate account information where required. Some features may require a logged-in account."
    ),
    SupportSection(
        title = "Account Creation and Security",
        content = "You are responsible for keeping your account credentials secure and for activity that occurs through your account. If you use a shared device, sign out when you are finished."
    ),
    SupportSection(
        title = "User Responsibility",
        content = "You are responsible for the content you create, upload, share, comment on, or interact with. Do not use the app to harm others, disrupt the service, impersonate people, or misrepresent information."
    ),
    SupportSection(
        title = "Allowed and Prohibited Content",
        content = "Do not post content that is abusive, harassing, illegal, hateful, sexually exploitative, intentionally misleading, spam, or invasive of another person's privacy. Content should respect other users and the purpose of the app."
    ),
    SupportSection(
        title = "Posts, Comments, Images, Polls, and Stories",
        content = "You may create posts, comments, images, polls, locations, and Stories where those features are available. You should only upload content you have the right to use and share."
    ),
    SupportSection(
        title = "Respect for Other Users",
        content = "Treat other users with respect. Do not threaten, harass, bully, expose private information, or use Flare to coordinate harmful behavior."
    ),
    SupportSection(
        title = "Moderation and Content Removal",
        content = "Content may be edited, hidden, removed, or moderated when it violates rules, creates risk, or interferes with the app experience. Administrative tools may support review or removal where implemented."
    ),
    SupportSection(
        title = "Suspension or Termination",
        content = "Accounts may be restricted, suspended, or removed when misuse is detected or when continued access would create risk for users or the service. The specific enforcement tools depend on implemented app features."
    ),
    SupportSection(
        title = "Ownership of User Content",
        content = "You keep responsibility for content you create. By posting content in Flare, you allow the app to store, display, and process that content as needed to provide the feature where you posted it."
    ),
    SupportSection(
        title = "Permitted Use",
        content = "Use Flare for normal social and community interactions supported by the app. Do not attempt to bypass security, scrape data, overload systems, reverse engineer restricted parts, or interfere with other users."
    ),
    SupportSection(
        title = "Availability and Changes",
        content = "Flare may change over time. Features may be added, adjusted, temporarily unavailable, or removed as the project evolves."
    ),
    SupportSection(
        title = "Third-Party Services",
        content = "Some features rely on services such as Supabase and Cloudinary. Those services support authentication, data, and media behavior required by the app."
    ),
    SupportSection(
        title = "Reasonable Limitation of Responsibility",
        content = "Flare is provided as an app experience connected to its project context. The maintainers should work to keep the app reliable, but no document here guarantees uninterrupted service, complete error-free behavior, or professional legal advice."
    ),
    SupportSection(
        title = "Changes to These Terms",
        content = "These terms may be updated when app behavior, rules, or project expectations change. Continued use after updates means you should follow the latest version shown in the app."
    ),
    SupportSection(
        title = "Requesting Help",
        content = "Use the support or project communication channels provided by the app team when you need help. This document does not invent a company, jurisdiction, address, commercial term, email, or phone number."
    )
)

private fun helpCenterSections(): List<SupportSection> = listOf(
    SupportSection(
        title = "Getting Started",
        content = "Open Flare, sign in or continue as a guest where supported, and use the bottom navigation to move between Feed, Search, Add Post, Notifications, and Profile. Some actions require a logged-in account.",
        highlighted = true
    ),
    SupportSection(
        title = "Create an Account",
        content = "Use the Sign Up screen to create an account with the required fields. After successful registration, the app signs you in and takes you to the main experience."
    ),
    SupportSection(
        title = "Sign In and Sign Out",
        content = "Use Login to access your account. You can sign out from Settings. Guest users can browse supported areas but may be asked to log in before posting, following, or using private account actions."
    ),
    SupportSection(
        title = "Recover or Change Password",
        content = "Change Password is available from Settings for logged-in users and uses the authentication flow implemented in the app. Do not assume a forgot-password email is sent unless that feature is explicitly available in the current build."
    ),
    SupportSection(
        title = "Edit Profile",
        content = "Open your Profile and choose Edit Profile to update supported fields such as display name, username, biography, avatar, or other fields shown by the screen."
    ),
    SupportSection(
        title = "Publish Text, Images, Polls, and Location",
        content = "Use New Post to write content, attach supported media, add poll options, or include location information when available. Location is only attached when you choose to add it."
    ),
    SupportSection(
        title = "Create and View Stories",
        content = "Use the story area in Feed to view Stories or start the Add Story flow. The viewer is fullscreen and includes its own controls for progress, closing, and available owner actions."
    ),
    SupportSection(
        title = "Search Users and Content",
        content = "Use Search to look for users, posts, trends, news, or categories supported by the current app build. Results depend on available local and remote data."
    ),
    SupportSection(
        title = "Follow and Unfollow",
        content = "Open a user's profile to follow or unfollow when the action is available. Followers and Following lists show related users and allow navigation back to profiles."
    ),
    SupportSection(
        title = "Notifications",
        content = "Notifications show activity related to your account where implemented. Push Notifications can be enabled or disabled in Settings and may require Android notification permission."
    ),
    SupportSection(
        title = "Dark Mode",
        content = "Use Settings to switch Dark Mode on or off. The app theme updates globally and should keep the Flare orange accent in both modes."
    ),
    SupportSection(
        title = "Text Size",
        content = "Use the Text Size slider in Settings to adjust global text scale. The app preserves Android accessibility font scale and applies the selected Flare scale across screens."
    ),
    SupportSection(
        title = "Privacy",
        content = "Privacy Settings are available for logged-in users. Current privacy preferences are saved on this device and should not be described as remote backend enforcement until that backend functionality exists.",
        highlighted = true
    ),
    SupportSection(
        title = "Edit or Delete Posts",
        content = "Where available, post actions allow editing or deleting your own content. Menus and actions may vary by screen, ownership, and current implementation."
    ),
    SupportSection(
        title = "Permissions",
        content = "Camera, gallery, location, and notification features may require Android permissions. If a feature does not work, check system permission settings and try the action again."
    ),
    SupportSection(
        title = "Connection or Loading Problems",
        content = "If content does not load, check your connection, refresh or reopen the screen, and try again. Remote data depends on available services and network access."
    ),
    SupportSection(
        title = "Frequently Asked Questions",
        content = "Why do some actions ask me to log in? Some features need an account. Why do email notifications not send emails? The current setting may be local unless backend email delivery is implemented. Why do privacy switches not hide everything remotely? Remote enforcement requires backend privacy fields and rules."
    ),
    SupportSection(
        title = "Reporting a Problem",
        content = "When reporting a problem, include the screen name, what you tapped, what you expected, what happened instead, whether you were logged in or guest, and any visible error message."
    )
)
