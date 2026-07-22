package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity

@Composable
fun ShareChatDialog(
    messages: List<ChatMessageEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Format chat messages into readable text export
    val formattedHistory = buildString {
        appendLine("--- মাহিম এআই টিউটর (Mahim AI Tutor) - আড্ডা ইতিহাস ---")
        appendLine()
        if (messages.isEmpty()) {
            appendLine("কোনো বার্তা নেই।")
        } else {
            messages.forEach { msg ->
                val senderName = if (msg.sender == "USER") "তুমি" else "মাহিম এআই টিউটর"
                appendLine("[$senderName]:")
                appendLine(msg.text.trim())
                appendLine()
            }
        }
        appendLine("--- মাহিম এআই টিউটর অ্যাপ থেকে শেয়ারকৃত ---")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
                Text(
                    text = "আড্ডা ইতিহাস শেয়ার করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = "আপনার মোট ${messages.size} টি বার্তা রপ্তানি বা শেয়ার করতে প্রস্তুত:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Preview Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = formattedHistory,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Share via Android Share Sheet
                Button(
                    onClick = {
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "মাহিম এআই টিউটর - চ্যাট ইতিহাস")
                                putExtra(Intent.EXTRA_TEXT, formattedHistory)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "চ্যাট ইতিহাস শেয়ার করুন"))
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "শেয়ার করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "অ্যাপে শেয়ার করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Copy to Clipboard
                OutlinedButton(
                    onClick = {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Mahim AI Tutor Chat", formattedHistory)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "চ্যাট ইতিহাস ক্লিপবোর্ডে কপি করা হয়েছে! 📋", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "কপি করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "টেক্সট কপি করুন", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "বাতিল", fontSize = 13.sp)
            }
        }
    )
}
