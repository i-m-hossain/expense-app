package com.imran.pocketledger.ui.expenses

import androidx.lifecycle.SavedStateHandle
import com.imran.pocketledger.MainDispatcherRule
import com.imran.pocketledger.data.ExpenseRepository
import com.imran.pocketledger.model.Category
import com.imran.pocketledger.model.Expense
import com.imran.pocketledger.model.ExpenseId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

/** A fake (not a mock): a tiny real implementation we fully control. */
private class FakeExpenseRepository(initial: List<Expense> = emptyList()) : ExpenseRepository {
    val stored = initial.toMutableList()
    val added = mutableListOf<Expense>()
    val updated = mutableListOf<Expense>()

    /** If set, add() waits until the test completes it, so we can act "during" a save. */
    var gate: CompletableDeferred<Unit>? = null

    override val expenses: Flow<List<Expense>> = MutableStateFlow(initial)

    override suspend fun get(id: ExpenseId): Expense? = stored.find { it.id == id }

    override suspend fun add(expense: Expense) {
        gate?.await()
        added += expense
    }

    override suspend fun update(expense: Expense) {
        updated += expense
    }

    override suspend fun delete(id: ExpenseId) {
        stored.removeAll { it.id == id }
    }

    override suspend fun restore(expense: Expense) {
        stored += expense
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val existing = Expense(
        id = ExpenseId(5),
        title = "Cinema",
        amount = BigDecimal("14.50"),
        category = Category.ENTERTAINMENT,
        date = LocalDate.of(2026, 11, 14),
    )
    private val repository = FakeExpenseRepository(initial = listOf(existing))

    /** Created lazily so the main-dispatcher rule is already in place. */
    private val viewModel by lazy { newViewModel() }

    private fun newViewModel(
        expenseId: ExpenseId? = null,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = AddExpenseViewModel(expenseId, savedStateHandle, repository)

    private fun AddExpenseViewModel.fillValidForm() {
        onEvent(AddExpenseEvent.TitleChanged("Lunch"))
        onEvent(AddExpenseEvent.AmountChanged("12.50"))
    }

    @Test
    fun `errors are hidden until the first save attempt`() {
        viewModel.onEvent(AddExpenseEvent.AmountChanged("abc"))

        assertEquals(null, viewModel.uiState.value.amountError)
    }

    @Test
    fun `saving an invalid form shows errors and saves nothing`() {
        viewModel.onEvent(AddExpenseEvent.Save)

        val state = viewModel.uiState.value
        assertEquals("Title is required", state.titleError)
        assertEquals("Amount is required", state.amountError)
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `saving a valid form adds it and closes once`() = runTest {
        val effects = mutableListOf<AddExpenseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }

        viewModel.fillValidForm()
        viewModel.onEvent(AddExpenseEvent.Save)

        assertEquals("Lunch", repository.added.single().title)
        assertEquals(listOf(AddExpenseEffect.Close), effects)
    }

    @Test
    fun `cancel while saving is ignored, so the form closes only once`() = runTest {
        val effects = mutableListOf<AddExpenseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.effects.toList(effects) }
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        viewModel.fillValidForm()
        viewModel.onEvent(AddExpenseEvent.Save)
        assertTrue(viewModel.uiState.value.isSaving)

        viewModel.onEvent(AddExpenseEvent.Cancel) // user taps back mid-save
        assertTrue(effects.isEmpty())

        gate.complete(Unit) // the save finishes
        advanceUntilIdle()

        assertEquals(listOf(AddExpenseEffect.Close), effects)
        assertEquals(1, repository.added.size)
    }

    @Test
    fun `editing prefills the form and saves with update, not add`() {
        val editViewModel = newViewModel(expenseId = existing.id)

        val state = editViewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("Cinema", state.title)
        assertEquals("14.50", state.amountText)

        editViewModel.onEvent(AddExpenseEvent.TitleChanged("Cinema + popcorn"))
        editViewModel.onEvent(AddExpenseEvent.Save)

        assertEquals(existing.copy(title = "Cinema + popcorn"), repository.updated.single())
        assertTrue(repository.added.isEmpty())
    }

    @Test
    fun `draft survives process death via SavedStateHandle`() {
        val handle = SavedStateHandle()
        val before = newViewModel(savedStateHandle = handle)
        before.onEvent(AddExpenseEvent.TitleChanged("Taxi"))
        before.onEvent(AddExpenseEvent.AmountChanged("9.00"))
        before.onEvent(AddExpenseEvent.CategoryChanged(Category.TRANSPORT))

        // Android kills the app; a brand-new ViewModel gets the same saved handle back.
        val after = newViewModel(savedStateHandle = handle)

        val state = after.uiState.value
        assertEquals("Taxi", state.title)
        assertEquals("9.00", state.amountText)
        assertEquals(Category.TRANSPORT, state.category)
    }
}
