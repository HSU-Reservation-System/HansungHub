package com.winterflw.hansunghub.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.winterflw.hansunghub.MainActivity
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

            // ---- 여기서 network 패키지의 API 사용 ----
            RetrofitClient.api.login(LoginRequest(id, pw))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (!response.isSuccessful) {
                            binding.tvStatus.text = "서버 오류"
                            return
                        }

                        val body = response.body()

                        if (body == null) {
                            binding.tvStatus.text = "잘못된 응답"
                            return
                        }

                        if (body.success) {
                            Log.d("HansungLogin", "FastAPI session saved")

                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()

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
