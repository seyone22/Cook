package com.seyone22.cook.data.repository.measure

import com.seyone22.cook.data.model.Measure
import kotlinx.coroutines.flow.Flow

interface MeasureRepository {
    suspend fun insertMeasure(measure: Measure): Long
    suspend fun deleteMeasure(measure: Measure)
    suspend fun updateMeasure(measure: Measure)

    suspend fun getMeasureById(measureId: Int): Flow<Measure?>
    suspend fun getAllMeasures(): Flow<List<Measure?>>
}