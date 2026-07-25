package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.Place
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
import com.example.data.LocationPoint
import com.example.ui.theme.JoseonGoldSecondary
import com.example.ui.theme.JoseonIndigoTertiary
import com.example.ui.theme.JoseonRedPrimary

@Composable
fun GoogleMapViewCard(
    locations: List<LocationPoint>,
    eventTitle: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLocationIndex by remember { mutableIntStateOf(0) }
    val selectedLoc = locations.getOrNull(selectedLocationIndex) ?: locations.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(JoseonRedPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map Location",
                            tint = JoseonRedPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "주요 관련 장소 구글 맵 지도",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${locations.size}개 유적지 좌표 등록됨",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Launch All on Google Maps Button
                TextButton(
                    onClick = {
                        openGoogleMaps(context, selectedLoc?.name ?: eventTitle, selectedLoc?.latitude ?: 37.5796, selectedLoc?.longitude ?: 126.9770)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open Google Maps",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "구글 맵 열기",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = JoseonRedPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map Preview Canvas with Stylized Historical Korean Map Grid & Markers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8E3D5))
                    .border(1.dp, Color(0xFFC8BFAC), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Grid lines simulating map coordinates
                    val gridStep = 40f
                    for (x in 0..width.toInt() step gridStep.toInt()) {
                        drawLine(
                            color = Color(0x11000000),
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), height),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..height.toInt() step gridStep.toInt()) {
                        drawLine(
                            color = Color(0x11000000),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }

                    // Topo contour decorative lines
                    val path = Path().apply {
                        moveTo(0f, height * 0.7f)
                        quadraticTo(width * 0.3f, height * 0.4f, width * 0.6f, height * 0.8f)
                        quadraticTo(width * 0.8f, height * 0.9f, width, height * 0.65f)
                    }
                    drawPath(
                        path = path,
                        color = Color(0x228B1E1E),
                        style = Stroke(width = 3f)
                    )

                    // Draw pins for locations
                    locations.forEachIndexed { index, loc ->
                        // Calculate canvas position from lat/lng offset
                        val normalizedX = ((loc.longitude - 126.0) / 2.0).coerceIn(0.15, 0.85).toFloat() * width
                        val normalizedY = (1.0 - (loc.latitude - 35.0) / 4.0).coerceIn(0.15, 0.85).toFloat() * height

                        val isSelected = index == selectedLocationIndex
                        val pinColor = if (isSelected) JoseonRedPrimary else JoseonIndigoTertiary
                        val radius = if (isSelected) 14f else 9f

                        // Outer glow/ring
                        if (isSelected) {
                            drawCircle(
                                color = pinColor.copy(alpha = 0.3f),
                                radius = radius * 2.2f,
                                center = Offset(normalizedX, normalizedY)
                            )
                        }

                        // Pin Base Circle
                        drawCircle(
                            color = pinColor,
                            radius = radius,
                            center = Offset(normalizedX, normalizedY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = radius * 0.4f,
                            center = Offset(normalizedX, normalizedY)
                        )
                    }
                }

                // Map Overlay Info Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color(0xCC1A1A1A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Google Maps Geo-Coordinates Engine",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }

                // Map Controls Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        onClick = {
                            if (selectedLoc != null) {
                                openGoogleMapsNavigation(context, selectedLoc.latitude, selectedLoc.longitude, selectedLoc.name)
                            }
                        },
                        shape = CircleShape,
                        color = JoseonRedPrimary,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Get Directions",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location Selector Tabs if multiple locations exist
            if (locations.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    locations.forEachIndexed { index, loc ->
                        val isSelected = index == selectedLocationIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLocationIndex = index },
                            label = {
                                Text(
                                    text = loc.name.take(12),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JoseonRedPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Selected Location Details Card
            if (selectedLoc != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = JoseonRedPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = selectedLoc.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "📍 주소: ${selectedLoc.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "🌐 위도: ${selectedLoc.latitude}, 경도: ${selectedLoc.longitude}",
                            style = MaterialTheme.typography.labelSmall,
                            color = JoseonIndigoTertiary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = selectedLoc.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Actions for Selected Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    openGoogleMaps(
                                        context = context,
                                        query = "${selectedLoc.name} ${selectedLoc.address}",
                                        lat = selectedLoc.latitude,
                                        lng = selectedLoc.longitude
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JoseonRedPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "구글 맵에서 장소 확인",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openGoogleMaps(context: Context, query: String, lat: Double, lng: Double) {
    try {
        val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=" + Uri.encode(query))
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback to Browser Google Maps
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode("$query ($lat,$lng)"))
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "구글 맵 연결 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
    }
}

private fun openGoogleMapsNavigation(context: Context, lat: Double, lng: Double, destinationName: String) {
    try {
        val navUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
        val navIntent = Intent(Intent.ACTION_VIEW, navUri)
        navIntent.setPackage("com.google.android.apps.maps")

        if (navIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(navIntent)
        } else {
            openGoogleMaps(context, destinationName, lat, lng)
        }
    } catch (e: Exception) {
        openGoogleMaps(context, destinationName, lat, lng)
    }
}
