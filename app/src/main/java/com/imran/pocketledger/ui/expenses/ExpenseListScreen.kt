package com.imran.pocketledger.ui.expenses

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imran.pocketledger.AppContainer
import com.imran.pocketledger.model.groupByMonth
import com.imran.pocketledger.model.sampleExpenses
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.time.format.DateTimeFormatter

private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/** Route: connects the ViewModel to the screen. The only place that knows a ViewModel exists. */
@Composable
fun ExpenseListRoute(
    modifier: Modifier = Modifier,
    viewModel: ExpenseListViewModel = viewModel { ExpenseListViewModel(AppContainer.expenseRepository) },
) {
    // Like useSelector: re-renders when the state changes; stops collecting while the app is in the background.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExpenseListScreen(uiState = uiState, modifier = modifier)
}

/** Pure function of state: no ViewModel, no business logic, easy to preview. */
@Composable
fun ExpenseListScreen(uiState: ExpenseListUiState, modifier: Modifier = Modifier) {
    when (uiState) {
        ExpenseListUiState.Loading -> CenteredBox(modifier) { CircularProgressIndicator() }
        ExpenseListUiState.Empty -> CenteredBox(modifier) {
            Text("No expenses yet. Tap \"+ Add expense\" to start.")
        }
        is ExpenseListUiState.Error -> CenteredBox(modifier) {
            Text(uiState.message, color = MaterialTheme.colorScheme.error)
        }
        is ExpenseListUiState.Success -> ExpenseList(uiState, modifier)
    }
}

@Composable
private fun ExpenseList(state: ExpenseListUiState.Success, modifier: Modifier = Modifier) {
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
            items(section.expenses, key = { it.id }) { expense ->
                ExpenseRow(expense = expense)
            }
        }
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
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseListEmptyPreview() {
    PocketLedgerTheme {
        ExpenseListScreen(uiState = ExpenseListUiState.Empty)
    }
}
