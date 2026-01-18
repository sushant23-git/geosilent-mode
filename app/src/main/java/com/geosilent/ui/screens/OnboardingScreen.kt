package com.geosilent.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geosilent.ui.theme.*
import com.geosilent.utils.PreferencesManager
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    preferencesManager: PreferencesManager = hiltViewModel<OnboardingViewModel>().preferencesManager
) {
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.LocationOn,
            title = "Welcome to Geo Silent Mode",
            description = "Automate your phone's behavior based on your location. Your phone will automatically switch to silent mode when you arrive at specific places."
        ),
        OnboardingPage(
            icon = Icons.Default.Settings,
            title = "How It Works",
            description = "Create zones on the map, and when you enter them, your phone will automatically perform actions like switching to silent mode, sending SMS, or enabling Do Not Disturb."
        ),
        OnboardingPage(
            icon = Icons.Default.Security,
            title = "Permissions Needed",
            description = "Location access is required to detect when you enter zones. We only check your location when crossing zone boundaries - not continuously."
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, SurfaceDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }
            
            // Page indicators
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Primary else TextSecondary.copy(alpha = 0.5f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                preferencesManager.setOnboardingCompleted(true)
                                onComplete()
                            }
                        }
                    ) {
                        Text("Skip", color = TextSecondary)
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Next")
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                preferencesManager.setOnboardingCompleted(true)
                                onComplete()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Get Started", modifier = Modifier.padding(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Primary, Secondary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = OnPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = OnBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
