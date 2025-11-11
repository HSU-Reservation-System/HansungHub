package com.winterflw.hansunghub

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * activity_reservation.xml 레이아웃을 사용하는 예약 화면 액티비티입니다.
 */
class eservationActivity : AppCompatActivity() {

    // Calendar 객체를 멤버 변수로 선언하여 날짜와 시간 선택에 사용합니다.
    private val calendar: Calendar = Calendar.getInstance()

    // 뷰 컴포넌트들을 선언합니다.
    private lateinit var spinnerPlace: Spinner
    private lateinit var etMembers: EditText
    private lateinit var etPeopleCount: EditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvSelectedStartTime: TextView
    private lateinit var tvSelectedEndTime: TextView
    private lateinit var btnReserve: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. XML 레이아웃 파일을 화면에 설정합니다.
        setContentView(R.layout.activity_reservation)

        // 2. XML의 뷰 ID를 사용해 뷰 컴포넌트들을 초기화합니다.
        spinnerPlace = findViewById(R.id.spinnerPlace)
        etMembers = findViewById(R.id.etMembers)
        etPeopleCount = findViewById(R.id.etPeopleCount)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvSelectedStartTime = findViewById(R.id.tvSelectedStartTime)
        tvSelectedEndTime = findViewById(R.id.tvSelectedEndTime)
        btnReserve = findViewById(R.id.btnReserve)

        // 3. 장소 선택 스피너 설정
        setupPlaceSpinner()

        // 4. 날짜 선택 텍스트뷰 클릭 리스너 설정
        tvSelectedDate.setOnClickListener {
            showDatePicker()
        }

        // 5. 시작 시간 선택 텍스트뷰 클릭 리스너 설정
        tvSelectedStartTime.setOnClickListener {
            // isStartTime = true
            showTimePicker(tvSelectedStartTime)
        }

        // 6. 종료 시간 선택 텍스트뷰 클릭 리스너 설정
        tvSelectedEndTime.setOnClickListener {
            // isStartTime = false
            showTimePicker(tvSelectedEndTime)
        }

        // 7. 예약하기 버튼 클릭 리스너 설정
        btnReserve.setOnClickListener {
            // 간단한 유효성 검사
            if (validateInput()) {
                // TODO: 실제 예약 로직 구현 (예: 데이터베이스에 저장, 서버로 전송)

                // 예약 정보 수집 (예시)
                val place = spinnerPlace.selectedItem.toString()
                val members = etMembers.text.toString()
                val count = etPeopleCount.text.toString()
                val date = tvSelectedDate.text.toString()
                val startTime = tvSelectedStartTime.text.toString()
                val endTime = tvSelectedEndTime.text.toString()

                val reservationDetails = """
                    장소: $place
                    인원: $members ($count 명)
                    날짜: $date
                    시간: $startTime - $endTime
                """.trimIndent()

                // 예약 완료 토스트 메시지
                Toast.makeText(this, "예약이 완료되었습니다.\n$reservationDetails", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 장소 선택 스피너를 설정합니다. (임시 데이터 사용)
     */
    private fun setupPlaceSpinner() {
        // TODO: 실제 장소 목록 데이터로 교체해야 합니다.
        val places = arrayOf("스터디룸 A", "스터디룸 B", "회의실 1", "세미나실", "다목적실")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, places)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlace.adapter = adapter
    }

    /**
     * 날짜 선택 다이얼로그 (DatePickerDialog)를 보여줍니다.
     */
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // 선택된 날짜로 Calendar 객체 업데이트
                calendar.set(selectedYear, selectedMonth, selectedDay)
                // 선택된 날짜를 텍스트뷰에 표시
                updateDateInView()
            }, year, month, day)

        // 오늘 이전 날짜는 선택할 수 없도록 설정 (선택 사항)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    /**
     * 텍스트뷰에 선택된 날짜를 포맷에 맞게 업데이트합니다.
     */
    private fun updateDateInView() {
        val myFormat = "yyyy-MM-dd (E)" // 날짜 포맷 (예: 2025-11-06 (목))
        val sdf = SimpleDateFormat(myFormat, Locale.KOREAN)
        tvSelectedDate.text = sdf.format(calendar.time)
    }

    /**
     * 시간 선택 다이얼로그 (TimePickerDialog)를 보여줍니다.
     * @param textView 시간을 표시할 TextView (시작 시간 또는 종료 시간)
     */
    private fun showTimePicker(textView: TextView) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this,
            { _, selectedHour, selectedMinute ->
                // 선택된 시간을 포맷에 맞게 텍스트뷰에 표시
                val formattedTime = String.format(Locale.KOREAN, "%02d:%02d", selectedHour, selectedMinute)
                textView.text = formattedTime
            }, hour, minute, true) // true: 24시간 형식 사용

        timePickerDialog.show()
    }

    /**
     * 사용자가 모든 필수 항목을 입력했는지 검사합니다.
     * @return 모두 입력되었으면 true, 아니면 false
     */
    private fun validateInput(): Boolean {
        // Spinner는 기본 선택값이 있으므로 별도 검사 안 함 (필요시 추가)
        return etMembers.text.toString().isNotEmpty() &&
                etPeopleCount.text.toString().isNotEmpty() &&
                !tvSelectedDate.text.toString().contains("선택") && // hint 텍스트와 비교
                !tvSelectedStartTime.text.toString().contains("시간") && // hint 텍스트와 비교
                !tvSelectedEndTime.text.toString().contains("시간") // hint 텍스트와 비교
    }
}