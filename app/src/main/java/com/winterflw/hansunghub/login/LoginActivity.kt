package com.winterflw.hansunghub.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.winterflw.hansunghub.MainActivity
import com.winterflw.hansunghub.databinding.ActivityLoginBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val studentId: String, val password: String)
data class LoginResponse(val success: Boolean, val sessionId: String?)

interface HansungApi {
    @POST("/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>
}

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var api: HansungApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://43.203.173.74:8000") // ⚠️ 현재는 HTTPS 미적용 상태
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(HansungApi::class.java)

        binding.btnLogin.setOnClickListener {
            val id = binding.etStudentId.text.toString()
            val pw = binding.etPassword.text.toString()

            api.login(LoginRequest(id, pw)).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val body = response.body()
                    if (body?.success == true) {
                        // 로그인 성공 시 sessionId 저장
                        val prefs = getSharedPreferences("HansungHubPrefs", MODE_PRIVATE)
                        prefs.edit().putString("sessionId", body.sessionId).apply()

                        // ✅ 저장 확인용 로그
                        val savedSession = prefs.getString("sessionId", null)
                        Log.d("HansungLogin", "Saved sessionId: $savedSession")

                        // 다음 화면으로 이동
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        binding.tvStatus.text = "로그인 실패"
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    binding.tvStatus.text = "서버 연결 실패"
                }
            })
        }
    }
}
