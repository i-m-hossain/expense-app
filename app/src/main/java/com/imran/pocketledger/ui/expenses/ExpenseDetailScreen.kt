package com.imran.pocketledger.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imran.pocketledger.AppContainer
import com.imran.pocketledger.R
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.ExpenseId
import com.imran.pocketledger.model.sampleExpenses
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.time.format.DateTimeFormatter

private val detailDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

@Composable
fun ExpenseDetailRoute(
    expenseId: ExpenseId,
    onBack: () -> Unit,
    onEdit: (ExpenseId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpenseDetailViewModel = viewModel { ExpenseDetailViewModel(expenseId, AppContainer.expenseRepository) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExpenseDetailScreen(uiState = uiState, onBack = onBack, onEdit = onEdit, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    uiState: ExpenseDetailUiState,
    onBack: () -> Unit,
    onEdit: (ExpenseId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is ExpenseDetailUiState.Found) {
                        TextButton(onClick = { onEdit(uiState.expense.id) }) { Text("Edit") }
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (uiState) {
                ExpenseDetailUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                ExpenseDetailUiState.NotFound ->
                    Text("This expense no longer exists.", Modifier.align(Alignment.Center))
                is ExpenseDetailUiState.Found ->
                    ExpenseDetails(uiState.expense)
            }
        }
    }
}

@Composable
private fun ExpenseDetails(expense: Expense, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(expense.title, style = MaterialTheme.typography.headlineSmall)
        Text(formatMoney(expense.amount), style = MaterialTheme.typography.displaySmall)
        Text(
            text = "${expense.category.label} · ${expense.date.format(detailDateFormatter)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseDetailScreenPreview() {
    PocketLedgerTheme {
        ExpenseDetailScreen(
            uiState = ExpenseDetailUiState.Found(sampleExpenses.first()),
            onBack = {},
            onEdit = {},
        )
    }
}
