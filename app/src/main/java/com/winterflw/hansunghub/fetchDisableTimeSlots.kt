package com.winterflw.hansunghub

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Comment
import org.jsoup.nodes.TextNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 예약 불가 시간대 수집기
 *
 * - 대상 페이지: https://www.hansung.ac.kr/onestop/8952/subview.do
 * - HTML 구조 가정:
 *    div.conBox 내부에 날짜가 HTML 주석(Comment)로 포함되고,
 *    같은 블록의 텍스트(TextNode)에 "HH:MM~HH:MM" 형식의 시간대가 적혀 있음.
 * - 동작:
 *    1) 주석에서 날짜 매칭 → matchedDate = true
 *    2) 텍스트에서 방 이름 매칭 → matchedRoom = true
 *    3) 둘 다 만족 + 라인에 ~ 포함 → "HH:MM~HH:MM" 정규식으로 파싱해서
 *       시간 슬롯("HH:00")들을 disabled 집합에 누적
 */
object DisableTimeFetcher {

    private val client = OkHttpClient()
    private val main = Handler(Looper.getMainLooper())
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * @param roomName   방 이름 (예: "IB101")
     * @param date       선택 날짜(LocalDate)
     * @param onResult   비활성 시간 슬롯 집합 콜백 (예: {"09:00","10:00"})
     */
    fun fetch(
        roomName: String,
        date: LocalDate,
        onResult: (Set<String>) -> Unit
    ) {
        val url = "https://www.hansung.ac.kr/onestop/8952/subview.do"
        val req = Request.Builder().url(url).get().build()

        client.newCall(req).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                // 네트워크 실패 시 빈 집합 반환
                main.post { onResult(emptySet()) }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val disabled = mutableSetOf<String>()
                try {
                    val body = response.body?.string().orEmpty()
                    val doc = Jsoup.parse(body)
                    val elements = doc.select("div.conBox")
                    val targetDate: String = dateFmt.format(date) // 예: "2025-11-06"

                    elements.forEach { el ->
                        var matchedRoom = false
                        var matchedDate = false

                        // div.conBox 하위 모든 Node를 순회: Comment / TextNode 둘 다 확인
                        el.childNodes().forEach { n ->
                            // 1) 날짜 매칭: Comment 노드에서 targetDate 포함 여부
                            //    Jsoup 버전에 따라 Comment의 데이터 접근이 data / data() 로 차이날 수 있어
                            if (n is Comment) {
                                // A안(일반적): Kotlin property 형태
                                val commentData: String = try {
                                    n.data
                                } catch (_: Throwable) {
                                    // B안(만약 data() 로 노출되면)
                                    try {
                                        @Suppress("UNCHECKED_CAST")
                                        (Comment::class.java.getMethod("data").invoke(n) as? String) ?: ""
                                    } catch (_: Throwable) {
                                        ""
                                    }
                                }
                                if (commentData.contains(targetDate)) {
                                    matchedDate = true
                                }
                                return@forEach
                            }

                            // 2) 텍스트 라인 처리: 방 이름/시간 구간 파싱
                            if (n is TextNode) {
                                val line: String = n.text().trim()

                                // 2-1) 방 이름 먼저 매칭
                                if (!matchedRoom) {
                                    val normalizedTitle = line.replace("\"", "")
                                    if (
                                        normalizedTitle.contains(roomName) &&
                                        (normalizedTitle.contains("- $roomName") || normalizedTitle.contains(roomName))
                                    ) {
                                        matchedRoom = true
                                    }
                                }

                                // 2-2) 날짜와 방이 모두 매칭되고, 라인에 "~"가 있을 때만 시간 구간 파싱
                                if (matchedRoom && matchedDate && line.contains("~")) {
                                    // 따옴표 제거 + 트리밍
                                    val normalized = line.replace("\"", "").trim()

                                    // 정규식: "HH:MM~HH:MM" 패턴의 1번 캡처 그룹만 추출
                                    val regex = Regex("""\n?\r?(\d{2}:\d{2}~\d{2}:\d{2})""") // "09:00~12:00" 같은 구간

                                    // ⚠️ 타입 추론 에러 방지: 람다 파라미터 타입 명시(MatchResult / String)
                                    regex.findAll(normalized)
                                        .map { mr: MatchResult -> mr.groupValues[1] } // 1번 캡처 그룹만 사용
                                        .forEach { range: String ->
                                            // "HH:MM~HH:MM" → 시작/종료 '시'를 정수로 파싱
                                            val parts = range.split("~")
                                            if (parts.size == 2) {
                                                val start = parts[0].substring(0, 2).toIntOrNull() ?: -1
                                                val end = parts[1].substring(0, 2).toIntOrNull() ?: -1

                                                // 유효 범위 검사: 00~24, start < end
                                                if (start in 0..23 && end in 1..24 && start < end) {
                                                    // 시작시간부터 종료시간-1까지 "HH:00" 슬롯을 예약 불가로 누적
                                                    for (h in start until end) {
                                                        disabled.add(String.format("%02d:00", h))
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // 파싱 중 오류가 나도 일단 지금까지 수집한 것(또는 빈 집합) 반환
                } finally {
                    // 메인 스레드로 콜백
                    main.post { onResult(disabled) }
                }
            }
        })
    }
}