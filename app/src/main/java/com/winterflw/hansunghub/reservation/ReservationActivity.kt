package com.winterflw.hansunghub.reservation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winterflw.hansunghub.reservation.model.FacilityType
import com.winterflw.hansunghub.ui.theme.HansunghubTheme

class ReservationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            HansunghubTheme {
                ReservationMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationMainScreen() {
    var selectedFacility by remember { mutableStateOf<FacilityType?>(null) }
    var showMyPage by remember { mutableStateOf(false) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                // 예약 탭
                NavigationBarItem(
                    selected = !showMyPage,
                    onClick = { 
                        showMyPage = false
                        selectedFacility = null  // 시설 선택 화면으로
                    },
                    icon = { Icon(Icons.Filled.EventNote, "예약") },
                    label = { Text("예약") }
                )
                
                // 마이페이지 탭
                NavigationBarItem(
                    selected = showMyPage,
                    onClick = { 
                        showMyPage = true
                        selectedFacility = null
                    },
                    icon = { Icon(Icons.Filled.Person, "마이페이지") },
                    label = { Text("마이페이지") }
                )
            }
        }
    ) { paddingValues ->
        when {
            showMyPage -> {
                // 마이페이지 화면
                MyPageScreen(modifier = Modifier.padding(paddingValues))
            }
            selectedFacility == null -> {
                // 시설 선택 화면
                ReservationScreen(
                    modifier = Modifier.padding(paddingValues),
                    onFacilitySelected = { facility ->
                        selectedFacility = facility
                    }
                )
            }
            else -> {
                // 예약 상세 화면
                ReservationDetailScreen(
                    facility = selectedFacility!!,
                    onBackClick = {
                        selectedFacility = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마이페이지", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "마이페이지",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "준비 중입니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

