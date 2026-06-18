package com.social.flare.features.post.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PollData(
    val question: String = "",
    val options: List<String> = listOf("", "")
) {
    val isValid: Boolean get() = question.isNotBlank() && options.count { it.isNotBlank() } >= 2
    val nonEmptyOptions: List<String> get() = options.filter { it.isNotBlank() }
}

@Composable
fun AddPollSection(
    pollData: PollData,
    onQuestionChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onRemovePoll: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Poll, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Encuesta", color = colorScheme.onSurface, fontSize = 14.sp)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onRemovePoll, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove poll", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ThemedPollTextField(
            value = pollData.question,
            onValueChange = { if (it.length <= 200) onQuestionChange(it) },
            placeholder = "Pregunta de la encuesta",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        pollData.options.forEachIndexed { index, option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "${index + 1}.",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.width(20.dp)
                )
                ThemedPollTextField(
                    value = option,
                    onValueChange = { if (it.length <= 100) onOptionChange(index, it) },
                    placeholder = "Opción ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                if (pollData.options.size > 2) {
                    IconButton(onClick = { onRemoveOption(index) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Remove option", tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.width(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (pollData.options.size < 4) {
            Spacer(modifier = Modifier.height(4.dp))
            IconButton(
                onClick = onAddOption,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add option", tint = colorScheme.primary)
            }
        }
    }
}

@Composable
private fun ThemedPollTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surfaceVariant,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface,
            focusedBorderColor = colorScheme.primary,
            unfocusedBorderColor = colorScheme.outline,
            cursorColor = colorScheme.primary,
            focusedPlaceholderColor = colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = colorScheme.onSurfaceVariant
        ),
        singleLine = true,
        modifier = modifier,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
    )
}
