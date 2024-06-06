package com.seyone22.cook.data.repository.measureConversion

import com.seyone22.cook.data.model.MeasureConversion
import kotlinx.coroutines.flow.Flow

interface MeasureConversionRepository {
    suspend fun insertMeasureConversion(measureConversion: MeasureConversion): Long
    suspend fun deleteMeasureConversion(measureConversion: MeasureConversion)
    suspend fun updateMeasureConversion(measureConversion: MeasureConversion)

    suspend fun getAllMeasureConversions(): Flow<List<MeasureConversion?>>
    suspend fun getMeasureConversionById(id: Long): MeasureConversion?
    suspend fun getRateFor(from: Long, to: Long): Double?
}