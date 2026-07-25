package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SillokRecord
import com.example.ui.theme.JoseonGoldSecondary
import com.example.ui.theme.JoseonIndigoTertiary
import com.example.ui.theme.JoseonRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnsShareBottomSheet(
    record: SillokRecord,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val shareText = buildShareMessage(record)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "조선왕조실록 SNS 연동 및 공유",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "검색한 실록 사건 및 역사 장소 정보를 SNS 친구들과 공유하세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Preview Message Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📜 [공유 미리보기]",
                        style = MaterialTheme.typography.labelMedium,
                        color = JoseonRedPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shareText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SNS Grid Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // KakaoTalk
                SnsShareItem(
                    title = "카카오톡",
                    icon = Icons.Outlined.Chat,
                    bgColor = Color(0xFFFEE500),
                    iconColor = Color(0xFF3C1E1E),
                    onClick = {
                        shareToApp(context, "com.kakao.talk", shareText, "카카오톡")
                        onDismissRequest()
                    }
                )

                // Instagram
                SnsShareItem(
                    title = "인스타그램",
                    icon = Icons.Outlined.CameraAlt,
                    bgColor = Color(0xFFE1306C),
                    iconColor = Color.White,
                    onClick = {
                        shareToApp(context, "com.instagram.android", shareText, "인스타그램")
                        onDismissRequest()
                    }
                )

                // Copy Link / Text
                SnsShareItem(
                    title = "복사하기",
                    icon = Icons.Default.ContentCopy,
                    bgColor = JoseonIndigoTertiary,
                    iconColor = Color.White,
                    onClick = {
                        copyToClipboard(context, shareText)
                        onDismissRequest()
                    }
                )

                // Universal Share Sheet
                SnsShareItem(
                    title = "전체 공유",
                    icon = Icons.Default.Share,
                    bgColor = JoseonRedPrimary,
                    iconColor = Color.White,
                    onClick = {
                        shareUniversal(context, record.title, shareText)
                        onDismissRequest()
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SnsShareItem(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

private fun buildShareMessage(record: SillokRecord): String {
    val locationsText = if (record.locations.isNotEmpty()) {
        "📍 주요 관련 장소: ${record.locations.first().name} (${record.locations.first().address})"
    } else ""

    val tagsText = record.tags.joinToString(" ") { "#$it" }

    return """
        [조선왕조실록 역사 탐색]
        왕대: ${record.king} (${record.reignYear})
        사건: ${record.title}
        
        📖 요약: ${record.summary}
        
        $locationsText
        
        $tagsText #조선왕조실록 #역사탐색 #구글맵역사
    """.trimIndent()
}

private fun shareToApp(context: Context, packageName: String, text: String, appName: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage(packageName)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to Universal Share if specific app is not installed
        Toast.makeText(context, "$appName 앱이 설치되어 있지 않아 전체 공유로 전환합니다.", Toast.LENGTH_SHORT).show()
        shareUniversal(context, "조선왕조실록 공유", text)
    }
}

private fun shareUniversal(context: Context, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "조선왕조실록 SNS 연동 공유")
    context.startActivity(chooser)
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("조선왕조실록", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "실록 기사 및 장소 정보가 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
}
