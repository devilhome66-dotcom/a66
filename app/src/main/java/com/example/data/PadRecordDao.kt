package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PadRecordDao {
    @Query("SELECT * FROM pad_records ORDER BY epochDay DESC, id DESC")
    fun getAllRecords(): Flow<List<PadRecord>>

    @Query("SELECT * FROM pad_records WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY epochDay ASC, id ASC")
    fun getRecordsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<PadRecord>>

    @Query("SELECT * FROM pad_records WHERE dateString LIKE :yearMonthPattern || '%' ORDER BY epochDay ASC, id ASC")
    fun getRecordsForMonth(yearMonthPattern: String): Flow<List<PadRecord>>

    @Query("SELECT * FROM pad_records WHERE dateString = :dateString LIMIT 1")
    suspend fun getRecordByDateString(dateString: String): PadRecord?

    @Query("SELECT * FROM pad_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): PadRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PadRecord): Long

    @Update
    suspend fun update(record: PadRecord)

    @Delete
    suspend fun delete(record: PadRecord)

    @Query("DELETE FROM pad_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pad_records")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PadRecord>)
}
