package com.winterflw.hansunghub.reservation

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.winterflw.hansunghub.R
import com.winterflw.hansunghub.databinding.DialogReservationSuccessBinding
import com.winterflw.hansunghub.databinding.ItemReserveResultBinding
import com.winterflw.hansunghub.network.RetrofitClient
import com.winterflw.hansunghub.network.model.ReserveRequest
import com.winterflw.hansunghub.network.model.ReserveResultItem
import com.winterflw.hansunghub.network.model.TimeSlot
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ReservationActivity : AppCompatActivity() {

    private val calendar: Calendar = Calendar.getInstance()

    // UI 요소
    private lateinit var spinnerPlace: Spinner
    private lateinit var etMembers: EditText
    private lateinit var etPeopleCount: EditText
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvSelectedTime: TextView
    private lateinit var rvTime: RecyclerView
    private lateinit var btnReserve: Button

    // 시간 선택 관련 변수
    private lateinit var timeAdapter: TimeSlotAdapter
    private lateinit var timeSlots: MutableList<TimeSlot>
    private val selectedTimes = mutableSetOf<String>()   // 여러 시간 저장
    private var disabledTimes = listOf<String>()         // 서버에서 받은 비활성 시간

    // 공간 정보 매핑
    private var placeNames = listOf<String>()
    private var placeSeqs = listOf<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        // XML 뷰 연결
        spinnerPlace = findViewById(R.id.spinnerPlace)
        etMembers = findViewById(R.id.etMembers)
        etPeopleCount = findViewById(R.id.etPeopleCount)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvSelectedTime = findViewById(R.id.tvSelectedTime)
        rvTime = findViewById(R.id.rvTime)
        btnReserve = findViewById(R.id.btnReserve)

        // 공간 목록 불러오기
        loadSpaces()

        // 날짜 선택
        tvSelectedDate.setOnClickListener {
            showDatePicker()
        }

        // 시간 선택 RecyclerView 구성
        setupTimeRecyclerView()

        // 예약 버튼
        btnReserve.setOnClickListener {

            if (!validateInput()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTimes.isEmpty()) {
                Toast.makeText(this, "시간을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val placeName = spinnerPlace.selectedItem.toString()
            val rawDate = tvSelectedDate.text.toString()
            val date = rawDate.substring(0, 10)   // "2025-11-17"

            val idx = placeNames.indexOf(placeName)
            if (idx == -1) {
                Toast.makeText(this, "공간 정보 오류", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedSeq = placeSeqs[idx]

            val request = ReserveRequest(
                spaceSeq = selectedSeq,
                spaceName = placeName,
                date = date,
                timeList = selectedTimes.toList(),
                tel = "01066278002",
                email = "eric91405@naver.com"
            )

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = RetrofitClient.api.reserve(request)

                    // 📌 예약 성공/실패 결과 Dialog 띄우기
                    showReserveSuccessDialog(result.results)

                } catch (e: Exception) {
                    Toast.makeText(
                        this@ReservationActivity,
                        "오류 발생: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ---------------------------------------------------------
    // *** 예약 성공 Dialog ***
    // ---------------------------------------------------------
    private fun showReserveSuccessDialog(results: List<ReserveResultItem>) {

        val dialogBinding =
            DialogReservationSuccessBinding.inflate(layoutInflater)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // RecyclerView 설정 (결과 리스트)
        dialogBinding.rvTimes.apply {
            layoutManager = LinearLayoutManager(this@ReservationActivity)
            adapter = ReserveResultAdapter(results)
        }

        dialogBinding.btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // ---------------------------------------------------------
    // 공간 목록 불러오기
    // ---------------------------------------------------------
    private fun loadSpaces() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val res = RetrofitClient.api.getSpaces()

                placeNames = res.spaces.map { it.spaceName }
                placeSeqs = res.spaces.map { it.spaceSeq }

                val adapter = ArrayAdapter(
                    this@ReservationActivity,
                    android.R.layout.simple_spinner_item,
                    placeNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPlace.adapter = adapter

            } catch (e: Exception) {
                Toast.makeText(this@ReservationActivity, "공간 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------------------------------------------------
    // 날짜 선택 Dialog
    // ---------------------------------------------------------
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                updateDateInView()
                fetchDisabledTimes()
            },
            year, month, day
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    private fun updateDateInView() {
        val sdf = SimpleDateFormat("yyyy-MM-dd (E)", Locale.KOREAN)
        tvSelectedDate.text = sdf.format(calendar.time)
    }


    // ---------------------------------------------------------
    // 서버에서 비활성 시간 받아오기
    // ---------------------------------------------------------
    private fun fetchDisabledTimes() {
        if (placeNames.isEmpty()) return
        if (!tvSelectedDate.text.contains("-")) return

        val placeName = spinnerPlace.selectedItem.toString()
        val spaceSeq = placeSeqs[placeNames.indexOf(placeName)]
        val date = tvSelectedDate.text.substring(0, 10) // yyyy-MM-dd

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val res = RetrofitClient.api.getDisabledTimes(date, spaceSeq)
                disabledTimes = res.disabled

                applyDisabledTimes()

            } catch (e: Exception) {
                Toast.makeText(this@ReservationActivity, "비활성 시간 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------------------------------------------------
    // 비활성 시간 적용
    // ---------------------------------------------------------
    private fun applyDisabledTimes() {
        selectedTimes.clear()

        timeSlots.forEach { slot ->
            slot.isAvailable = !disabledTimes.contains(slot.time)
            slot.isSelected = false
        }

        tvSelectedTime.text = "선택된 시간: 없음"
        timeAdapter.notifyDataSetChanged()
    }


    // ---------------------------------------------------------
    // 시간 RecyclerView 구성
    // ---------------------------------------------------------
    private fun setupTimeRecyclerView() {

        timeSlots = createDailyTimeSlots()

        timeAdapter = TimeSlotAdapter(timeSlots) { clicked ->

            if (!clicked.isAvailable) {
                Toast.makeText(this, "예약 불가한 시간입니다.", Toast.LENGTH_SHORT).show()
                return@TimeSlotAdapter
            }

            if (clicked.time in selectedTimes) {
                selectedTimes.remove(clicked.time)
                clicked.isSelected = false
            } else {
                if (selectedTimes.size >= 6) {
                    Toast.makeText(this, "1일 최대 6시간까지 예약할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    return@TimeSlotAdapter
                }
                selectedTimes.add(clicked.time)
                clicked.isSelected = true
            }

            tvSelectedTime.text =
                if (selectedTimes.isEmpty()) "선택된 시간: 없음"
                else "선택된 시간: ${selectedTimes.joinToString()}"

            timeAdapter.notifyDataSetChanged()
        }

        rvTime.layoutManager = GridLayoutManager(this, 3)
        rvTime.adapter = timeAdapter
    }


    // ---------------------------------------------------------
    // 시간 목록 생성
    // ---------------------------------------------------------
    private fun createDailyTimeSlots(): MutableList<TimeSlot> {
        val result = mutableListOf<TimeSlot>()
        val times = listOf(
            "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00",
            "17:00", "18:00", "19:00", "20:00"
        )
        times.forEach {
            result.add(TimeSlot(time = it, isAvailable = true))
        }
        return result
    }

    // ---------------------------------------------------------
    // 입력값 검증
    // ---------------------------------------------------------
    private fun validateInput(): Boolean {
        return etMembers.text.toString().isNotEmpty() &&
                etPeopleCount.text.toString().isNotEmpty() &&
                !tvSelectedDate.text.toString().contains("선택") &&
                selectedTimes.isNotEmpty()
    }
}
