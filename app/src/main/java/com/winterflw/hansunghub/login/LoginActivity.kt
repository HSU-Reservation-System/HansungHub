package com.winterflw.hansunghub.login

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.winterflw.hansunghub.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // 리다이렉트 직접 감지하도록 설정
    private val client = OkHttpClient.Builder()
        .followRedirects(false)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val id = binding.etStudentId.text.toString().trim()
            val pw = binding.etPassword.text.toString().trim()

            if (id.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "학번과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            } else {
                tryHansungLogin(id, pw)
            }
        }
    }

    private fun tryHansungLogin(userId: String, userPwd: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginUrl = "https://hansung.ac.kr/hnuLogin/cncschool/loginProcess.do"

                val formBody = FormBody.Builder()
                    .add("siteId", "cncschool")
                    .add("returnUrl", "")
                    .add("referer", "/cncschool/index.do")
                    .add("inputUserId", userId)
                    .add("inputUserPwd", userPwd)
                    .build()

                val request = Request.Builder()
                    .url(loginUrl)
                    .post(formBody)
                    .header("User-Agent", "Mozilla/5.0 (Android HansungHub)")
                    .header("Referer", "https://hansung.ac.kr/hnuLogin/cncschool/loginView.do")
                    .header("Origin", "https://hansung.ac.kr")
                    .build()

                val response = client.newCall(request).execute()
                val statusCode = response.code
                val cookies = response.headers("Set-Cookie")
                val location = response.header("Location")

                Log.d("HansungLogin", "StatusCode: $statusCode")
                Log.d("HansungLogin", "Cookies: $cookies")
                Log.d("HansungLogin", "Location: $location")

                withContext(Dispatchers.Main) {
                    when {
                        statusCode == 302 && cookies.isNotEmpty() -> {
                            Toast.makeText(this@LoginActivity, "✅ 로그인 성공!", Toast.LENGTH_SHORT).show()
                        }
                        statusCode == 200 -> {
                            Toast.makeText(this@LoginActivity, "❌ 로그인 실패 (아이디/비밀번호 확인)", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this@LoginActivity, "⚠️ 응답 코드: $statusCode", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
