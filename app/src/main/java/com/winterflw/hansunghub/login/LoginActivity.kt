package com.winterflw.hansunghub.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.winterflw.hansunghub.reservation.ReservationActivity
import com.winterflw.hansunghub.databinding.ActivityLoginBinding
import com.winterflw.hansunghub.network.RetrofitClient
import com.winterflw.hansunghub.network.model.LoginRequest
import com.winterflw.hansunghub.network.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {

            val id = binding.etStudentId.text.toString()
            val pw = binding.etPassword.text.toString()

            // 입력 검증
            if (id.isEmpty() || pw.isEmpty()) {
                binding.tvStatus.text = "학번과 비밀번호를 입력해주세요"
                return@setOnClickListener
            }

            binding.tvStatus.text = "로그인 중..."

            // 코딩라운지 로그인 API 호출
            RetrofitClient.api.codingLogin(LoginRequest(id, pw))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (!response.isSuccessful) {
                            binding.tvStatus.text = "서버 오류: ${response.code()}"
                            return
                        }

                        val body = response.body()

                        if (body == null) {
                            binding.tvStatus.text = "잘못된 응답"
                            return
                        }

                        if (body.success) {
                            Log.d("HansungLogin", "Login successful")

                            startActivity(Intent(this@LoginActivity, ReservationActivity::class.java))
                            finish()

                        } else {
                            binding.tvStatus.text = "로그인 실패 - 학번/비밀번호를 확인해주세요"
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        binding.tvStatus.text = "서버 연결 실패: ${t.message}"
                        Log.e("HansungLogin", "Login failed", t)
                    }
                })
        }
    }
}
