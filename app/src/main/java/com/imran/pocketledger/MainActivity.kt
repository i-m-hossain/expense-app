package com.imran.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.imran.pocketledger.navigation.AddExpense
import com.imran.pocketledger.navigation.ExpenseList
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
    // Owns the back stack (like React Router's history). Survives rotation automatically.
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ExpenseList) {
        composable<ExpenseList> {
            ExpenseListRoute(onAddClick = { navController.navigate(AddExpense) })
        }
        composable<AddExpense> {
            // viewModel() inside a destination is scoped to that back-stack entry:
            // a fresh AddExpenseViewModel each time the form opens, cleared when it's popped.
            AddExpenseRoute(onClose = { navController.popBackStack() })
        }
    }
}
