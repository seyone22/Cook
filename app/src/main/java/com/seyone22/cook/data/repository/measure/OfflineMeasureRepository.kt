package com.seyone22.cook.data.repository.measure

import com.seyone22.cook.data.model.Measure
import kotlinx.coroutines.flow.Flow

class OfflineMeasureRepository(private val measureDao: MeasureDao) : MeasureRepository {
    override suspend fun insertMeasure(measure: Measure): Long = measureDao.insert(measure)
    override suspend fun deleteMeasure(measure: Measure) = measureDao.delete(measure)
    override suspend fun updateMeasure(measure: Measure) = measureDao.update(measure)

    override fun getMeasureById(measureId: Int): Flow<Measure?> =
        measureDao.getMeasureById(measureId)

    override fun getAllMeasures(): Flow<List<Measure?>> = measureDao.getAllMeasures()
    override fun getMeasureByName(name: String): Flow<Measure?> =
        measureDao.getMeasureByName(name)
}