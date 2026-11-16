package com.imran.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.imran.pocketledger.ui.expenses.AddExpenseRoute
import com.imran.pocketledger.ui.expenses.ExpenseListRoute
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
    // The expense list now lives in the repository (via ViewModels), not here.
    // Only "which screen is showing" remains; M4 replaces this flag with Navigation.
    var showForm by rememberSaveable { mutableStateOf(false) }

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
            AddExpenseRoute(
                onClose = { showForm = false },
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            ExpenseListRoute(modifier = Modifier.padding(innerPadding))
        }
    }
}
