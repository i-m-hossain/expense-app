package com.imran.pocketledger.ui.expenses

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imran.pocketledger.AppContainer
import com.imran.pocketledger.model.Category
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")

/** Route: wires the ViewModel to the stateless form, and turns one-off effects into navigation. */
@Composable
fun AddExpenseRoute(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddExpenseViewModel = viewModel { AddExpenseViewModel(AppContainer.expenseRepository) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)

    // Like useEffect(() => ..., []): starts once, cancelled when this composable leaves the screen.
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AddExpenseEffect.Close -> currentOnClose()
            }
        }
    }

    // System back goes through Cancel, so it's ignored while a save is running.
    BackHandler { viewModel.onEvent(AddExpenseEvent.Cancel) }

    // Scaffold here just keeps the form clear of the status and navigation bars.
    Scaffold(modifier = modifier) { innerPadding ->
        AddExpenseForm(
            state = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

/** Stateless form: renders `state`, reports every user action as an event. */
@Composable
fun AddExpenseForm(
    state: AddExpenseUiState,
    onEvent: (AddExpenseEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Whether the dialog is open is pure UI state, so it stays here, not in the ViewModel.
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Add expense", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(AddExpenseEvent.TitleChanged(it)) },
            label = { Text("Title") },
            singleLine = true,
            isError = state.titleError != null,
            supportingText = state.titleError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.amountText,
            onValueChange = { onEvent(AddExpenseEvent.AmountChanged(it)) },
            label = { Text("Amount") },
            singleLine = true,
            isError = state.amountError != null,
            supportingText = state.amountError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        CategoryDropdown(
            selected = state.category,
            onSelected = { onEvent(AddExpenseEvent.CategoryChanged(it)) },
        )

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Date: ${state.date.format(formDateFormatter)}")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
        ) {
            TextButton(
                onClick = { onEvent(AddExpenseEvent.Cancel) },
                enabled = !state.isSaving,
            ) { Text("Cancel") }
            Button(
                onClick = { onEvent(AddExpenseEvent.Save) },
                enabled = !state.isSaving,
            ) {
                Text(if (state.isSaving) "Saving…" else "Save")
            }
        }
    }

    if (showDatePicker) {
        ExpenseDatePickerDialog(
            initialDate = state.date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onEvent(AddExpenseEvent.DateChanged(it))
                showDatePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selected: Category,
    onSelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Category.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    // DatePicker works in UTC epoch millis, so convert to/from LocalDate at the edges.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis
                        ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                        ?.also(onConfirm)
                        ?: onDismiss()
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}

@Preview(showBackground = true)
@Composable
fun AddExpenseFormPreview() {
    PocketLedgerTheme {
        AddExpenseForm(
            state = AddExpenseUiState(
                amountText = "12.345",
                date = LocalDate.of(2026, 11, 16),
                titleError = "Title is required",
                amountError = "Enter a positive amount with at most 2 decimals",
            ),
            onEvent = {},
        )
    }
}
