package com.imran.pocketledger.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imran.pocketledger.AppContainer
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.ExpenseId
import com.imran.pocketledger.model.groupByMonth
import com.imran.pocketledger.model.sampleExpenses
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.time.format.DateTimeFormatter

private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/** Route: connects the ViewModel to the screen. The only place that knows a ViewModel exists. */
@Composable
fun ExpenseListRoute(
    onAddClick: () -> Unit,
    onExpenseClick: (ExpenseId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseListViewModel = viewModel { ExpenseListViewModel(AppContainer.expenseRepository) },
) {
    // Like useSelector: re-renders when the state changes; stops collecting while the app is in the background.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExpenseListEffect.ShowUndoDelete -> {
                    // showSnackbar suspends until the snackbar is dismissed or its action is tapped.
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted \"${effect.expense.title}\"",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete(effect.expense)
                    }
                }
            }
        }
    }

    ExpenseListScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAddClick = onAddClick,
        onExpenseClick = onExpenseClick,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

/** Pure function of state: no ViewModel, no business logic, easy to preview. */
@Composable
fun ExpenseListScreen(
    uiState: ExpenseListUiState,
    snackbarHostState: SnackbarHostState,
    onAddClick: () -> Unit,
    onExpenseClick: (ExpenseId) -> Unit,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Each screen owns its Scaffold, so the FAB and snackbar live with the screen that uses them.
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddClick) { Text("+ Add expense") }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (uiState) {
            ExpenseListUiState.Loading -> CenteredBox(contentModifier) { CircularProgressIndicator() }
            ExpenseListUiState.Empty -> CenteredBox(contentModifier) {
                Text("No expenses yet. Tap \"+ Add expense\" to start.")
            }
            is ExpenseListUiState.Error -> CenteredBox(contentModifier) {
                Text(uiState.message, color = MaterialTheme.colorScheme.error)
            }
            is ExpenseListUiState.Success -> ExpenseList(
                state = uiState,
                onExpenseClick = onExpenseClick,
                onDelete = onDelete,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun ExpenseList(
    state: ExpenseListUiState.Success,
    onExpenseClick: (ExpenseId) -> Unit,
    onDelete: (Expense) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 88.dp), // room so the FAB doesn't cover the last row
    ) {
        item(key = "total") {
            TotalHeader(total = formatMoney(state.total))
        }

        state.months.forEach { section ->
            item(key = section.month.toString()) {
                Text(
                    text = section.month.format(monthFormatter),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            // Keys must be Bundle-friendly, so use the raw Long inside the value class.
            items(section.expenses, key = { it.id.value }) { expense ->
                SwipeToDeleteRow(
                    expense = expense,
                    onClick = { onExpenseClick(expense.id) },
                    onDelete = { onDelete(expense) },
                )
            }
        }
    }
}

/** Swipe a row right-to-left to delete it. */
@Composable
private fun SwipeToDeleteRow(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val swipeState = rememberSwipeToDismissBoxState()
    val currentOnDelete by rememberUpdatedState(onDelete)

    // Runs whenever the swipe settles; when it settles fully swiped, delete.
    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            currentOnDelete()
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
    ) {
        ExpenseRow(
            expense = expense,
            // Background first so the red "Delete" layer is hidden until you swipe.
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick),
        )
    }
}

@Composable
private fun CenteredBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
private fun TotalHeader(total: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Total spent",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = total,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun ExpenseListScreenPreview() {
    PocketLedgerTheme {
        ExpenseListScreen(
            uiState = ExpenseListUiState.Success(
                total = sampleExpenses.sumOf { it.amount },
                months = groupByMonth(sampleExpenses).map { (month, items) -> MonthSection(month, items) },
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAddClick = {},
            onExpenseClick = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseListEmptyPreview() {
    PocketLedgerTheme {
        ExpenseListScreen(
            uiState = ExpenseListUiState.Empty,
            snackbarHostState = remember { SnackbarHostState() },
            onAddClick = {},
            onExpenseClick = {},
            onDelete = {},
        )
    }
}
