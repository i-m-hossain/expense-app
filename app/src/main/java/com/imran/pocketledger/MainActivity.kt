package com.imran.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.sampleExpenses
import com.imran.pocketledger.ui.expenses.AddExpenseScreen
import com.imran.pocketledger.ui.expenses.ExpenseListScreen
import com.imran.pocketledger.ui.theme.PocketLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketLedgerTheme {
                PocketLedgerApp()
            }
        }
    }
}

@Composable
fun PocketLedgerApp() {
    // Hoisted state: the parent owns the list; children get values + callbacks.
    // Like React: an immutable list, replaced with a new one on every change.
    // NOTE: `remember` (not saveable) → added expenses are lost on rotation. M3's ViewModel fixes this.
    var expenses by remember { mutableStateOf(sampleExpenses) }
    var showForm by rememberSaveable { mutableStateOf(false) }

    // System back closes the form instead of exiting the app.
    BackHandler(enabled = showForm) { showForm = false  }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (!showForm) {
                ExtendedFloatingActionButton(onClick = { showForm = true }) {
                    Text("+ Add expense")
                }
            }
        },
    ) { innerPadding ->
        if (showForm) {
            AddExpenseScreen(
                nextId = (expenses.maxOfOrNull { it.id } ?: 0L) + 1,
                onSave = { expense: Expense ->
                    expenses = expenses + expense
                    showForm = false
                },
                onCancel = { showForm = false },
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            ExpenseListScreen(
                expenses = expenses,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
