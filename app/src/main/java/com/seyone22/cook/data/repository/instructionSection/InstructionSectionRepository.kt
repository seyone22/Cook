package com.seyone22.cook.data.repository.instructionsection

import com.seyone22.cook.data.model.InstructionSection
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface InstructionSectionRepository {
    suspend fun insertSection(section: InstructionSection)
    suspend fun updateSection(section: InstructionSection)
    suspend fun deleteSection(section: InstructionSection)

    fun getSectionById(id: Int): Flow<InstructionSection?>
    fun getSectionsForRecipe(recipeId: UUID): Flow<List<InstructionSection?>>
    fun getAllSections(): Flow<List<InstructionSection?>>
    suspend fun deleteSectionsForRecipe(id: UUID)
}
