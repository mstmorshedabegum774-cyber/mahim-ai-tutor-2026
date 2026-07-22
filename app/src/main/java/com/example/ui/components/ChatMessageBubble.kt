package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.StarYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    isSpeakingThisMsg: Boolean,
    onToggleBookmark: (Long, Boolean) -> Unit,
    onSpeakMessage: (String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_tutor_icon_1784706207133),
                    contentDescription = "Mahim Avatar",
                    modifier = Modifier.size(32.dp).clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Sender name tag
            Text(
                text = if (isUser) "তুমি" else "মাহিম এআই টিউটর 🦉",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF535C68),
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp, end = 4.dp)
            )

            // Bubble
            val bubbleColor by animateColorAsState(
                targetValue = if (isUser) {
                    com.example.ui.theme.UserBubbleColor
                } else {
                    com.example.ui.theme.StoryBubbleBg
                },
                label = "bubbleColor"
            )

            val textColor = if (isUser) com.example.ui.theme.OnUserBubbleText else Color(0xFF2D3436)

            Surface(
                color = bubbleColor,
                contentColor = textColor,
                shape = if (isUser) {
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
                },
                shadowElevation = 1.dp,
                border = if (!isUser) {
                    androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.StoryBorderColor)
                } else null
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                        lineHeight = 22.sp,
                        fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                    )

                    // Footer with Time + Actions for Tutor
                    Row(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .align(if (isUser) Alignment.End else Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = if (isUser) com.example.ui.theme.OnUserBubbleText.copy(alpha = 0.8f) else Color(0xFF636E72)
                        )

                        if (!isUser) {
                            Spacer(modifier = Modifier.width(8.dp))

                            // TTS Read Aloud
                            IconButton(
                                onClick = { onSpeakMessage(message.text, message.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeakingThisMsg) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = "Read Aloud",
                                    tint = if (isSpeakingThisMsg) MaterialTheme.colorScheme.primary else Color(0xFF636E72),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Bookmark
                            IconButton(
                                onClick = { onToggleBookmark(message.id, message.isBookmarked) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (message.isBookmarked) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (message.isBookmarked) StarYellow else Color(0xFF636E72),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Copy
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Mahim Tutor Answer", message.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "লেখাটি কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Text",
                                    tint = Color(0xFF636E72),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
