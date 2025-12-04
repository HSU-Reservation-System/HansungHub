package com.winterflw.hansunghub.reservation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
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
    var selectedFacility by remember { mutableStateOf<FacilityType?>(null) }               // 예약 화면용
    var selectedFacilityForDetail by remember { mutableStateOf<FacilityType?>(null) }      // 상세 페이지용
    var showMyPage by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = !showMyPage,
                    onClick = {
                        showMyPage = false
                        selectedFacility = null
                        selectedFacilityForDetail = null
                    },
                    icon = { Icon(Icons.Filled.EventNote, "예약") },
                    label = { Text("예약") }
                )
                NavigationBarItem(
                    selected = showMyPage,
                    onClick = {
                        showMyPage = true
                        selectedFacility = null
                        selectedFacilityForDetail = null
                    },
                    icon = { Icon(Icons.Filled.Person, "마이페이지") },
                    label = { Text("마이페이지") }
                )
            }
        }
    ) { paddingValues ->

        when {
            // 마이페이지
            showMyPage -> MyPageScreen(
                modifier = Modifier.padding(paddingValues)
            )

            // 상세 페이지
            selectedFacilityForDetail != null -> ReservationDetailPreviewScreen(
                facility = selectedFacilityForDetail!!,
                onBackClick = {      // 상세 -> 카드
                    selectedFacilityForDetail = null
                },
                onReserveClick = { f ->    // 상세 -> 예약
                    selectedFacility = f
                    selectedFacilityForDetail = null
                },
                modifier = Modifier.padding(paddingValues)
            )

            // 카드 페이지
            selectedFacility == null -> ReservationScreen(
                modifier = Modifier.padding(paddingValues),
                onFacilitySelected = { facility ->
                    selectedFacilityForDetail = facility     // 카드 -> 상세
                }
            )

            // 예약 페이지
            else -> ReservationDetailScreen(
                facility = selectedFacility!!,
                onBackClick = {          // 예약 -> 상세
                    selectedFacilityForDetail = selectedFacility
                    selectedFacility = null
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "마이페이지",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) {
            paddingValues ->
        androidx.compose.foundation.layout.Column(
            modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "마이페이지",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.height(16.dp)
            )
            Text(
                text = "준비 중입니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
