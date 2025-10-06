package com.seyone22.cook.data.repository.instructionsection

import com.seyone22.cook.data.model.InstructionSection
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineInstructionSectionRepository(
    private val sectionDao: InstructionSectionDao
) : InstructionSectionRepository {

    override suspend fun insertSection(section: InstructionSection) =
        sectionDao.insert(section)

    override suspend fun updateSection(section: InstructionSection) =
        sectionDao.update(section)

    override suspend fun deleteSection(section: InstructionSection) =
        sectionDao.delete(section)

    override suspend fun getSectionById(id: Int): Flow<InstructionSection?> =
        sectionDao.getSectionById(id)

    override suspend fun getSectionsForRecipe(recipeId: UUID): Flow<List<InstructionSection?>> =
        sectionDao.getSectionsForRecipe(recipeId)

    override suspend fun getAllSections(): Flow<List<InstructionSection?>> =
        sectionDao.getAllSections()

    override suspend fun deleteSectionsForRecipe(id: UUID) =
        sectionDao.deleteSectionsForRecipe(id)
}
