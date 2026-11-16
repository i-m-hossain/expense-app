package com.imran.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.imran.pocketledger.model.sampleExpenses
import com.imran.pocketledger.ui.expenses.ExpenseListScreen
import com.imran.pocketledger.ui.theme.PocketLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketLedgerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExpenseListScreen(expenses = sampleExpenses, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


