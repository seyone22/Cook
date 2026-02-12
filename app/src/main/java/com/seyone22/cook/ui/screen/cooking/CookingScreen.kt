package com.seyone22.cook.ui.screen.cooking

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import java.util.UUID

object CookingDestination : NavigationDestination {
    override val route = "Cooking"
    override val titleRes = com.seyone22.cook.R.string.app_name
    override val routeId = 12
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CookingScreen(
    viewModel: CookingViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
    backStackEntry: String,
    initialScale: Float = 1.0f // New parameter
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Collecting State correctly from ViewModel
    val cookingViewState by viewModel.cookingViewState.collectAsStateWithLifecycle()
    val activeInstructions by viewModel.activeInstructions.collectAsStateWithLifecycle()
    val checkedIngredients by viewModel.checkedIngredients.collectAsStateWithLifecycle()
    val timeLeft by com.seyone22.cook.service.TimerService.remainingTime.collectAsStateWithLifecycle()
    val scaleFactor by viewModel.scaleFactor.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { activeInstructions.size })
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var showDashboard by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var selectedTimerDuration by remember { mutableLongStateOf(0L) }
    var textSizeMultiplier by rememberSaveable { mutableFloatStateOf(1f) }

    val recipeId = remember(backStackEntry) {
        try { UUID.fromString(backStackEntry) } catch (e: Exception) { null }
    }

    // REGEX for finding times
    val timeRegex = remember {
        Regex("(\\d+)\\s*(min(?:ute)?s?|h(?:ou)?rs?)", RegexOption.IGNORE_CASE)
    }

    // 1. DATA LOADING & WAKE LOCK
    LaunchedEffect(recipeId) {
        recipeId?.let {
            viewModel.fetchData()
            viewModel.loadRecipeDetails(it)
        }
    }

    LaunchedEffect(initialScale) {
        viewModel.setScaleFactor(initialScale.toDouble())
    }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    // 2. FILTERING INGREDIENTS (Safe access to UI state)
    val ingredientsForRecipe = remember(cookingViewState.recipeIngredients, recipeId) {
        cookingViewState.recipeIngredients.filter { it?.recipeId == recipeId }
    }

    var showScaleDialog by remember { mutableStateOf(false) }

    // --- SCALE DIALOG ---
    if (showScaleDialog) {
        // Reusing your existing ScaleRecipeDialogAction logic
        com.seyone22.cook.ui.common.dialog.GenericDialog(
            dialogAction = com.seyone22.cook.ui.common.dialog.action.ScaleRecipeDialogAction(
                initialEntry = scaleFactor,
                itemName = "Scale Factor",
                onAdd = { newFactor ->
                    viewModel.setScaleFactor(newFactor)
                    showScaleDialog = false
                }
            ),
            onDismiss = { showScaleDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CookTopBar(
                title = "Step ${pagerState.currentPage + 1} / ${activeInstructions.size}",
                currentActivity = CookingDestination.route,
                navController = navController
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(horizontal = 16.dp)) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                            enabled = pagerState.currentPage > 0
                        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev") }

                        FilledIconButton(
                            onClick = { selectedTab = 0; showDashboard = true },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) { Icon(Icons.Default.RestaurantMenu, "Ingredients") }

                        IconButton(
                            onClick = {
                                if (pagerState.currentPage < activeInstructions.size - 1) {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                } else { navController.popBackStack() }
                            }
                        ) {
                            Icon(
                                if (pagerState.currentPage == activeInstructions.size - 1) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                "Next",
                                tint = if (pagerState.currentPage == activeInstructions.size - 1) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 8.dp))

                        IconButton(onClick = {
                            textSizeMultiplier = when (textSizeMultiplier) {
                                1f -> 1.25f
                                1.25f -> 1.5f
                                else -> 1f
                            }
                        }) { Icon(Icons.Default.FormatSize, "Text Size") }
                    }
                }

                AnimatedVisibility(
                    visible = timeLeft != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.padding(start = 8.dp, bottom = 16.dp),
                        onClick = { selectedTab = 1; showDashboard = true },
                        icon = { Icon(Icons.Default.Timer, null) },
                        text = {
                            val mins = (timeLeft ?: 0) / 60
                            val secs = (timeLeft ?: 0) % 60
                            Text(String.format("%02d:%02d", mins, secs))
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (activeInstructions.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LinearProgressIndicator(
                        progress = { (pagerState.currentPage + 1).toFloat() / activeInstructions.size },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) { page ->
                        // Inside the HorizontalPager in CookingScreen.kt
                        val isMetric by viewModel.isMetric.collectAsStateWithLifecycle()
                        val scaleFactor by viewModel.scaleFactor.collectAsStateWithLifecycle()
                        val rawInstruction = activeInstructions[page]
// Apply conversion before creating the AnnotatedString
                        val instruction = viewModel.convertText(rawInstruction, isMetric, scaleFactor)

                        val annotatedText = buildAnnotatedString {
                            var lastIndex = 0
                            timeRegex.findAll(instruction).forEach { match ->
                                append(instruction.substring(lastIndex, match.range.first))
                                pushStringAnnotation(tag = "TIMER", annotation = match.value)
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold)) {
                                    append(match.value)
                                }
                                pop()
                                lastIndex = match.range.last + 1
                            }
                            append(instruction.substring(lastIndex))
                        }

                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.Center
                        ) {
                            ClickableText(
                                modifier = Modifier.padding(bottom = 120.dp, top = 32.dp),
                                text = annotatedText,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = MaterialTheme.typography.displaySmall.fontSize * textSizeMultiplier,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                onClick = { offset ->
                                    annotatedText.getStringAnnotations("TIMER", offset, offset).firstOrNull()?.let {
                                        val match = timeRegex.find(it.item)
                                        match?.let { m ->
                                            selectedTimerDuration = parseToSeconds(m.groupValues[1], m.groupValues[2])
                                            showTimerDialog = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            confirmButton = {
                Button(onClick = {
                    com.seyone22.cook.service.TimerService.start(context, selectedTimerDuration)
                    showTimerDialog = false
                }) { Text("Start") }
            },
            dismissButton = { TextButton(onClick = { showTimerDialog = false }) { Text("Cancel") } },
            title = { Text("Start Timer?") },
            text = { Text("${selectedTimerDuration / 60} minutes will be added to your timers.") }
        )
    }

    if (showDashboard) {
        ModalBottomSheet(
            onDismissRequest = { showDashboard = false },
            sheetState = sheetState
        ) {
            DashboardContent(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                ingredients = ingredientsForRecipe,
                checkedIds = checkedIngredients,
                onToggleIngredient = { viewModel.toggleIngredient(it) },
                timeLeft = timeLeft,
                onStopTimer = { com.seyone22.cook.service.TimerService.stop(context) },
                viewModel = viewModel,
                onScaleClick = {
                    // We don't necessarily have to close the dashboard,
                    // the dialog will appear over it.
                    showScaleDialog = true
                }
            )
        }
    }
}

@Composable
fun DashboardContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    ingredients: List<RecipeIngredient?>,
    checkedIds: Set<Long>,
    onToggleIngredient: (Long) -> Unit,
    timeLeft: Long?,
    onStopTimer: () -> Unit,
    viewModel: CookingViewModel,
    onScaleClick: () -> Unit // Add this
) {
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { onTabSelected(0) }, text = { Text("Ingredients") }, icon = { Icon(Icons.Default.List, null) })
            Tab(selected = selectedTab == 1, onClick = { onTabSelected(1) }, text = { Text("Timers") }, icon = { Icon(Icons.Default.Timer, null) })
            Tab(selected = selectedTab == 2, onClick = { onTabSelected(2) }, text = { Text("Units") }, icon = { Icon(Icons.Default.Settings, null) })
        }

        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 500.dp).padding(16.dp)) {
            when (selectedTab) {
                0 -> IngredientTabContent(
                    ingredients = ingredients,
                    checkedIds = checkedIds,
                    onToggle = onToggleIngredient,
                    viewModel = viewModel,
                    onScaleClick = onScaleClick // Pass it down
                )
                1 -> TimerTabContent(timeLeft, onStopTimer)
                2 -> {
                    val isMetric by viewModel.isMetric.collectAsStateWithLifecycle()

                    // 2. Pass state and the toggle function to the tab
                    UnitTabContent(
                        isMetric = isMetric,
                        onToggle = { enabled -> viewModel.toggleUnitSystem(enabled) }
                    )
                }            }
        }
    }
}

@Composable
fun IngredientTabContent(
    ingredients: List<RecipeIngredient?>,
    checkedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    viewModel: CookingViewModel,
    onScaleClick: () -> Unit // New callback
) {
    val isMetric by viewModel.isMetric.collectAsStateWithLifecycle()
    // Observe the scale factor from the ViewModel
    val scaleFactor by viewModel.scaleFactor.collectAsStateWithLifecycle()

    LazyColumn {
        // --- SCALE INDICATOR HEADER ---
        item {
            Surface(
                // Make the header interactive
                onClick = onScaleClick,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Current Scale: ${if (scaleFactor % 1.0 == 0.0) scaleFactor.toInt() else scaleFactor}x",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Tap to adjust quantities",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- INGREDIENTS LIST ---
        items(items = ingredients) { ingredient: RecipeIngredient? ->
            ingredient?.let { item ->
                val isChecked = checkedIds.contains(item.id)

                // Now passes both metric preference and scale factor for processing
                val displayInfo = viewModel.formatIngredient(item, isMetric, scaleFactor)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onToggle(item.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggle(item.id) }
                    )
                    Text(
                        text = displayInfo,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TimerTabContent(timeLeft: Long?, onStop: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (timeLeft != null) {
            Text("Active Timer", style = MaterialTheme.typography.titleMedium)
            Text(String.format("%02d:%02d", timeLeft / 60, timeLeft % 60), style = MaterialTheme.typography.displayMedium)
            Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Stop Timer") }
        } else { Text("No active timers") }
    }
}

@Composable
fun UnitTabContent(
    isMetric: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column {
        ListItem(
            headlineContent = { Text("Measurement System") },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Imperial", style = MaterialTheme.typography.labelSmall)
                    Switch(
                        checked = isMetric,
                        onCheckedChange = { onToggle(it) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text("Metric", style = MaterialTheme.typography.labelSmall)
                }
            }
        )
        Text(
            text = "Conversions are approximate based on standard kitchen scales.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun parseToSeconds(value: String, unit: String): Long {
    val amount = value.toLong()
    return if (unit.lowercase().startsWith("h")) amount * 3600 else amount * 60
}