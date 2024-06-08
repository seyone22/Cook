package com.seyone22.cook.data.repository.instruction

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seyone22.cook.data.model.Instruction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface InstructionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(instruction: Instruction)
    @Update
    suspend fun update(instruction: Instruction)
    @Delete
    suspend fun delete(instruction: Instruction)

    @Query("SELECT * FROM instructions" +
            "   WHERE id = :id" +
            "   ORDER BY id ASC")
    fun getInstructionById(id: Int): Flow<Instruction>
    @Query("SELECT * FROM instructions" +
            "   WHERE recipeId = :recipeId" +
            "   ORDER BY id ASC")
    fun getInstructionsForRecipe(recipeId: UUID): Flow<List<Instruction>>
    @Query("SELECT * FROM instructions" +
            "   ORDER BY id ASC")
    fun getAllInstructions(): Flow<List<Instruction>>
}