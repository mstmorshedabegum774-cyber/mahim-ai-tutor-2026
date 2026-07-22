package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class QuickPrompt(
    val icon: String,
    val title: String,
    val prompt: String
)

val sampleQuickPrompts = listOf(
    QuickPrompt("🌌", "মহাকাশ", "সূর্য ও চাঁদের গল্প সহজ করে বলো তো!"),
    QuickPrompt("🦁", "পশুপাখি", "পাখিরা কীভাবে আকাশে উড়তে পারে?"),
    QuickPrompt("🔬", "বিজ্ঞান", "মেঘ থেকে কীভাবে সুন্দর বৃষ্টি পড়ে?"),
    QuickPrompt("📐", "মজার গণিত", "যোগ করা আর বিয়োগ করা সহজভাবে বুঝিয়ে দাও!"),
    QuickPrompt("📜", "ছোট গল্প", "একটি সুন্দর শিক্ষণীয় ছোট গল্প বলো!"),
    QuickPrompt("🧩", "মজার ধাঁধা", "আমাকে একটি মজার ধাঁধা জিজ্ঞেস করো দেখি!")
)

@Composable
fun SubjectCategoryChips(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sampleQuickPrompts) { prompt ->
            Surface(
                modifier = Modifier.clickable { onPromptSelected(prompt.prompt) },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = prompt.icon, fontSize = 16.sp)
                    Text(
                        text = prompt.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}
