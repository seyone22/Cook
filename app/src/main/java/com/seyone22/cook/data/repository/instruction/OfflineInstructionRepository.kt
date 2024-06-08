package com.seyone22.cook.data.repository.instruction

import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.repository.instruction.InstructionDao
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineInstructionRepository(private val instructionDao: InstructionDao):
    InstructionRepository {
    override suspend fun insertInstruction(instruction: Instruction) = instructionDao.insert(instruction)
    override suspend fun deleteInstruction(instruction: Instruction) = instructionDao.delete(instruction)
    override suspend fun updateInstruction(instruction: Instruction) = instructionDao.update(instruction)

    override suspend fun getInstructionById(id: Int): Flow<Instruction?> = instructionDao.getInstructionById(id)
    override suspend fun getInstructionsForRecipe(recipeId: UUID): Flow<List<Instruction?>> = instructionDao.getInstructionsForRecipe(recipeId)
    override suspend fun getAllInstructions(): Flow<List<Instruction?>> = instructionDao.getAllInstructions()
    override suspend fun deleteInstructionsForRecipe(id: UUID) = instructionDao.deleteInstructionsForRecipe(id)
}