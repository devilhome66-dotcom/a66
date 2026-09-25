package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pad_records",
    indices = [
        Index(value = ["epochDay"], unique = false),
        Index(value = ["dateString"], unique = false)
    ]
)
data class PadRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,      // Format: "YYYY-MM-DD"
    val epochDay: Long,          // Epoch days from 1970-01-01 for fast range queries
    val floor1Count: Int = 0,    // 1樓補充片數
    val floor2Count: Int = 0,    // 2樓補充片數
    val floor3Count: Int = 0,    // 3樓補充片數
    val padType: String = "一般型", // 一般型 / 日用 / 夜用 / 護墊 / 特殊
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalCount: Int
        get() = (floor1Count.coerceAtLeast(0)) + 
                (floor2Count.coerceAtLeast(0)) + 
                (floor3Count.coerceAtLeast(0))
}
