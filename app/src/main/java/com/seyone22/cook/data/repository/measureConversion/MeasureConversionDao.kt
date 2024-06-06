package com.seyone22.cook.data.repository.measureConversion

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.MeasureConversion
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasureConversionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(measureConversion: MeasureConversion) : Long
    @Update
    suspend fun update(measureConversion: MeasureConversion)
    @Delete
    suspend fun delete(measureConversion: MeasureConversion)

    @Query("SELECT * FROM conversions")
    fun getAll(): Flow<List<MeasureConversion>>
    @Query("SELECT * FROM conversions WHERE id = :id")
    suspend fun getById(id: Long): MeasureConversion?

    @Query("SELECT conversionFactor FROM conversions WHERE fromUnitId = :fromUnit AND toUnitId = :toUnit")
    suspend fun getRateFor(fromUnit: Long, toUnit: Long): Double?
}