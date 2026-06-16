package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.auth.data.local.entity.CitizenEntity

@Composable
fun ProfileInfoSection(citizen: CitizenEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = citizen.display_name,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text(
            text = citizen.username,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )
        if (!citizen.bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = citizen.bio,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.5.dp,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}
