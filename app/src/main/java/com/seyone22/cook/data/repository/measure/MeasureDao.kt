package com.seyone22.cook.data.repository.measure

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.Measure
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasureDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(measure: Measure): Long

    @Update
    suspend fun update(measure: Measure)

    @Delete
    suspend fun delete(measure: Measure)

    @Query(
        "SELECT * FROM measures" +
                "   WHERE id = :measureId" +
                "   ORDER BY abbreviation ASC"
    )
    fun getMeasureById(measureId: Int): Flow<Measure>

    @Query(
        "SELECT * FROM measures" +
                "   ORDER BY abbreviation ASC"
    )
    fun getAllMeasures(): Flow<List<Measure>>

    @Query(
        "SELECT * FROM measures" +
                "   WHERE name = :name" +
                "   ORDER BY name ASC"
    )
    fun getMeasureByName(name: String): Flow<Measure>
}