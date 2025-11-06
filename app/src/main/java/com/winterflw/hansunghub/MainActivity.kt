package com.winterflw.hansunghub
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.winterflw.hansunghub.ui.theme.HansunghubTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HansunghubTheme {
                val navController = rememberNavController()

                val items = listOf(
                    BottomItem.Reserve,   // 시작 탭
                    BottomItem.Calendar,
                    BottomItem.Rooms,
                    BottomItem.Settings
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomBar(
                            items = items,
                            currentDestination = navController
                                .currentBackStackEntryAsState().value?.destination
                        ) { route ->
                            if (route != null) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomItem.Reserve.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomItem.Reserve.route) {
                            ReserveScreen(
                                onOpenLegacyReservation = {
                                    // XML 기반 ReservationActivity가 있다면 이걸로 연동 가능
                                    val intent = Intent(this@MainActivity, ReservationActivity::class.java)
                                    startActivity(intent)
                                }
                            )
                        }
                        composable(BottomItem.Calendar.route) {
                            SimpleCenterText("캘린더 화면(준비 중)")
                        }
                        composable(BottomItem.Rooms.route) {
                            SimpleCenterText("공간 목록/선택(준비 중)")
                        }
                        composable(BottomItem.Settings.route) {
                            SimpleCenterText("설정 화면(준비 중)")
                        }
                    }
                }
            }
        }
    }
}

/** 하단 네비게이션 항목 정의 */
sealed class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Reserve  : BottomItem("reserve",  "예약",    Icons.Filled.Home)
    data object Calendar : BottomItem("calendar", "캘린더",  Icons.Filled.CalendarToday)
    data object Rooms    : BottomItem("rooms",    "공간",    Icons.Filled.MeetingRoom)
    data object Settings : BottomItem("settings", "설정",    Icons.Filled.Settings)
}

/** 하단 바 */
@Composable
private fun BottomBar(
    items: List<BottomItem>,
    currentDestination: NavDestination?,
    onClick: (String?) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = { onClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

/** 예약 탭(시작 화면) */
@Composable
fun ReserveScreen(
    onOpenLegacyReservation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "예약 탭",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "XML 기반 예약 화면으로 이동하려면 아래 버튼을 누르세요.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.padding(12.dp))
        Button(onClick = onOpenLegacyReservation) {
            Text("예약 화면 열기 (ReservationActivity)")
        }
    }
}

/** 임시 중앙 텍스트 */
@Composable
fun SimpleCenterText(msg: String) {
    Text(
        text = msg,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewReserve() {
    HansunghubTheme {
        ReserveScreen(onOpenLegacyReservation = {})
    }
}