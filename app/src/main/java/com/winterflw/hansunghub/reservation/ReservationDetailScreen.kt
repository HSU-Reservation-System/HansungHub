package com.winterflw.hansunghub.reservation

import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.winterflw.hansunghub.network.RetrofitClient
import com.winterflw.hansunghub.network.model.*
import com.winterflw.hansunghub.reservation.model.FacilityType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    facility: FacilityType,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val calendar = remember { Calendar.getInstance() }

    // State
    var spaces by remember { mutableStateOf<List<SpaceItem>>(emptyList()) }
    var selectedSpace by remember { mutableStateOf<SpaceItem?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTimes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var disabledTimes by remember { mutableStateOf<List<String>>(emptyList()) }
    var userInfo by remember { mutableStateOf<UserInfoResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // 추가 필드 (상상베이스/학술정보관용)
    var addItem1 by remember { mutableStateOf("") }
    var addItem2 by remember { mutableStateOf("") }

    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // 사용자 정보 로드
                userInfo = RetrofitClient.api.getUserInfo()

                // 공간 목록 로드
                val response = RetrofitClient.api.getSpaces()
                spaces = response.spaces.filter {
                    it.spaceSeq in facility.spaceSeqRange
                }
            } catch (e: Exception) {
                Toast.makeText(context, "데이터 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 날짜/공간 변경 시 비활성 시간 조회
    LaunchedEffect(selectedDate, selectedSpace) {
        if (selectedDate.isNotEmpty() && selectedSpace != null) {
            scope.launch {
                try {
                    val response = RetrofitClient.api.getDisabledTimes(
                        selectedDate,
                        selectedSpace!!.spaceSeq
                    )
                    disabledTimes = response.disabled
                    selectedTimes = emptySet() // 시간 선택 초기화
                } catch (e: Exception) {
                    Toast.makeText(context, "시간 조회 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(facility.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(facility.color),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 100.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 공간 선택
            SectionCard(title = "공간 선택", icon = Icons.Default.Place) {
                if (spaces.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    var expanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedSpace?.spaceName ?: "공간을 선택하세요",
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ArrowDropUp
                                    else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            spaces.forEach { space ->
                                DropdownMenuItem(
                                    text = { Text(space.spaceName) },
                                    onClick = {
                                        selectedSpace = space
                                        selectedTimes = emptySet()
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedSpace == space) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. 날짜 선택
            SectionCard(title = "날짜 선택", icon = Icons.Default.DateRange) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                calendar.set(year, month, day)
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
                                selectedDate = sdf.format(calendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            // 오늘 00:00:00부터 선택 가능
                            val today = Calendar.getInstance()
                            today.set(Calendar.HOUR_OF_DAY, 0)
                            today.set(Calendar.MINUTE, 0)
                            today.set(Calendar.SECOND, 0)
                            today.set(Calendar.MILLISECOND, 0)
                            datePicker.minDate = today.timeInMillis
                            show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (selectedDate.isEmpty()) "날짜를 선택하세요"
                        else selectedDate
                    )
                }
            }

            // 3. 시간 선택
            AnimatedVisibility(visible = selectedDate.isNotEmpty() && selectedSpace != null) {
                SectionCard(title = "시간 선택 (최대 6시간)", icon = Icons.Default.Schedule) {
                    val timeSlots = listOf(
                        "09:00", "10:00", "11:00", "12:00",
                        "13:00", "14:00", "15:00", "16:00",
                        "17:00", "18:00", "19:00", "20:00"
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(timeSlots) { time ->
                            val isDisabled = time in disabledTimes
                            val isBeforeNow = isTimeBeforeNow(time, selectedDate)
                            val isSelected = time in selectedTimes

                            TimeSlotChip(
                                time = time,
                                isDisabled = isDisabled || isBeforeNow,
                                isSelected = isSelected,
                                onClick = {
                                    if (!isDisabled && !isBeforeNow) {
                                        selectedTimes = if (isSelected) {
                                            selectedTimes - time
                                        } else {
                                            if (selectedTimes.size >= 6) {
                                                Toast.makeText(context, "최대 6시간까지 선택 가능", Toast.LENGTH_SHORT).show()
                                                selectedTimes
                                            } else {
                                                selectedTimes + time
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 4. 추가 정보 입력 (상상베이스/학술정보관)
            if (facility == FacilityType.SANGSANG_BASE || facility == FacilityType.LIBRARY) {
                SectionCard(title = "추가 정보", icon = Icons.Default.Info) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (facility == FacilityType.SANGSANG_BASE) {
                            OutlinedTextField(
                                value = addItem1,
                                onValueChange = { addItem1 = it },
                                label = { Text("전체 이용자 성명/학번") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("예: 홍길동/1234567") }
                            )
                            OutlinedTextField(
                                value = addItem2,
                                onValueChange = { newValue ->
                                    // 숫자만 입력 가능
                                    if (newValue.all { it.isDigit() }) {
                                        addItem2 = newValue
                                    }
                                },
                                label = { Text("총 인원수(숫자만 입력)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("예: 4") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        if (facility == FacilityType.LIBRARY) {
                            OutlinedTextField(
                                value = addItem1,
                                onValueChange = { addItem1 = it },
                                label = { Text("동반 이용자 학번/이름 입력") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("ex. 23홍길동, 24김한성..") }
                            )
                            OutlinedTextField(
                                value = addItem2,
                                onValueChange = { newValue ->
                                    // 숫자만 입력 가능
                                    if (newValue.all { it.isDigit() }) {
                                        addItem2 = newValue
                                    }
                                },
                                label = { Text("총 인원수(숫자만 입력)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("예: 4") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }

            // 5. 예약 버튼
            Button(
                onClick = {
                    if (selectedSpace == null) {
                        Toast.makeText(context, "공간을 선택해주세요", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedDate.isEmpty()) {
                        Toast.makeText(context, "날짜를 선택해주세요", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedTimes.isEmpty()) {
                        Toast.makeText(context, "시간을 선택해주세요", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (userInfo == null) {
                        Toast.makeText(context, "사용자 정보 로드 중...", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // 예약 요청
                    scope.launch {
                        isLoading = true
                        try {
                            val request = ReserveRequest(
                                spaceSeq = selectedSpace!!.spaceSeq,
                                spaceName = selectedSpace!!.spaceName,
                                date = selectedDate,
                                timeList = selectedTimes.toList(),
                                tel = userInfo!!.telno,
                                email = userInfo!!.email,
                                addItem1 = if (addItem1.isNotEmpty()) addItem1 else null,
                                addItem2 = if (addItem2.isNotEmpty()) addItem2 else null
                            )

                            val result = RetrofitClient.api.reserve(request)

                            if (result.success) {
                                Toast.makeText(
                                    context,
                                    "예약 성공!\n시간: ${result.times.joinToString()}",
                                    Toast.LENGTH_LONG
                                ).show()
                                onBackClick()
                            } else {
                                Toast.makeText(context, "예약 실패", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && selectedSpace != null && selectedDate.isNotEmpty() && selectedTimes.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(facility.color)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("예약하기", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TimeSlotChip(
    time: String,
    isDisabled: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    when {
                        isDisabled -> Modifier.background(
                            com.winterflw.hansunghub.ui.theme.TimeDisabled
                        )
                        isSelected -> Modifier.background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    com.winterflw.hansunghub.ui.theme.HansungBlue,
                                    com.winterflw.hansunghub.ui.theme.HansungBlueLight
                                )
                            )
                        )
                        else -> Modifier.background(
                            com.winterflw.hansunghub.ui.theme.TimeAvailable
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = time,
                color = when {
                    isDisabled -> com.winterflw.hansunghub.ui.theme.TextTertiary
                    isSelected -> Color.White
                    else -> com.winterflw.hansunghub.ui.theme.TextPrimary
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

private fun isTimeBeforeNow(timeSlot: String, selectedDate: String): Boolean {
    try {
        // 한국 시간대 설정
        val koreaTimeZone = TimeZone.getTimeZone("Asia/Seoul")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        sdf.timeZone = koreaTimeZone

        // 오늘 날짜 구하기 (한국 시간)
        val calendar = Calendar.getInstance(koreaTimeZone)
        val today = sdf.format(calendar.time)

        // 선택한 날짜가 오늘보다 이전이면 모두 불가능
        val selectedDateObj = sdf.parse(selectedDate)
        val todayDateObj = sdf.parse(today)

        if (selectedDateObj != null && todayDateObj != null) {
            // 과거 날짜면 모두 불가
            if (selectedDateObj.before(todayDateObj)) {
                return true
            }

            // 미래 날짜면 모두 가능
            if (selectedDateObj.after(todayDateObj)) {
                return false
            }
        }

        // 여기까지 왔다는 건 오늘 날짜 → 현재 시간과 비교 (한국 시간)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val slotHour = timeSlot.split(":")[0].toIntOrNull() ?: return false

        // 현재 시간 이하면 예약 불가
        return slotHour <= currentHour

    } catch (e: Exception) {
        return false
    }
}