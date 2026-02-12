package com.seyone22.cook.ui.screen.meals

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.seyone22.cook.R
import com.seyone22.cook.data.model.MealEntry
import com.seyone22.cook.data.model.MealEntryWithDetails
import com.seyone22.cook.data.repository.mealEntry.composables.MealEntryBottomSheet
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

object MealsDestination : NavigationDestination {
    override val route = "Meals"
    override val titleRes = R.string.app_name
    override val routeId = 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealsScreen(
    modifier: Modifier,
    viewModel: MealsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
    setOverlayStatus: (Boolean) -> Unit = {},
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current
) {
    val mealEntriesWithDetails by viewModel.mealEntryWithDetails.collectAsState()

    val groupedMealEntries =
        mealEntriesWithDetails?.groupBy { it?.entry?.entryDate ?: LocalDate.now() }
            ?.toSortedMap(compareByDescending<LocalDate> { it.year }.thenByDescending { it.monthValue }
                .thenByDescending { it.dayOfMonth }) ?: emptyMap()

    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(500) }
    val endDate = remember { currentDate.plusDays(500) }
    var selection by remember { mutableStateOf(currentDate) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedMealEntry by remember { mutableStateOf<MealEntryWithDetails?>(null) }

    val lazyListState = rememberLazyListState()

    Column(
        modifier = modifier, verticalArrangement = Arrangement.Top
    ) {
        val state = rememberWeekCalendarState(
            startDate = startDate,
            endDate = endDate,
            firstVisibleWeekDate = currentDate,
        )

        WeekCalendar(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.primary),
            state = state,
            dayContent = { day ->
                Day(day.date, isSelected = selection == day.date) { clicked ->
                    if (selection != clicked) {
                        selection = clicked
                        coroutineScope.launch {
                            lazyListState.scrollToItem(1)
                        }
                    }
                }
            },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = lazyListState
        ) {
            groupedMealEntries.forEach { (date, entries) ->
                stickyHeader {
                    // Sticky Header for each date
                    Column(
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        Text(
                            text = "${date.dayOfMonth} ${
                            date.month.name.lowercase().replaceFirstChar { it.uppercase() }
                        } ${if (date.year != LocalDate.now().year) date.year else ""}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp))
                    }
                }

                item {
                    // Grid of Meal Entry Cards for the specific date
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        entries.forEach { entry ->
                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(120.dp), onClick = {
                                    setOverlayStatus(true)
                                    showBottomSheet = true
                                    selectedMealEntry = entry
                                }) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = entry?.entry?.imageUri,
                                            placeholder = painterResource(id = R.drawable.placeholder),  // Add placeholder image
                                            error = painterResource(id = R.drawable.placeholder)  // Add error image
                                        ),
                                        contentDescription = "Meal Image",
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .fillMaxSize(),  // To make the image cover the available area without distortion,
                                        contentScale = ContentScale.Crop
                                    )
                                    ElevatedAssistChip(
                                        label = { Text("${entry?.entry?.id ?: "Unknown Meal"}") },
                                        onClick = { },
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp)
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet && selectedMealEntry != null) {
        MealEntryBottomSheet(
            onDismiss = { showBottomSheet = false }, mealEntry = selectedMealEntry!!
        )
    }
}

fun launchCamera(context: android.content.Context, onUriReady: (Uri) -> Unit) {
    // Create temporary file in the app's cache directory
    val tempFile = File.createTempFile("meal_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit() // delete on app exit, temp only for initial storage
    }

    // Get the URI for the temporary image file in cache
    val tempUri = FileProvider.getUriForFile(
        context, "${context.packageName}.provider", tempFile
    )

    // Move the image to the app's files directory
    val appFilesDir = context.filesDir
    val imageFile = File(appFilesDir, "meal_${System.currentTimeMillis()}.jpg")

    // Move file from cache to the app's persistent files directory
    tempFile.copyTo(imageFile, overwrite = true)

    // Optionally, delete the original temporary file after moving
    tempFile.delete()

    // Get URI for the final image in the app's files directory
    val finalUri = FileProvider.getUriForFile(
        context, "${context.packageName}.provider", imageFile
    )

    // Return the URI to the caller
    onUriReady(finalUri)
}

private val dateFormatter = DateTimeFormatter.ofPattern("dd")

@Composable
private fun Day(date: LocalDate, isSelected: Boolean, onClick: (LocalDate) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = date.dayOfWeek.getDisplayName(
                    TextStyle.SHORT, java.util.Locale.getDefault()
                ),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = dateFormatter.format(date),
                fontSize = 14.sp,
                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(MaterialTheme.colorScheme.secondary)
                    .align(Alignment.BottomCenter),
            )
        }
    }
}
