package com.winterflw.hansunghub.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winterflw.hansunghub.R
import com.winterflw.hansunghub.network.RetrofitClient
import com.winterflw.hansunghub.network.model.LoginRequest
import com.winterflw.hansunghub.reservation.ReservationActivity
import com.winterflw.hansunghub.ui.theme.HansunghubTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HansunghubTheme() {
                LoginScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, ReservationActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 로그인 함수
    // 로그인 함수
    val performLogin: () -> Unit = {
        if (studentId.isEmpty() || password.isEmpty()) {
            statusMessage = "학번과 비밀번호를 입력해주세요"
        } else {
            isLoading = true
            statusMessage = "로그인 중..."

            scope.launch {
                try {
                    val response = RetrofitClient.api.codingLogin(
                        LoginRequest(studentId, password)
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        onLoginSuccess()
                    } else {
                        statusMessage = "로그인 실패 - 학번/비밀번호를 확인해주세요"
                        isLoading = false
                    }
                } catch (e: Exception) {
                    statusMessage = "서버 연결 실패: ${e.message}"
                    isLoading = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8F9FD)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로고 이미지
            Image(
                painter = painterResource(id = R.drawable.login_logo),
                contentDescription = "한성대 로고",
                modifier = Modifier
                    .width(550.dp)
                    .height(250.dp)
                    .padding(bottom = 2.dp)
            )

            Text(
                text = "HansungHub",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E5BFF)
            )

            Text(
                text = "한성대학교 시설 예약 시스템",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 로그인 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 제목
                    Text(
                        text = "로그인",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1D2E)
                    )

                    Text(
                        text = "한성대학교 포털 계정으로 로그인하세요",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 학번 입력
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("학번") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E5BFF),
                            unfocusedBorderColor = Color(0xFF6B8FFF),
                            focusedContainerColor = Color(0xFFF5F7FB),
                            unfocusedContainerColor = Color(0xFFF5F7FB)
                        )
                    )

                    // 비밀번호 입력
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("비밀번호") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { performLogin() }
                        ),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E5BFF),
                            unfocusedBorderColor = Color(0xFF6B8FFF),
                            focusedContainerColor = Color(0xFFF5F7FB),
                            unfocusedContainerColor = Color(0xFFF5F7FB)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 로그인 버튼
                    Button(
                        onClick = performLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E5BFF)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "로그인",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 상태 메시지
                    if (statusMessage.isNotEmpty()) {
                        Text(
                            text = statusMessage,
                            fontSize = 13.sp,
                            color = if (statusMessage.contains("실패") || statusMessage.contains("오류"))
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF6B7280),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}