package com.imran.pocketledger.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.groupByMonth
import com.imran.pocketledger.model.sampleExpenses
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.time.format.DateTimeFormatter

private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

@Composable
fun ExpenseListScreen(expenses: List<Expense>, modifier: Modifier = Modifier) {
    val total = expenses.sumOf { it.amount }
    val byMonth = groupByMonth(expenses)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item(key = "total") {
            TotalHeader(total = formatMoney(total))
        }

        byMonth.forEach { (month, monthExpenses) ->
            item(key = month.toString()) {
                Text(
                    text = month.format(monthFormatter),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                )
            }
            items(monthExpenses, key = { it.id }) { expense ->
                ExpenseRow(expense = expense)
            }
        }
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
        ExpenseListScreen(expenses = sampleExpenses)
    }
}
