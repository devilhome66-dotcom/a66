package com.example.data

import kotlinx.coroutines.flow.Flow

class PadRepository(private val dao: PadRecordDao) {
    val allRecords: Flow<List<PadRecord>> = dao.getAllRecords()

    fun getRecordsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<PadRecord>> {
        return dao.getRecordsBetween(startEpochDay, endEpochDay)
    }

    fun getRecordsForMonth(yearMonth: String): Flow<List<PadRecord>> {
        return dao.getRecordsForMonth(yearMonth)
    }

    suspend fun getRecordByDateString(dateString: String): PadRecord? {
        return dao.getRecordByDateString(dateString)
    }

    suspend fun getRecordById(id: Long): PadRecord? {
        return dao.getRecordById(id)
    }

    suspend fun saveRecord(record: PadRecord): Long {
        return dao.insert(record)
    }

    suspend fun deleteRecord(record: PadRecord) {
        dao.delete(record)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }

    suspend fun insertAll(records: List<PadRecord>) {
        dao.insertAll(records)
    }
}
