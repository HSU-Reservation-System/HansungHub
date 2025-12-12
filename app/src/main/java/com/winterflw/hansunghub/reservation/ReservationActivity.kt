package com.winterflw.hansunghub.reservation


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winterflw.hansunghub.login.LoginActivity
import com.winterflw.hansunghub.network.RetrofitClient
import com.winterflw.hansunghub.network.model.UserInfoResponse
import com.winterflw.hansunghub.reservation.model.FacilityType
import com.winterflw.hansunghub.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Divider


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
        containerColor = BackgroundLight,
        bottomBar = {
            ModernBottomNavigationBar(
                showMyPage = showMyPage,
                onReservationClick = {
                    showMyPage = false
                    selectedFacility = null
                    selectedFacilityForDetail = null
                },
                onMyPageClick = {
                    showMyPage = true
                    selectedFacility = null
                    selectedFacilityForDetail = null
                }
            )
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

/** 모던한 하단 네비게이션 */
@Composable
fun ModernBottomNavigationBar(
    showMyPage: Boolean,
    onReservationClick: () -> Unit,
    onMyPageClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(120.dp)
        ) {
            NavigationBarItem(
                selected = !showMyPage,
                onClick = onReservationClick,
                icon = {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (!showMyPage) 52.dp else 40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (!showMyPage) {
                                        Modifier.background(
                                            brush = Brush.horizontalGradient(
                                                listOf(HansungBlue, HansungBlueLight)
                                            )
                                        )
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.EventNote,
                                "예약",
                                tint = if (!showMyPage) Color.White else TextTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        "예약",
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (!showMyPage) FontWeight.Bold else FontWeight.Normal,
                        color = if (!showMyPage) HansungBlue else TextTertiary,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                selected = showMyPage,
                onClick = onMyPageClick,
                icon = {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (showMyPage) 52.dp else 40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (showMyPage) {
                                        Modifier.background(
                                            brush = Brush.horizontalGradient(
                                                listOf(HansungBlue, HansungBlueLight)
                                            )
                                        )
                                    } else {
                                        Modifier.background(Color.Transparent)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                "마이페이지",
                                tint = if (showMyPage) Color.White else TextTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        "마이페이지",
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (showMyPage) FontWeight.Bold else FontWeight.Normal,
                        color = if (showMyPage) HansungBlue else TextTertiary,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<UserInfoResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 사용자 정보 로드
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                userInfo = RetrofitClient.api.getUserInfo()
            } catch (e: Exception) {
                Toast.makeText(context, "정보 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 프로필 아이콘
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(HansungBlue, HansungBlueLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            // 사용자 정보 카드
            if (isLoading) {
                CircularProgressIndicator(color = HansungBlue)
            } else if (userInfo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "내 정보",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Divider(color = SurfaceLight)

                        // 이름
                        InfoRow(
                            icon = Icons.Default.Person,
                            label = "이름",
                            value = userInfo!!.userNm
                        )

                        // 학번
                        InfoRow(
                            icon = Icons.Default.Badge,
                            label = "학번",
                            value = userInfo!!.hakbun
                        )

                        // 이메일
                        InfoRow(
                            icon = Icons.Default.Email,
                            label = "이메일",
                            value = userInfo!!.email
                        )

                        // 전화번호
                        InfoRow(
                            icon = Icons.Default.Phone,
                            label = "전화번호",
                            value = userInfo!!.telno
                        )
                    }
                }
            } else {
                Text(
                    "정보를 불러올 수 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 로그아웃 버튼
            Button(
                onClick = {
                    // LoginActivity로 이동하고 모든 액티비티 종료
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    Toast.makeText(context, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.ExitToApp, "로그아웃")
                Spacer(Modifier.width(8.dp))
                Text(
                    "로그아웃",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // 바텀 네비게이션 공간
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HansungBlue,
            modifier = Modifier.size(24.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
