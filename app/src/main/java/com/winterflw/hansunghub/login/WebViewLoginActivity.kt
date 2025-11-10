package com.winterflw.hansunghub.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.winterflw.hansunghub.MainActivity
import com.winterflw.hansunghub.databinding.ActivityWebviewLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebViewLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewLoginBinding
    private val TAG = "WebViewLogin"

    // 로그인 성공을 판별할 URL 조각 (리다이렉트되는 경로)
    private val successUrlContains = "/cncschool/index.do"  // 필요시 바꿔주세요
    private val loginPageUrl = "https://hansung.ac.kr/hnuLogin/cncschool/loginView.do"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // WebView 설정
        val webView: WebView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.allowContentAccess = true

        // (선택) mixed content 허용이 필요하면 아래 설정(HTTPS 내부 리소스 섞일 때)
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //     webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        // }

        // 프로그레스바 연결
        binding.progressBar.visibility = View.GONE
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = newProgress
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        // WebViewClient: 페이지 로딩/리다이렉트 감지
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // 외부 링크는 브라우저로 열고 싶으면 처리
                val url = request?.url?.toString() ?: return false
                // 내부 로그인 흐름(같은 호스트)은 WebView에서 처리
                return if (Uri.parse(url).host?.contains("hansung.ac.kr") == true) {
                    false
                } else {
                    // 외부 링크는 기본 브라우저로
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d(TAG, "onPageStarted: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "onPageFinished: $url")

                // 1) 쿠키를 읽어보고 JSESSIONID 존재하면 로그인 성공으로 처리
                val cookieManager = CookieManager.getInstance()
                val cookieString = cookieManager.getCookie("https://hansung.ac.kr")
                Log.d(TAG, "Cookies for hansung.ac.kr -> $cookieString")

                val isSuccessUrl = url?.contains(successUrlContains) == true
                val hasValidSession = cookieString?.contains("JSESSIONID") == true


                if (isSuccessUrl && hasValidSession) {
                    saveCookieToPrefs(cookieString)
                    onLoginSucceeded()
                }

            }
        }

        // 처음 로그인 페이지 로드
        webView.loadUrl(loginPageUrl)
    }

    private fun saveCookieToPrefs(cookie: String?) {
        if (cookie == null) return
        val prefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        prefs.edit().putString("cookie", cookie).apply()
        Log.d(TAG, "Saved cookie -> $cookie")
    }

    private fun onLoginSucceeded() {
        // UI 스레드에서 처리
        runOnUiThread {
            Toast.makeText(this, "로그인 완료 - 세션 저장됨", Toast.LENGTH_SHORT).show()
            // 예: MainActivity로 이동
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onBackPressed() {
        val web = binding.webView
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    // 로그아웃 할 때는 아래처럼 WebView Cookie 및 저장소를 지워야 함
    private fun clearLoginCookies() {
        CoroutineScope(Dispatchers.IO).launch {
            val cm = CookieManager.getInstance()
            cm.removeAllCookies(null)
            cm.flush()
            getSharedPreferences("session", Context.MODE_PRIVATE).edit().remove("cookie").apply()
            Log.d(TAG, "Cleared cookies")
        }
    }
}
