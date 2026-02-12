package com.seyone22.cook.data.repository.instruction

import com.seyone22.cook.data.model.Instruction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface InstructionRepository {
    suspend fun insertInstruction(instruction: Instruction)
    suspend fun deleteInstruction(instruction: Instruction)
    suspend fun updateInstruction(instruction: Instruction)

    fun getInstructionById(id: Int): Flow<Instruction?>
    fun getInstructionsForRecipe(recipeId: UUID): Flow<List<Instruction?>>
    fun getAllInstructions(): Flow<List<Instruction?>>
    suspend fun deleteInstructionsForRecipe(id: UUID)
}