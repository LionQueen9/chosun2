package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultSillokData
import com.example.data.LocationPoint
import com.example.data.SillokRecord
import com.example.ui.theme.JoseonGoldSecondary
import com.example.ui.theme.JoseonIndigoTertiary
import com.example.ui.theme.JoseonRedPrimary

@Composable
fun MapExplorerView(
    onRecordSelect: (SillokRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allRecords = DefaultSillokData.sampleRecords
    val allLocationsWithRecord = remember {
        allRecords.flatMap { record ->
            record.locations.map { loc -> Pair(loc, record) }
        }
    }

    var selectedPair by remember { mutableStateOf(allLocationsWithRecord.firstOrNull()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(JoseonRedPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "조선왕조실록 구글 맵 지도 탐색기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "실록에 등장하는 한반도 주요 역사 유적지 위치 지도",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Interactive Canvas Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE5DFC9))
                .border(1.5.dp, Color(0xFFC0B293), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw map grid
                for (i in 0..10) {
                    val x = width * (i / 10f)
                    drawLine(Color(0x15000000), Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
                    val y = height * (i / 10f)
                    drawLine(Color(0x15000000), Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                }

                // Decorative coastline path simulation
                val coastPath = Path().apply {
                    moveTo(width * 0.15f, 0f)
                    quadraticTo(width * 0.3f, height * 0.4f, width * 0.2f, height * 0.8f)
                    quadraticTo(width * 0.4f, height, width * 0.7f, height * 0.85f)
                    quadraticTo(width * 0.9f, height * 0.5f, width * 0.85f, 0f)
                }
                drawPath(coastPath, Color(0x188B1E1E), style = Stroke(width = 3f))

                // Draw location pins
                allLocationsWithRecord.forEach { (loc, record) ->
                    val normX = ((loc.longitude - 126.0) / 2.5).coerceIn(0.1, 0.9).toFloat() * width
                    val normY = (1.0 - (loc.latitude - 34.0) / 6.0).coerceIn(0.1, 0.9).toFloat() * height

                    val isSelected = selectedPair?.first?.name == loc.name
                    val color = if (isSelected) JoseonRedPrimary else JoseonIndigoTertiary
                    val radius = if (isSelected) 16f else 10f

                    if (isSelected) {
                        drawCircle(color.copy(alpha = 0.35f), radius * 2.2f, Offset(normX, normY))
                    }
                    drawCircle(color, radius, Offset(normX, normY))
                    drawCircle(Color.White, radius * 0.35f, Offset(normX, normY))
                }
            }

            // Map Overlay Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(Color(0xDD000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Google Maps Geo Engine",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Location Card Preview
        selectedPair?.let { (loc, rec) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = JoseonRedPrimary)
                            Text(text = loc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Surface(color = JoseonGoldSecondary, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = rec.king,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "📍 ${loc.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = loc.description, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                openGoogleMapsApp(context, loc.name, loc.address, loc.latitude, loc.longitude)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = JoseonRedPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("구글 맵 연결", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onRecordSelect(rec) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("실록 기사 보기", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "전체 역사 장소 목록 (${allLocationsWithRecord.size}개)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // All Locations List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allLocationsWithRecord) { pair ->
                val (loc, rec) = pair
                val isSelected = selectedPair?.first?.name == loc.name

                Surface(
                    onClick = { selectedPair = pair },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) JoseonRedPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = loc.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = loc.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                            onClick = {
                                openGoogleMapsApp(context, loc.name, loc.address, loc.latitude, loc.longitude)
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = "Open Map", tint = JoseonRedPrimary)
                        }
                    }
                }
            }
        }
    }
}

private fun openGoogleMapsApp(context: Context, name: String, address: String, lat: Double, lng: Double) {
    try {
        val query = "$name $address"
        val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=" + Uri.encode(query))
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "구글 맵 연결 실패", Toast.LENGTH_SHORT).show()
    }
}
