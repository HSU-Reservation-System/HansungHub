package com.winterflw.hansunghub.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName   // ❗ 반드시 필요
import com.winterflw.hansunghub.MainActivity
import com.winterflw.hansunghub.databinding.ActivityLoginBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val studentId: String, val password: String)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("sessionId")      // 서버 JSON key와 일치!
    val sessionId: String?
)


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
            .baseUrl("http://43.203.173.74:8000") // ⚠️ HTTPS 미적용 상태
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(HansungApi::class.java)

        binding.btnLogin.setOnClickListener {
            val id = binding.etStudentId.text.toString()
            val pw = binding.etPassword.text.toString()

            api.login(LoginRequest(id, pw)).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    // 서버 응답 자체가 실패한 경우
                    if (!response.isSuccessful) {
                        binding.tvStatus.text = "서버 오류"
                        return
                    }

                    val body = response.body()

                    // JSON 파싱 실패
                    if (body == null) {
                        binding.tvStatus.text = "잘못된 응답"
                        return
                    }

                    if (body.success) {
                        val prefs = getSharedPreferences("HansungHubPrefs", MODE_PRIVATE)
                        prefs.edit().putString("sessionId", body.sessionId).apply()

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
