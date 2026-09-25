package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PadRecord
import com.example.data.PadRepository
import com.example.util.DateUtils
import com.example.util.RangePreset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SummaryStats(
    val totalFloor1: Int = 0,
    val totalFloor2: Int = 0,
    val totalFloor3: Int = 0,
    val totalAll: Int = 0,
    val daysRecorded: Int = 0,
    val daysInRange: Int = 0,
    val dailyAverage: Double = 0.0,
    val percentFloor1: Float = 0f,
    val percentFloor2: Float = 0f,
    val percentFloor3: Float = 0f
)

class PadViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PadRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PadRepository(db.padRecordDao())
    }

    // --- Daily Logging State ---
    private val _selectedDate = MutableStateFlow(DateUtils.getTodayString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _floor1Count = MutableStateFlow(0)
    val floor1Count: StateFlow<Int> = _floor1Count.asStateFlow()

    private val _floor2Count = MutableStateFlow(0)
    val floor2Count: StateFlow<Int> = _floor2Count.asStateFlow()

    private val _floor3Count = MutableStateFlow(0)
    val floor3Count: StateFlow<Int> = _floor3Count.asStateFlow()

    private val _padType = MutableStateFlow("一般型")
    val padType: StateFlow<String> = _padType.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _currentRecordId = MutableStateFlow<Long?>(null)
    val currentRecordId: StateFlow<Long?> = _currentRecordId.asStateFlow()

    private val _isExistingRecord = MutableStateFlow(false)
    val isExistingRecord: StateFlow<Boolean> = _isExistingRecord.asStateFlow()

    private val _saveMessage = MutableSharedFlow<String>()
    val saveMessage: SharedFlow<String> = _saveMessage.asSharedFlow()

    // --- All Records ---
    val allRecords: StateFlow<List<PadRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Monthly Stats State ---
    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentYearMonth())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthRecords: StateFlow<List<PadRecord>> = _selectedMonth
        .flatMapLatest { month ->
            repository.getRecordsForMonth(month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthStats: StateFlow<SummaryStats> = monthRecords.map { records ->
        calculateStats(records, null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryStats())

    // --- Date Range State ("幾日到幾日共消耗總數") ---
    private val todayEpoch = DateUtils.getTodayEpochDay()
    private val _rangeStartEpoch = MutableStateFlow(todayEpoch - 6) // Default last 7 days
    val rangeStartEpoch: StateFlow<Long> = _rangeStartEpoch.asStateFlow()

    private val _rangeEndEpoch = MutableStateFlow(todayEpoch)
    val rangeEndEpoch: StateFlow<Long> = _rangeEndEpoch.asStateFlow()

    private val _activePreset = MutableStateFlow<RangePreset?>(RangePreset.LAST_7_DAYS)
    val activePreset: StateFlow<RangePreset?> = _activePreset.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val rangeRecords: StateFlow<List<PadRecord>> = combine(
        _rangeStartEpoch,
        _rangeEndEpoch
    ) { start, end ->
        val sortedStart = minOf(start, end)
        val sortedEnd = maxOf(start, end)
        Pair(sortedStart, sortedEnd)
    }.flatMapLatest { (start, end) ->
        repository.getRecordsBetween(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rangeStats: StateFlow<SummaryStats> = combine(
        rangeRecords,
        _rangeStartEpoch,
        _rangeEndEpoch
    ) { records, start, end ->
        val days = (maxOf(start, end) - minOf(start, end) + 1).toInt()
        calculateStats(records, days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryStats())

    init {
        // Load today's existing record if any
        loadRecordForDate(DateUtils.getTodayString())
    }

    // --- Date Navigation & Loading ---
    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
        loadRecordForDate(dateStr)
    }

    fun stepDay(offsetDays: Int) {
        val newDate = DateUtils.addDays(_selectedDate.value, offsetDays)
        selectDate(newDate)
    }

    private fun loadRecordForDate(dateStr: String) {
        viewModelScope.launch {
            val record = repository.getRecordByDateString(dateStr)
            if (record != null) {
                _currentRecordId.value = record.id
                _floor1Count.value = record.floor1Count
                _floor2Count.value = record.floor2Count
                _floor3Count.value = record.floor3Count
                _padType.value = record.padType.ifEmpty { "一般型" }
                _notes.value = record.notes
                _isExistingRecord.value = true
            } else {
                _currentRecordId.value = null
                _floor1Count.value = 0
                _floor2Count.value = 0
                _floor3Count.value = 0
                _notes.value = ""
                _isExistingRecord.value = false
            }
        }
    }

    // --- Count Adjustments ---
    fun adjustFloorCount(floor: Int, delta: Int) {
        when (floor) {
            1 -> _floor1Count.value = (_floor1Count.value + delta).coerceAtLeast(0)
            2 -> _floor2Count.value = (_floor2Count.value + delta).coerceAtLeast(0)
            3 -> _floor3Count.value = (_floor3Count.value + delta).coerceAtLeast(0)
        }
    }

    fun setFloorCount(floor: Int, count: Int) {
        when (floor) {
            1 -> _floor1Count.value = count.coerceAtLeast(0)
            2 -> _floor2Count.value = count.coerceAtLeast(0)
            3 -> _floor3Count.value = count.coerceAtLeast(0)
        }
    }

    fun setPadType(type: String) {
        _padType.value = type
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun saveCurrentRecord() {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            val epochDay = DateUtils.parseDateStringToEpochDay(dateStr)
            val record = PadRecord(
                id = _currentRecordId.value ?: 0,
                dateString = dateStr,
                epochDay = epochDay,
                floor1Count = _floor1Count.value,
                floor2Count = _floor2Count.value,
                floor3Count = _floor3Count.value,
                padType = _padType.value,
                notes = _notes.value.trim(),
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.saveRecord(record)
            _currentRecordId.value = id
            _isExistingRecord.value = true
            _saveMessage.emit("已成功登記 ${DateUtils.formatShortDate(dateStr)} 補充紀錄！(共 ${record.totalCount} 片)")
        }
    }

    fun deleteRecord(record: PadRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
            if (record.dateString == _selectedDate.value) {
                loadRecordForDate(_selectedDate.value)
            }
            _saveMessage.emit("已刪除 ${record.dateString} 紀錄")
        }
    }

    fun editRecord(record: PadRecord) {
        selectDate(record.dateString)
    }

    // --- Monthly Controls ---
    fun selectMonth(yearMonth: String) {
        _selectedMonth.value = yearMonth
    }

    fun stepMonth(offset: Int) {
        val current = _selectedMonth.value
        _selectedMonth.value = if (offset > 0) {
            DateUtils.getNextMonth(current)
        } else {
            DateUtils.getPreviousMonth(current)
        }
    }

    // --- Range Controls ---
    fun setRange(startEpoch: Long, endEpoch: Long, preset: RangePreset? = null) {
        _rangeStartEpoch.value = minOf(startEpoch, endEpoch)
        _rangeEndEpoch.value = maxOf(startEpoch, endEpoch)
        _activePreset.value = preset
    }

    fun applyPreset(preset: RangePreset) {
        val (start, end) = DateUtils.getPresetRange(preset)
        setRange(start, end, preset)
    }

    // --- Summary Math ---
    private fun calculateStats(records: List<PadRecord>, totalDaysOverride: Int?): SummaryStats {
        var f1 = 0
        var f2 = 0
        var f3 = 0
        records.forEach {
            f1 += it.floor1Count
            f2 += it.floor2Count
            f3 += it.floor3Count
        }
        val total = f1 + f2 + f3
        val recordedCount = records.count { it.totalCount > 0 }
        val days = totalDaysOverride ?: if (records.isNotEmpty()) records.size else 1
        val avg = if (days > 0) (total.toDouble() / days) else 0.0

        val p1 = if (total > 0) (f1.toFloat() / total) else 0f
        val p2 = if (total > 0) (f2.toFloat() / total) else 0f
        val p3 = if (total > 0) (f3.toFloat() / total) else 0f

        return SummaryStats(
            totalFloor1 = f1,
            totalFloor2 = f2,
            totalFloor3 = f3,
            totalAll = total,
            daysRecorded = recordedCount,
            daysInRange = days,
            dailyAverage = avg,
            percentFloor1 = p1,
            percentFloor2 = p2,
            percentFloor3 = p3
        )
    }

    // --- Report Generator for Sharing / Copying ---
    fun generateRangeReportText(): String {
        val startStr = DateUtils.epochDayToDateString(_rangeStartEpoch.value)
        val endStr = DateUtils.epochDayToDateString(_rangeEndEpoch.value)
        val stats = rangeStats.value
        val records = rangeRecords.value

        val sb = StringBuilder()
        sb.append("📋【衛生棉補充/消耗統計報表】\n")
        sb.append("📅 統計區間：$startStr ~ $endStr (共 ${stats.daysInRange} 天)\n")
        sb.append("📊 補充總計：${stats.totalAll} 片 (登記天數: ${stats.daysRecorded} 天)\n")
        sb.append("📈 日均消耗：${String.format(java.util.Locale.getDefault(), "%.1f", stats.dailyAverage)} 片/天\n\n")
        sb.append("🏢 各樓層分佈：\n")
        sb.append("  • 1樓：${stats.totalFloor1} 片 (${String.format(java.util.Locale.getDefault(), "%.1f", stats.percentFloor1 * 100)}%)\n")
        sb.append("  • 2樓：${stats.totalFloor2} 片 (${String.format(java.util.Locale.getDefault(), "%.1f", stats.percentFloor2 * 100)}%)\n")
        sb.append("  • 3樓：${stats.totalFloor3} 片 (${String.format(java.util.Locale.getDefault(), "%.1f", stats.percentFloor3 * 100)}%)\n\n")
        sb.append("📝 明細筆數：共 ${records.size} 筆記錄\n")
        if (records.isNotEmpty()) {
            sb.append("最新明細摘要：\n")
            records.take(5).forEach { r ->
                sb.append("  - ${r.dateString}: 1F=${r.floor1Count}, 2F=${r.floor2Count}, 3F=${r.floor3Count} (計${r.totalCount}片)")
                if (r.notes.isNotEmpty()) sb.append(" [${r.notes}]")
                sb.append("\n")
            }
            if (records.size > 5) {
                sb.append("  ... 更多請在 App 中查看\n")
            }
        }
        return sb.toString()
    }

    // --- Demo Data Generator & Clear All ---
    fun seedDemoData() {
        viewModelScope.launch {
            val today = DateUtils.getTodayEpochDay()
            val sampleRecords = mutableListOf<PadRecord>()
            val sampleNotes = listOf(
                "定期每早巡檢補充",
                "清潔人員回報補滿",
                "下午活動人潮補充",
                "一般耗損補充",
                "全館補充完畢",
                "補至標準庫存滿水位"
            )

            // Generate records for the past 25 days
            for (i in 0..24) {
                val epoch = today - i
                val dateStr = DateUtils.epochDayToDateString(epoch)
                // Realistic random-like replenishment variations
                val f1 = when (i % 5) {
                    0 -> 15
                    1 -> 20
                    2 -> 10
                    3 -> 25
                    else -> 12
                }
                val f2 = when (i % 4) {
                    0 -> 12
                    1 -> 18
                    2 -> 8
                    else -> 15
                }
                val f3 = when (i % 3) {
                    0 -> 6
                    1 -> 10
                    else -> 8
                }
                val type = if (i % 3 == 0) "綜合/一般" else if (i % 2 == 0) "日用型" else "夜用型"
                val note = sampleNotes[i % sampleNotes.size]

                sampleRecords.add(
                    PadRecord(
                        dateString = dateStr,
                        epochDay = epoch,
                        floor1Count = f1,
                        floor2Count = f2,
                        floor3Count = f3,
                        padType = type,
                        notes = note
                    )
                )
            }
            repository.insertAll(sampleRecords)
            loadRecordForDate(_selectedDate.value)
            _saveMessage.emit("已成功載入 25 天示範數據！可查看月度與區間統計。")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            loadRecordForDate(_selectedDate.value)
            _saveMessage.emit("已清除所有紀錄數據")
        }
    }
}
