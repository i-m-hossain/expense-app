package com.imran.pocketledger.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.sampleExpenses
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

private val rowDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")

fun formatMoney(amount: BigDecimal): String =
    NumberFormat.getCurrencyInstance().format(amount)

@Composable
fun ExpenseRow(expense: Expense, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "${expense.category.label} · ${expense.date.format(rowDateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = formatMoney(expense.amount),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseRowPreview() {
    PocketLedgerTheme {
        ExpenseRow(expense = sampleExpenses.first())
    }
}
