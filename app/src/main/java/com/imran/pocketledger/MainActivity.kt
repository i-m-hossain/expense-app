package com.imran.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.imran.pocketledger.model.ExpenseId
import com.imran.pocketledger.navigation.ExpenseDetail
import com.imran.pocketledger.navigation.ExpenseForm
import com.imran.pocketledger.navigation.ExpenseList
import com.imran.pocketledger.ui.expenses.AddExpenseRoute
import com.imran.pocketledger.ui.expenses.ExpenseDetailRoute
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
    // Owns the back stack (like React Router's history). Survives rotation and process death.
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ExpenseList) {
        composable<ExpenseList> {
            ExpenseListRoute(
                onAddClick = { navController.navigate(ExpenseForm()) },
                onExpenseClick = { id -> navController.navigate(ExpenseDetail(id.value)) },
            )
        }
        composable<ExpenseDetail> { backStackEntry ->
            // toRoute() turns the saved back-stack entry back into our typed route object.
            val route = backStackEntry.toRoute<ExpenseDetail>()
            ExpenseDetailRoute(
                expenseId = ExpenseId(route.id),
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(ExpenseForm(expenseId = id.value)) },
            )
        }
        composable<ExpenseForm> { backStackEntry ->
            val route = backStackEntry.toRoute<ExpenseForm>()
            // viewModel() inside a destination is scoped to that back-stack entry:
            // a fresh ViewModel each time the form opens, cleared when it's popped.
            AddExpenseRoute(
                expenseId = route.expenseId?.let(::ExpenseId),
                onClose = { navController.popBackStack() },
            )
        }
    }
}
