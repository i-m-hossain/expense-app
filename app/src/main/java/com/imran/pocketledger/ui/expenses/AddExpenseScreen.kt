package com.imran.pocketledger.ui.expenses

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.imran.pocketledger.model.Category
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.ExpenseValidation
import com.imran.pocketledger.model.validateExpense
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val formDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")

/**
 * Stateful wrapper: owns the form state and decides what happens on Save.
 * rememberSaveable keeps every field across rotation.
 */
@Composable
fun AddExpenseScreen(
    nextId: Long,
    onSave: (Expense) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(Category.FOOD) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val validation = validateExpense(nextId, title, amountText, category, date)
    // Only nag the user about errors after they've tried to save once.
    val errors = (validation as? ExpenseValidation.Invalid).takeIf { submitted }

    AddExpenseForm(
        title = title,
        onTitleChange = { title = it },
        amountText = amountText,
        onAmountChange = { amountText = it },
        category = category,
        onCategoryChange = { category = it },
        date = date,
        onDateChange = { date = it },
        titleError = errors?.titleError,
        amountError = errors?.amountError,
        onSaveClick = {
            submitted = true
            when (validation) {
                is ExpenseValidation.Valid -> onSave(validation.expense)
                is ExpenseValidation.Invalid -> Unit // errors are now shown
            }
        },
        onCancelClick = onCancel,
        modifier = modifier,
    )
}

/** Stateless form: everything comes in as parameters, every change goes out as a callback. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseForm(
    title: String,
    onTitleChange: (String) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit,
    category: Category,
    onCategoryChange: (Category) -> Unit,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    titleError: String?,
    amountError: String?,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            singleLine = true,
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        CategoryDropdown(selected = category, onSelected = onCategoryChange)

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Date: ${date.format(formDateFormatter)}")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
        ) {
            TextButton(onClick = onCancelClick) { Text("Cancel") }
            Button(onClick = onSaveClick) { Text("Save") }
        }
    }

    if (showDatePicker) {
        ExpenseDatePickerDialog(
            initialDate = date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onDateChange(it)
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
            title = "",
            onTitleChange = {},
            amountText = "12.345",
            onAmountChange = {},
            category = Category.FOOD,
            onCategoryChange = {},
            date = LocalDate.of(2026, 11, 16),
            onDateChange = {},
            titleError = "Title is required",
            amountError = "Enter a positive amount with at most 2 decimals",
            onSaveClick = {},
            onCancelClick = {},
        )
    }
}
