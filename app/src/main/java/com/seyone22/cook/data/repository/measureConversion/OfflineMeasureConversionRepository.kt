package com.seyone22.cook.data.repository.measureConversion

import com.seyone22.cook.data.model.MeasureConversion
import kotlinx.coroutines.flow.Flow

class OfflineMeasureConversionRepository(private val measureConversionDao: MeasureConversionDao) :
    MeasureConversionRepository {
    override suspend fun insertMeasureConversion(measureConversion: MeasureConversion): Long =
        measureConversionDao.insert(measureConversion)

    override suspend fun deleteMeasureConversion(measureConversion: MeasureConversion) =
        measureConversionDao.delete(measureConversion)

    override suspend fun updateMeasureConversion(measureConversion: MeasureConversion) =
        measureConversionDao.update(measureConversion)

    override suspend fun getMeasureConversionById(id: Long): MeasureConversion? =
        measureConversionDao.getById(id)

    override suspend fun getRateFor(from: Long, to: Long): Double? =
        measureConversionDao.getRateFor(from, to)

    override suspend fun getAllMeasureConversions(): Flow<List<MeasureConversion?>> =
        measureConversionDao.getAll()
}