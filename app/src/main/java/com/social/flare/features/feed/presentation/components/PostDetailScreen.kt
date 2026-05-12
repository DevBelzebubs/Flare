package com.social.flare.features.feed.presentation.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { MainPostDetail() }

            item {
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                Text(
                    text = "Comments (5)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                CommentItem(
                    username = "micah_bell", time = "2 h ago",
                    content = "You're not better than me blacklung...",
                    likes = 145, replies = 2,
                    isReply = false
                )
            }
            item {
                CommentItem(
                    username = "plan_der_linde", time = "55 min ago",
                    content = "Hey, let's go for Bronte, he's such an asshole, we must to appart him to reach our goal",
                    likes = 21, replies = 0,
                    isReply = true
                )
            }
            item {
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                CommentItem(
                    username = "strainer_marston", time = "1 h ago",
                    content = "So... this is the end you say... idk Arthur, maybe we can do something else",
                    likes = 72, replies = 1,
                    isReply = false
                )
            }
            item {
                CommentItem(
                    username = "morgan_1899", time = "1 h ago",
                    content = "No, this is getting an end, don't be a fool and be a godamn father and a godamn husban for your family, John Marston",
                    likes = 0, replies = 0,
                    isReply = true
                )
            }
        }
    }
}

@Composable
private fun MainPostDetail() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Red)) // Avatar Placeholder
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Morgan_1899", color = Color.White, fontWeight = FontWeight.Bold)
                Text("6 h ago", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Text(
            text = "I were diagnosed with tuberculosis, idk how much i will still standing, Dutch is going crazy and micah is earning power for that, this gang is broken, without Hosea, Lenny and even Kieran and the pinkerton in our shoes, this is only the beggining of the end..",
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color.DarkGray))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatColumn("4,572", "likes")
            StatColumn("935", "comments")
            StatColumn("89", "shares")
        }

        HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                IconTextButton(Icons.Outlined.FavoriteBorder, "Like")
                Spacer(modifier = Modifier.width(16.dp))
                IconTextButton(Icons.Outlined.ChatBubbleOutline, "Comment")
                Spacer(modifier = Modifier.width(16.dp))
                IconTextButton(Icons.Outlined.Send, "Share")
            }
            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save", tint = Color.White)
        }
    }
}

@Composable
private fun StatColumn(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
private fun IconTextButton(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun CommentItem(
    username: String, time: String, content: String,
    likes: Int, replies: Int, isReply: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 56.dp else 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        if (isReply) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(Color.DarkGray)
                    .padding(end = 12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isReply) Color(0xFFCC5500) else Color.DarkGray))
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(time, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(content, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("$likes", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.width(16.dp))

                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Reply", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("$replies", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.width(16.dp))
                Text("Reply", color = Color(0xFFFF5722), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}