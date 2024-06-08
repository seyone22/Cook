package com.seyone22.cook.data.repository.instruction

import com.seyone22.cook.data.model.Instruction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface InstructionRepository {
    suspend fun insertInstruction(instruction: Instruction)
    suspend fun deleteInstruction(instruction: Instruction)
    suspend fun updateInstruction(instruction: Instruction)

    suspend fun getInstructionById(id: Int): Flow<Instruction?>
    suspend fun getInstructionsForRecipe(recipeId: UUID): Flow<List<Instruction?>>
    suspend fun getAllInstructions(): Flow<List<Instruction?>>
}