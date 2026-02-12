package com.seyone22.cook.data.repository.measure

import com.seyone22.cook.data.model.Measure
import kotlinx.coroutines.flow.Flow

interface MeasureRepository {
    suspend fun insertMeasure(measure: Measure): Long
    suspend fun deleteMeasure(measure: Measure)
    suspend fun updateMeasure(measure: Measure)

    fun getMeasureById(measureId: Int): Flow<Measure?>
    fun getAllMeasures(): Flow<List<Measure?>>
    fun getMeasureByName(name: String): Flow<Measure?>

}