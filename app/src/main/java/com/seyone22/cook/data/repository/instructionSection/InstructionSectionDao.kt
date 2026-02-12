package com.seyone22.cook.data.repository.instructionsection

import androidx.room.*
import com.seyone22.cook.data.model.InstructionSection
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface InstructionSectionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(section: InstructionSection)

    @Update
    suspend fun update(section: InstructionSection)

    @Delete
    suspend fun delete(section: InstructionSection)

    @Query(
        "SELECT * FROM instruction_sections " +
                "WHERE id = :id " +
                "ORDER BY id ASC"
    )
    fun getSectionById(id: Int): Flow<InstructionSection>

    @Query(
        "SELECT * FROM instruction_sections " +
                "WHERE recipeId = :recipeId " +
                "ORDER BY id ASC"
    )
    fun getSectionsForRecipe(recipeId: UUID): Flow<List<InstructionSection>>

    @Query(
        "SELECT * FROM instruction_sections " +
                "ORDER BY id ASC"
    )
    fun getAllSections(): Flow<List<InstructionSection>>

    @Query(
        "DELETE FROM instruction_sections " +
                "WHERE recipeId = :id"
    )
    suspend fun deleteSectionsForRecipe(id: UUID)
}
