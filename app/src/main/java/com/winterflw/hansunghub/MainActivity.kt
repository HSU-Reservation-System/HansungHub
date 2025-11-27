package com.winterflw.hansunghub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.winterflw.hansunghub.reservation.ReservationActivity
import com.winterflw.hansunghub.ui.theme.HansunghubTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HansunghubTheme {
                val navController = rememberNavController()

                val items = listOf(
                    BottomItem.Home,
                    BottomItem.Reserve,
                    BottomItem.MyPage
                )

                val context = LocalContext.current

                Scaffold(
                    bottomBar = {
                        BottomBar(
                            items = items,
                            currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                            onHomeClick = {
                                navController.navigate(BottomItem.Home.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            onReserveClick = {
                                // 👉 예약 탭 클릭 시 XML 기반 ReservationActivity 실행
                                val intent = Intent(context, ReservationActivity::class.java)
                                context.startActivity(intent)
                            },
                            onMyPageClick = {
                                navController.navigate(BottomItem.MyPage.route)
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomItem.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomItem.Home.route) {
                            SimpleCenterText("홈 화면")
                        }
                        composable(BottomItem.MyPage.route) {
                            SimpleCenterText("마이페이지 (준비 중)")
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
    data object Home     : BottomItem("home",     "홈",      Icons.Filled.Home)
    data object Reserve  : BottomItem("reserve",  "예약",    Icons.Filled.EventNote)
    data object MyPage   : BottomItem("mypage",   "마이페이지", Icons.Filled.Person)
}

/** 하단 바 */
@Composable
private fun BottomBar(
    items: List<BottomItem>,
    currentDestination: NavDestination?,
    onHomeClick: () -> Unit,
    onReserveClick: () -> Unit,
    onMyPageClick: () -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    when (item) {
                        BottomItem.Home -> onHomeClick()
                        BottomItem.Reserve -> onReserveClick()
                        BottomItem.MyPage -> onMyPageClick()
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun SimpleCenterText(msg: String) {
    Text(text = msg, modifier = Modifier.padding(16.dp))
}
