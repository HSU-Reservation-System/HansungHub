package com.winterflw.hansunghub
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
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
                    BottomItem.Home,
                    BottomItem.Reserve,
                    BottomItem.MyPage
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
                        startDestination = BottomItem.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomItem.Home.route) {
                            SimpleCenterText("홈 화면")
                        }
                        composable(BottomItem.Reserve.route) {
                            ReserveScreen()
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

/** 예약 화면 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveScreen() {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // 상태 관리
    var selectedPlace by remember { mutableStateOf("스터디룸 A") }
    var members by remember { mutableStateOf("") }
    var peopleCount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("날짜를 선택하세요") }
    var startTime by remember { mutableStateOf("시작 시간") }
    var endTime by remember { mutableStateOf("종료 시간") }
    var expanded by remember { mutableStateOf(false) }

    val places = listOf("스터디룸 A", "스터디룸 B", "회의실 1", "세미나실", "다목적실")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "공간 예약",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 장소 선택 드롭다운
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedPlace,
                onValueChange = {},
                readOnly = true,
                label = { Text("장소 선택") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxSize()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                places.forEach { place ->
                    DropdownMenuItem(
                        text = { Text(place) },
                        onClick = {
                            selectedPlace = place
                            expanded = false
                        }
                    )
                }
            }
        }

        // 참여 인원 이름
        OutlinedTextField(
            value = members,
            onValueChange = { members = it },
            label = { Text("참여 인원 이름") },
            placeholder = { Text("예: 홍길동, 김철수") },
            modifier = Modifier.fillMaxSize()
        )

        // 총 인원 수
        OutlinedTextField(
            value = peopleCount,
            onValueChange = { peopleCount = it },
            label = { Text("총 인원 수") },
            placeholder = { Text("예: 2") },
            modifier = Modifier.fillMaxSize()
        )

        // 날짜 선택
        OutlinedButton(
            onClick = {
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    val myFormat = "yyyy-MM-dd (E)"
                    val sdf = SimpleDateFormat(myFormat, Locale.KOREAN)
                    selectedDate = sdf.format(calendar.time)
                }, year, month, day).apply {
                    datePicker.minDate = System.currentTimeMillis() - 1000
                }.show()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Text(selectedDate)
        }

        // 시간 선택
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 시작 시간
            OutlinedButton(
                onClick = {
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                        startTime = String.format(Locale.KOREAN, "%02d:%02d", selectedHour, selectedMinute)
                    }, hour, minute, true).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(startTime)
            }

            Text(
                text = "-",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            // 종료 시간
            OutlinedButton(
                onClick = {
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)

                    TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                        endTime = String.format(Locale.KOREAN, "%02d:%02d", selectedHour, selectedMinute)
                    }, hour, minute, true).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(endTime)
            }
        }

        // 예약하기 버튼
        Button(
            onClick = {
                if (members.isNotEmpty() &&
                    peopleCount.isNotEmpty() &&
                    !selectedDate.contains("선택") &&
                    !startTime.contains("시간") &&
                    !endTime.contains("시간")
                ) {
                    val reservationDetails = """
                        장소: $selectedPlace
                        인원: $members ($peopleCount 명)
                        날짜: $selectedDate
                        시간: $startTime - $endTime
                    """.trimIndent()

                    Toast.makeText(context, "예약이 완료되었습니다.\n$reservationDetails", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Text("예약하기")
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
        ReserveScreen()
    }
}