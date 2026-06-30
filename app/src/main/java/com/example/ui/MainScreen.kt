package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Issue
import com.example.data.User
import java.io.File
import kotlin.math.sqrt

// --- Color Constants for Categories ---
val ColorPothole = Color(0xFFE23E3E)
val ColorWaterLeak = Color(0xFF3B8BD4)
val ColorStreetlight = Color(0xFFF2A623)
val ColorWaste = Color(0xFF2AC670)
val ColorDrainage = Color(0xFF9B59B6)
val ColorOther = Color(0xFF999999)

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "pothole" -> ColorPothole
        "water_leak" -> ColorWaterLeak
        "streetlight" -> ColorStreetlight
        "waste" -> ColorWaste
        "drainage" -> ColorDrainage
        else -> ColorOther
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "pothole" -> Icons.Default.Warning
        "water_leak" -> Icons.Default.WaterDrop
        "streetlight" -> Icons.Default.Lightbulb
        "waste" -> Icons.Default.Delete
        "drainage" -> Icons.Default.Opacity
        else -> Icons.Default.Info
    }
}

// Map Bounds (Jaipur Area)
const val MIN_LAT = 26.85
const val MAX_LAT = 26.95
const val MIN_LNG = 75.72
const val MAX_LNG = 75.85

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isReporting by viewModel.isReporting.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val submissionResult by viewModel.submissionResult.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            if (!isReporting) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == Tab.MAP,
                        onClick = { viewModel.setCurrentTab(Tab.MAP) },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                        label = { Text("Civic Map") },
                        modifier = Modifier.testTag("tab_map")
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.REPORTS,
                        onClick = { viewModel.setCurrentTab(Tab.REPORTS) },
                        icon = { Icon(Icons.Default.List, contentDescription = "Issues List") },
                        label = { Text("Reports") },
                        modifier = Modifier.testTag("tab_reports")
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.DASHBOARD,
                        onClick = { viewModel.setCurrentTab(Tab.DASHBOARD) },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Dashboard") },
                        label = { Text("Impact") },
                        modifier = Modifier.testTag("tab_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.LEADERBOARD,
                        onClick = { viewModel.setCurrentTab(Tab.LEADERBOARD) },
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                        label = { Text("Leaderboard") },
                        modifier = Modifier.testTag("tab_leaderboard")
                    )
                    NavigationBarItem(
                        selected = currentTab == Tab.PROFILE,
                        onClick = { viewModel.setCurrentTab(Tab.PROFILE) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main content screens based on tab selection
            when (currentTab) {
                Tab.MAP -> MapTabScreen(viewModel)
                Tab.REPORTS -> ReportsListTabScreen(viewModel)
                Tab.DASHBOARD -> DashboardTabScreen(viewModel)
                Tab.LEADERBOARD -> LeaderboardTabScreen(viewModel)
                Tab.PROFILE -> ProfileTabScreen(viewModel)
            }

            // Reporting Form overlays the entire layout
            AnimatedVisibility(
                visible = isReporting,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                ReportIssueScreen(viewModel)
            }

            // Submission Success/Error Dialog
            submissionResult?.let { result ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearSubmissionResult() },
                    title = {
                        Text(
                            text = if (result.startsWith("Success")) "AI Triage Completed!" else "Triage Request Failed",
                            fontWeight = FontWeight.Bold,
                            color = if (result.startsWith("Success")) ColorWaste else ColorPothole
                        )
                    },
                    text = {
                        Column {
                            Icon(
                                imageVector = if (result.startsWith("Success")) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.startsWith("Success")) ColorWaste else ColorPothole,
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 12.dp)
                            )
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Points awarded: +10 XP\nBadge progress updated!",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.clearSubmissionResult() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (result.startsWith("Success")) ColorWaste else ColorPothole
                            )
                        ) {
                            Text("Awesome", color = Color.White)
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // Loading overlay for submission
            if (isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .pointerInput(Unit) {}, // Consume taps
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "AI Triage in Progress...",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Gemini Vision is analyzing damage\ncategorization, severity, & location...",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MAP TAB SCREEN
// ==========================================
@Composable
fun MapTabScreen(viewModel: MainViewModel) {
    val filteredIssues by viewModel.filteredIssues.collectAsStateWithLifecycle()
    val selectedIssue by viewModel.selectedIssue.collectAsStateWithLifecycle()
    val activeCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    var mapScale by remember { mutableStateOf(1.0f) }
    var mapOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    val context = LocalContext.current

    // Launch standard Camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.startReporting(bitmap)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Custom Visual Jaipur Map
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        mapScale = (mapScale * zoom).coerceIn(0.5f, 5.0f)
                        mapOffset = mapOffset + pan
                    }
                }
                .pointerInput(filteredIssues) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            // Convert local tap offset on Map to Lat/Lng
                            val width = size.width
                            val height = size.height
                            
                            // Revert offset and scale transforms
                            val absoluteX = (offset.x - mapOffset.x) / mapScale
                            val absoluteY = (offset.y - mapOffset.y) / mapScale

                            val fracX = (absoluteX / width).coerceIn(0f, 1f)
                            val fracY = (absoluteY / height).coerceIn(0f, 1f)

                            val tappedLng = MIN_LNG + fracX * (MAX_LNG - MIN_LNG)
                            val tappedLat = MAX_LAT - fracY * (MAX_LAT - MIN_LAT)

                            // Launch mockup image reporter for double tap!
                            val bitmap = createMockBitmap("pothole")
                            viewModel.startReporting(bitmap)
                            // Set coordinate in form
                            viewModel.submitReport(tappedLat, tappedLng)
                        },
                        onTap = { offset ->
                            val width = size.width
                            val height = size.height

                            var foundIssue: Issue? = null
                            var minDistance = Double.MAX_VALUE

                            // Find tapped pin
                            filteredIssues.forEach { issue ->
                                val fracX = (issue.longitude - MIN_LNG) / (MAX_LNG - MIN_LNG)
                                val fracY = 1.0 - (issue.latitude - MIN_LAT) / (MAX_LAT - MIN_LAT)

                                val pinX = (fracX * width).toFloat() * mapScale + mapOffset.x
                                val pinY = (fracY * height).toFloat() * mapScale + mapOffset.y

                                val dist = sqrt((offset.x - pinX) * (offset.x - pinX) + (offset.y - pinY) * (offset.y - pinY)).toDouble()
                                if (dist < 48 * mapScale && dist < minDistance) {
                                    minDistance = dist
                                    foundIssue = issue
                                }
                            }
                            viewModel.setSelectedIssue(foundIssue)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw Cyberpunk/Tech styled Map Grid Background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                )

                // Grid lines for high-tech civic aesthetic
                val gridSize = 80f * mapScale
                val startX = mapOffset.x % gridSize
                val startY = mapOffset.y % gridSize
                
                var x = startX
                while (x < canvasWidth) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f
                    )
                    x += gridSize
                }
                var y = startY
                while (y < canvasHeight) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                    y += gridSize
                }

                // --- Draw Jaipur Major Roads & Landmark Labels ---
                // Draw Highway / Main Bypass Line (West to East)
                val roadColor = Color(0xFF334155)
                val roadWidth = 8f * mapScale

                // MI Road
                val miRoadY1 = (0.35f * canvasHeight) * mapScale + mapOffset.y
                drawLine(roadColor, Offset(0f, miRoadY1), Offset(canvasWidth, miRoadY1 + 100f * mapScale), roadWidth)

                // Tonk Road
                val tonkRoadX = (0.6f * canvasWidth) * mapScale + mapOffset.x
                drawLine(roadColor, Offset(tonkRoadX, 0f), Offset(tonkRoadX - 100f * mapScale, canvasHeight), roadWidth)

                // Bypass Ring
                val bypassX1 = (0.2f * canvasWidth) * mapScale + mapOffset.x
                val bypassX2 = (0.8f * canvasWidth) * mapScale + mapOffset.x
                drawLine(roadColor, Offset(bypassX1, 0f), Offset(bypassX2, canvasHeight), roadWidth / 2)

                // Landmark representation circles
                val landmarks = listOf(
                    Triple("Hawa Mahal", 26.9239, 75.8267),
                    Triple("Malviya Nagar", 26.9124, 75.7873),
                    Triple("C-Scheme", 26.9210, 75.8070),
                    Triple("Jaipur Station", 26.9124, 75.8263),
                    Triple("Ram Nagar", 26.8975, 75.7654),
                    Triple("Mansarovar", 26.8732, 75.7612),
                    Triple("Albert Hall", 26.9118, 75.8193)
                )

                landmarks.forEach { (name, lat, lng) ->
                    val fracX = (lng - MIN_LNG) / (MAX_LNG - MIN_LNG)
                    val fracY = 1.0 - (lat - MIN_LAT) / (MAX_LAT - MIN_LAT)

                    val xPos = (fracX * canvasWidth).toFloat() * mapScale + mapOffset.x
                    val yPos = (fracY * canvasHeight).toFloat() * mapScale + mapOffset.y

                    // Draw subtle pulse circle for landmark
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = 24f * mapScale,
                        center = Offset(xPos, yPos)
                    )
                    drawCircle(
                        color = Color.Cyan.copy(alpha = 0.3f),
                        radius = 6f * mapScale,
                        center = Offset(xPos, yPos)
                    )
                }

                // --- Draw Pins for Civic Issues ---
                filteredIssues.forEach { issue ->
                    val fracX = (issue.longitude - MIN_LNG) / (MAX_LNG - MIN_LNG)
                    val fracY = 1.0 - (issue.latitude - MIN_LAT) / (MAX_LAT - MIN_LAT)

                    val pinX = (fracX * canvasWidth).toFloat() * mapScale + mapOffset.x
                    val pinY = (fracY * canvasHeight).toFloat() * mapScale + mapOffset.y

                    val pinColor = getCategoryColor(issue.category)

                    // Resizing/darkening based on upvote count (US-4: upvotes darkens/enlarges pin)
                    val voteMultiplier = (1.0f + (issue.votes / 50.0f)).coerceIn(1.0f, 2.5f)
                    val baseRadius = 14f * mapScale * voteMultiplier
                    val isSelected = selectedIssue?.id == issue.id

                    // Outer shadow glow
                    drawCircle(
                        color = pinColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
                        radius = baseRadius + (8f * mapScale),
                        center = Offset(pinX, pinY)
                    )

                    // Main Pin Center
                    drawCircle(
                        color = pinColor,
                        radius = baseRadius,
                        center = Offset(pinX, pinY)
                    )

                    // Inner core marker
                    drawCircle(
                        color = Color.White,
                        radius = baseRadius * 0.4f,
                        center = Offset(pinX, pinY)
                    )
                }
            }
        }

        // 2. Interactive Map Filters Row at Top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterBadge(
                        text = "All",
                        selected = activeCategoryFilter == null,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setCategoryFilter(null) }
                    )
                    FilterBadge(
                        text = "Potholes",
                        selected = activeCategoryFilter == "pothole",
                        color = ColorPothole,
                        onClick = { viewModel.setCategoryFilter("pothole") }
                    )
                    FilterBadge(
                        text = "Water Leaks",
                        selected = activeCategoryFilter == "water_leak",
                        color = ColorWaterLeak,
                        onClick = { viewModel.setCategoryFilter("water_leak") }
                    )
                    FilterBadge(
                        text = "Lights",
                        selected = activeCategoryFilter == "streetlight",
                        color = ColorStreetlight,
                        onClick = { viewModel.setCategoryFilter("streetlight") }
                    )
                    FilterBadge(
                        text = "Waste",
                        selected = activeCategoryFilter == "waste",
                        color = ColorWaste,
                        onClick = { viewModel.setCategoryFilter("waste") }
                    )
                    FilterBadge(
                        text = "Drainage",
                        selected = activeCategoryFilter == "drainage",
                        color = ColorDrainage,
                        onClick = { viewModel.setCategoryFilter("drainage") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Double-tap anywhere on map to instantly simulate & report an issue there!",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        // Helper instruction panel at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = selectedIssue?.let { 200.dp } ?: 100.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Drag to Pan • Pinch to Zoom",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 3. Floating Action Button for Submitting New Report
        var showCameraOptions by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = showCameraOptions,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        // Real Camera trigger
                        ExtendedFloatingActionButton(
                            onClick = {
                                showCameraOptions = false
                                cameraLauncher.launch()
                            },
                            icon = { Icon(Icons.Default.PhotoCamera, "Camera") },
                            text = { Text("Real Camera") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("fab_real_camera")
                        )

                        // Mock Camera Simulator trigger (for offline/emulator demo)
                        ExtendedFloatingActionButton(
                            onClick = {
                                showCameraOptions = false
                                // Open mock chooser directly
                                val mockBitmap = createMockBitmap("pothole")
                                viewModel.startReporting(mockBitmap)
                            },
                            icon = { Icon(Icons.Default.Science, "Demo Simulator") },
                            text = { Text("Mock Simulator") },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.testTag("fab_mock_camera")
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showCameraOptions = !showCameraOptions },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_report_issue")
                ) {
                    Icon(
                        imageVector = if (showCameraOptions) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Report Issue",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // 4. Overlap Issue Details Card if Selected
        AnimatedVisibility(
            visible = selectedIssue != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            selectedIssue?.let { issue ->
                IssueDetailCard(
                    issue = issue,
                    onClose = { viewModel.setSelectedIssue(null) },
                    onVote = { viewModel.toggleVote(issue.id) },
                    onStatusChange = { newStatus -> viewModel.updateIssueStatus(issue.id, newStatus) }
                )
            }
        }
    }
}

@Composable
fun FilterBadge(text: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) color else color.copy(alpha = 0.15f),
        contentColor = if (selected) Color.White else color,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// ==========================================
// ISSUE DETAILS CARD / SHEET
// ==========================================
@Composable
fun IssueDetailCard(
    issue: Issue,
    onClose: () -> Unit,
    onVote: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("issue_detail_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Surface(
                    color = getCategoryColor(issue.category).copy(alpha = 0.15f),
                    contentColor = getCategoryColor(issue.category),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            getCategoryIcon(issue.category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = issue.category.replace("_", " ").uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Close Button
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close details")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Side: Image
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (issue.photoPath?.startsWith("sample:") == true) {
                        // Dynamically render programmatic placeholder since files don't exist
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getCategoryColor(issue.category).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(issue.category),
                                contentDescription = null,
                                tint = getCategoryColor(issue.category),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    } else if (issue.photoPath != null) {
                        // User captured image
                        AsyncImage(
                            model = File(issue.photoPath),
                            contentDescription = "Issue Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // No photo fallback
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                // Right Side: Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Severity: ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Severity Stars representation
                        Row {
                            for (i in 1..5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= issue.severity) ColorStreetlight else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = issue.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Location coordinate summary
                    Text(
                        text = "Lat: ${String.format("%.4f", issue.latitude)}, Lng: ${String.format("%.4f", issue.longitude)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Bar (Upvote, Status Changer, Reporter Tag)
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reporter info
                Column {
                    Text(
                        text = "Reported by",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = issue.reporterName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Status Toggler (Simulates Municipal action / Phase 2 status checks!)
                var expandedStatus by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        onClick = { expandedStatus = true },
                        shape = RoundedCornerShape(8.dp),
                        color = when (issue.status) {
                            "reported" -> ColorPothole.copy(alpha = 0.15f)
                            "in_progress" -> ColorStreetlight.copy(alpha = 0.15f)
                            "fixed" -> ColorWaste.copy(alpha = 0.15f)
                            else -> ColorOther.copy(alpha = 0.15f)
                        },
                        contentColor = when (issue.status) {
                            "reported" -> ColorPothole
                            "in_progress" -> ColorStreetlight
                            "fixed" -> ColorWaste
                            else -> ColorOther
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = issue.status.replace("_", " ").uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("REPORTED") },
                            onClick = {
                                onStatusChange("reported")
                                expandedStatus = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("IN PROGRESS") },
                            onClick = {
                                onStatusChange("in_progress")
                                expandedStatus = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("FIXED") },
                            onClick = {
                                onStatusChange("fixed")
                                expandedStatus = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("DISMISSED") },
                            onClick = {
                                onStatusChange("dismissed")
                                expandedStatus = false
                            }
                        )
                    }
                }

                // Upvote Trigger
                Button(
                    onClick = onVote,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (issue.hasVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (issue.hasVoted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (issue.hasVoted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Upvote",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${issue.votes}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// REPORT ISSUE SHEET / SCREEN (US-1 & US-2)
// ==========================================
@Composable
fun ReportIssueScreen(viewModel: MainViewModel) {
    val capturedBitmap by viewModel.capturedBitmap.collectAsStateWithLifecycle()
    val userDescription by viewModel.userDescription.collectAsStateWithLifecycle()

    var selectedLat by remember { mutableStateOf(26.9124) }
    var selectedLng by remember { mutableStateOf(75.7873) }

    // Mock images list for Simulator
    val mockOptions = listOf(
        Pair("pothole", "Jaipur Market Pothole"),
        Pair("water_leak", "C-Scheme Pipe Burst"),
        Pair("streetlight", "Railway Station Darkness"),
        Pair("waste", "Ram Nagar Garbage Dump"),
        Pair("drainage", "Sewer Overflow Mansarovar")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Report Civic Issue",
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { viewModel.cancelReporting() }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel report")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Left photo preview / Simulator Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                capturedBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No photo loaded.")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulator category photo selectors (Incredibly handy for emulator testers!)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Demo Camera Simulator:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mockOptions.forEach { (cat, label) ->
                        Surface(
                            onClick = {
                                val bitmap = createMockBitmap(cat)
                                viewModel.startReporting(bitmap)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = getCategoryColor(cat).copy(alpha = 0.15f),
                            contentColor = getCategoryColor(cat),
                            border = BorderStroke(1.dp, getCategoryColor(cat).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Geolocation display and mock slider adjustment
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            "Geolocation Auto-filled:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Reset to Jaipur center
                    TextButton(onClick = {
                        selectedLat = 26.9124
                        selectedLng = 75.7873
                    }) {
                        Text("Reset Center", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Lat: ${String.format("%.4f", selectedLat)}, Lng: ${String.format("%.4f", selectedLng)} (Jaipur Sector)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Fine-tune reporting coordinates:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Sliders to simulate movement on map
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("LAT: ", fontSize = 10.sp, modifier = Modifier.width(32.dp))
                    Slider(
                        value = selectedLat.toFloat(),
                        onValueChange = { selectedLat = it.toDouble() },
                        valueRange = MIN_LAT.toFloat()..MAX_LAT.toFloat(),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("LNG: ", fontSize = 10.sp, modifier = Modifier.width(32.dp))
                    Slider(
                        value = selectedLng.toFloat(),
                        onValueChange = { selectedLng = it.toDouble() },
                        valueRange = MIN_LNG.toFloat()..MAX_LNG.toFloat(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optional custom description
        OutlinedTextField(
            value = userDescription,
            onValueChange = { viewModel.updateUserDescription(it) },
            label = { Text("What's wrong? (Optional custom description)") },
            placeholder = { Text("e.g. leaking faucet, pothole causing accidents near temple...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("report_description_input"),
            shape = RoundedCornerShape(16.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button (Triggers Gemini Analysis!)
        Button(
            onClick = { viewModel.submitReport(selectedLat, selectedLng) },
            enabled = capturedBitmap != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_report_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Text(
                    "RUN AI TRIAGE & SUBMIT",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// ==========================================
// REPORTS LIST TAB SCREEN
// ==========================================
@Composable
fun ReportsListTabScreen(viewModel: MainViewModel) {
    val filteredIssues by viewModel.filteredIssues.collectAsStateWithLifecycle()
    val activeCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val activeStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Seeded & Reported Issues",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Manage reported public damage or view local progress",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Inline filters inside Reports List
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Category Filter:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterBadge("All", activeCategoryFilter == null, MaterialTheme.colorScheme.primary) { viewModel.setCategoryFilter(null) }
                    FilterBadge("Potholes", activeCategoryFilter == "pothole", ColorPothole) { viewModel.setCategoryFilter("pothole") }
                    FilterBadge("Water Leaks", activeCategoryFilter == "water_leak", ColorWaterLeak) { viewModel.setCategoryFilter("water_leak") }
                    FilterBadge("Lights", activeCategoryFilter == "streetlight", ColorStreetlight) { viewModel.setCategoryFilter("streetlight") }
                    FilterBadge("Waste", activeCategoryFilter == "waste", ColorWaste) { viewModel.setCategoryFilter("waste") }
                    FilterBadge("Drainage", activeCategoryFilter == "drainage", ColorDrainage) { viewModel.setCategoryFilter("drainage") }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Status Filter:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterBadge("All", activeStatusFilter == null, MaterialTheme.colorScheme.primary) { viewModel.setStatusFilter(null) }
                    FilterBadge("Reported", activeStatusFilter == "reported", ColorPothole) { viewModel.setStatusFilter("reported") }
                    FilterBadge("In Progress", activeStatusFilter == "in_progress", ColorStreetlight) { viewModel.setStatusFilter("in_progress") }
                    FilterBadge("Fixed", activeStatusFilter == "fixed", ColorWaste) { viewModel.setStatusFilter("fixed") }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Issues List
        if (filteredIssues.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No reports match your selected filters.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredIssues) { issue ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setSelectedIssue(issue)
                                viewModel.setCurrentTab(Tab.MAP)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Icon Circle
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(getCategoryColor(issue.category).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(issue.category),
                                    contentDescription = null,
                                    tint = getCategoryColor(issue.category)
                                )
                            }

                            // Text details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = issue.category.replace("_", " ").uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = getCategoryColor(issue.category)
                                    )
                                    // Status tag
                                    Text(
                                        text = issue.status.replace("_", " ").uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = when (issue.status) {
                                            "reported" -> ColorPothole
                                            "in_progress" -> ColorStreetlight
                                            "fixed" -> ColorWaste
                                            else -> ColorOther
                                        }
                                    )
                                }
                                Text(
                                    text = issue.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Votes: ${issue.votes}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Severity: ${issue.severity}/5",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// IMPACT DASHBOARD SCREEN (US-5)
// ==========================================
@Composable
fun DashboardTabScreen(viewModel: MainViewModel) {
    val allIssues by viewModel.allIssues.collectAsStateWithLifecycle()

    val totalCount = allIssues.size
    val activeCount = allIssues.count { it.status == "reported" || it.status == "in_progress" }
    val resolvedCount = allIssues.count { it.status == "fixed" }
    val resolutionRate = if (totalCount > 0) (resolvedCount.toFloat() / totalCount * 100).toInt() else 0

    // Groups
    val categoryCounts = allIssues.groupBy { it.category }.mapValues { it.value.size }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "Impact Dashboard",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Real-time analytics of community improvements in Jaipur",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Aggregate Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Reported",
                    value = "$totalCount",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active / Open",
                    value = "$activeCount",
                    color = ColorStreetlight,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Resolved Issues",
                    value = "$resolvedCount",
                    color = ColorWaste,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Circular Resolve rate meter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .drawBehind {
                                // Draw circular resolve ring
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = size.width / 2
                                )
                                drawArc(
                                    color = ColorWaste,
                                    startAngle = -90f,
                                    sweepAngle = (360f * (resolutionRate / 100f)),
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16f)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$resolutionRate%",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = ColorWaste
                        )
                    }

                    Column {
                        Text(
                            "Civic Cleanup Efficiency",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Representing the portion of infrastructure damage successfully fixed by Jaipur municipal authorities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Issues by Category Visual bar list (Reacts dynamically to DB!)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Reports by Infrastructure Category",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val categories = listOf("pothole", "water_leak", "streetlight", "waste", "drainage", "other")
                    categories.forEach { cat ->
                        val count = categoryCounts[cat] ?: 0
                        val percentage = if (totalCount > 0) count.toFloat() / totalCount else 0.0f
                        val label = cat.replace("_", " ").uppercase()

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(getCategoryIcon(cat), null, tint = getCategoryColor(cat), modifier = Modifier.size(14.dp))
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Text("$count reports", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Styled loading bar representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percentage)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(getCategoryColor(cat))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// GAMIFIED LEADERBOARD SCREEN (US-6)
// ==========================================
@Composable
fun LeaderboardTabScreen(viewModel: MainViewModel) {
    val topUsers by viewModel.topUsers.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Vigilante Leaderboard",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Recognizing Jaipur's top active civic action reporters",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(topUsers.sortedByDescending { it.points }) { user ->
                val isMe = user.id == "user_1"
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = if (isMe) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rank Indicator
                        val index = topUsers.indexOfFirst { it.id == user.id } + 1
                        val rankColor = when (index) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (index <= 3) rankColor.copy(alpha = 0.2f) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$index",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = rankColor
                            )
                        }

                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // User Info details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "YOU",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.badges,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Point tally
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${user.points} XP",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${user.issuesReported} reports",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// USER PROFILE SCREEN (US-6)
// ==========================================
@Composable
fun ProfileTabScreen(viewModel: MainViewModel) {
    val localUser by viewModel.localUser.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Citizen Profile",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Big Profile Avatar Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = localUser?.name ?: "Guest User",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("POINTS TALLY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${localUser?.points ?: 0} XP", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.height(40.dp).width(1.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ISSUES DETECTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${localUser?.issuesReported ?: 0}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = ColorStreetlight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Unlocked Badges",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Text(
            "Active reporter rewards & achievements",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Grid listing of dynamic badges!
        val userBadges = localUser?.badges?.split(", ")?.map { it.trim() } ?: emptyList()
        val allAvailableBadges = listOf(
            Pair("First Responder", "Awarded for reporting your first hyperlocal civic issue."),
            Pair("Civic Guardian", "Report 3 or more approved infrastructure reports."),
            Pair("Pothole Hunter", "Detect 5+ pothole segments dynamically."),
            Pair("Elite Vigilante", "Surpass 100+ civic experience points.")
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(allAvailableBadges) { (badgeTitle, badgeDesc) ->
                val isUnlocked = userBadges.contains(badgeTitle)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                    ),
                    border = if (isUnlocked) BorderStroke(1.dp, MaterialTheme.colorScheme.secondary) else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isUnlocked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.05f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.MilitaryTech else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badgeTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = badgeDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper to draw clean programmatic JPEG representations for the simulator
private fun createMockBitmap(category: String): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint()

    // Fill slate gray background
    paint.color = android.graphics.Color.rgb(40, 44, 52)
    canvas.drawRect(0f, 0f, 400f, 400f, paint)

    // Decorative grid lines
    paint.color = android.graphics.Color.rgb(60, 64, 72)
    paint.strokeWidth = 2f
    for (i in 0..4) {
        val spacing = i * 100f
        canvas.drawLine(spacing, 0f, spacing, 400f, paint)
        canvas.drawLine(0f, spacing, 400f, spacing, paint)
    }

    // Draw Category elements to let Gemini Vision do computer vision analysis!
    when (category.lowercase()) {
        "pothole" -> {
            // Draw pothole crater on asphalt
            paint.color = android.graphics.Color.rgb(20, 20, 22)
            canvas.drawCircle(200f, 200f, 110f, paint)
            // inner cracked crater
            paint.color = android.graphics.Color.rgb(65, 30, 30) // dirt/rock
            canvas.drawCircle(200f, 200f, 90f, paint)
            paint.color = android.graphics.Color.RED
            paint.textSize = 28f
            paint.isFakeBoldText = true
            canvas.drawText("ROAD DAMAGE CRATER", 50f, 210f, paint)
        }
        "water_leak" -> {
            // Draw spraying pipeline burst
            paint.color = android.graphics.Color.rgb(100, 110, 120) // grey pipe
            canvas.drawRect(50f, 180f, 350f, 220f, paint)
            // burst point spray
            paint.color = android.graphics.Color.rgb(0, 191, 255) // skyblue spray water
            canvas.drawCircle(200f, 200f, 60f, paint)
            canvas.drawCircle(170f, 160f, 40f, paint)
            canvas.drawCircle(230f, 150f, 30f, paint)
            paint.color = android.graphics.Color.BLUE
            paint.textSize = 28f
            paint.isFakeBoldText = true
            canvas.drawText("HIGH PRESSURE LEAK", 50f, 260f, paint)
        }
        "streetlight" -> {
            // Draw dark street and single pole
            paint.color = android.graphics.Color.rgb(10, 10, 15)
            canvas.drawRect(0f, 0f, 400f, 400f, paint)
            // Pole
            paint.color = android.graphics.Color.rgb(150, 150, 150)
            canvas.drawRect(190f, 80f, 210f, 400f, paint)
            // broken light bulb (gray / cracked)
            paint.color = android.graphics.Color.rgb(70, 70, 70)
            canvas.drawCircle(200f, 80f, 30f, paint)
            paint.color = android.graphics.Color.YELLOW
            paint.textSize = 28f
            paint.isFakeBoldText = true
            canvas.drawText("DARK STREETLIGHT", 70f, 250f, paint)
        }
        "waste" -> {
            // Draw heaps of garbage piles
            paint.color = android.graphics.Color.rgb(139, 69, 19) // brown pile
            canvas.drawCircle(200f, 280f, 100f, paint)
            paint.color = android.graphics.Color.rgb(34, 139, 34) // green mold/litter
            canvas.drawCircle(150f, 300f, 60f, paint)
            canvas.drawCircle(250f, 290f, 70f, paint)
            paint.color = android.graphics.Color.GREEN
            paint.textSize = 28f
            paint.isFakeBoldText = true
            canvas.drawText("UNCOLLECTED TRASH", 60f, 150f, paint)
        }
        "drainage" -> {
            // Draw sewer hole with overflowing dark green fluid
            paint.color = android.graphics.Color.BLACK
            canvas.drawCircle(200f, 200f, 80f, paint)
            paint.color = android.graphics.Color.rgb(46, 139, 87) // sewer green water
            canvas.drawCircle(200f, 200f, 70f, paint)
            canvas.drawCircle(180f, 220f, 50f, paint)
            canvas.drawCircle(230f, 180f, 40f, paint)
            paint.color = android.graphics.Color.rgb(153, 50, 204)
            paint.textSize = 28f
            paint.isFakeBoldText = true
            canvas.drawText("SEWER FLUID FLOOD", 60f, 210f, paint)
        }
        else -> {
            paint.color = android.graphics.Color.GRAY
            canvas.drawCircle(200f, 200f, 50f, paint)
        }
    }

    return bitmap
}
