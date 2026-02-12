import com.seyone22.cook.data.model.MealEntry
import com.seyone22.cook.data.model.MealEntryIngredientCrossRef
import com.seyone22.cook.data.model.MealEntryTagCrossRef
import com.seyone22.cook.data.model.MealEntryWithDetails
import com.seyone22.cook.data.repository.mealEntry.MealEntryDao
import com.seyone22.cook.data.repository.mealEntry.MealEntryRepository
import kotlinx.coroutines.flow.first

class OfflineMealEntryRepository(
    private val mealEntryDao: MealEntryDao
) : MealEntryRepository {

    override suspend fun insertMealEntry(entry: MealEntry): Long {
        return mealEntryDao.insertMealEntry(entry)
    }

    override suspend fun insertTags(entryId: Long, tagIds: List<Long>) {
        val refs = tagIds.map { tagId ->
            MealEntryTagCrossRef(mealEntryId = entryId, tagId = tagId)
        }
        mealEntryDao.insertTagCrossRefs(refs)
    }

    override suspend fun insertIngredients(
        entryId: Long, ingredients: List<Triple<Long, Double, String>>
    ) {
        val refs = ingredients.map { (ingredientId, quantity, unit) ->
            MealEntryIngredientCrossRef(
                mealEntryId = entryId, ingredientId = ingredientId, quantity = quantity, unit = unit
            )
        }
        mealEntryDao.insertIngredientCrossRefs(refs)
    }

    override suspend fun getMealEntryWithDetails(id: Long): MealEntryWithDetails? {
        val entry = mealEntryDao.getMealEntryById(id) ?: return null
        val tags = mealEntryDao.getTagsForMealEntry(id)
        val ingredients = mealEntryDao.getIngredientsForMealEntry(id)
        return MealEntryWithDetails(entry, tags, ingredients)
    }

    // New method to get all MealEntryWithDetails
    // Modified method to get all MealEntryWithDetails
    override suspend fun getAllMealEntriesWithDetails(): List<MealEntryWithDetails> {
        val allEntries = mealEntryDao.getAllMealEntries()

        return allEntries.map { entry ->
            // Collect each Flow before returning it
            val tags = mealEntryDao.getTagsForMealEntry(entry.id)
            val ingredients = mealEntryDao.getIngredientsForMealEntry(entry.id)

            // Return resolved MealEntryWithDetails
            MealEntryWithDetails(entry, tags, ingredients)
        }
    }

    override suspend fun updateNotes(entryId: Long, newNotes: String) {
        mealEntryDao.updateNotes(entryId, newNotes)
    }

    override suspend fun deleteMealEntry(entryId: Long) {
        mealEntryDao.deleteMealEntry(entryId)
    }
}
