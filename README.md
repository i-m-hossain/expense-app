# PocketLedger

An Android expense tracker, built step by step to learn Kotlin and Jetpack Compose.
The milestones and progress are tracked in [Learning_plan.md](Learning_plan.md).

> **Status:** M0–M4 complete; next is M5 (Hilt & modularization). Data is kept **in memory** and resets
> when the app process is killed. Saving to a database comes in M6 (Room).

## Features

- **Expense list** grouped by month, newest first, with a total at the top
- **Add an expense:** tap **+ Add expense**, fill in title, amount, category and date
- **View an expense:** tap a row to open its detail screen
- **Edit an expense:** on the detail screen, tap **Edit**
- **Delete an expense:** on the list, **swipe a row from right to left**, then tap **Undo**
  in the snackbar if it was a mistake
- **Validation:** title required; amount must be positive with at most 2 decimals
- Form input survives screen rotation and the app being killed in the background

## Tech stack

| Area | Library |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3 |
| State | ViewModel, `StateFlow`, coroutines |
| Navigation | Navigation Compose with type-safe `@Serializable` routes |
| Tests | JUnit 4, `kotlinx-coroutines-test` |
| Build | Gradle (Kotlin DSL), version catalog in `gradle/libs.versions.toml` |

Min SDK 26, target SDK 37.

## Architecture

MVVM with unidirectional data flow: the ViewModel exposes one `StateFlow<UiState>`,
the screen draws it, and user actions go back to the ViewModel as events.

```
Screen (Compose)  ──events──►  ViewModel  ──►  ExpenseRepository (interface)
       ▲                           │                    │
       └──────── UiState ──────────┘          InMemoryExpenseRepository
```

Each screen has two composables:
- `…Route` gets the ViewModel and collects its state
- `…Screen` / `…Form` only draws the state it's given (easy to preview and test)

## Project structure

```
app/src/main/java/com/imran/pocketledger/
├── MainActivity.kt          NavHost: which screen shows for which route
├── AppContainer.kt          hand-made dependency container (Hilt replaces it in M5)
├── navigation/
│   └── Destinations.kt      type-safe routes: ExpenseList, ExpenseDetail, ExpenseForm
├── model/
│   ├── Expense.kt           Expense, ExpenseId, Category
│   ├── ExpenseValidation.kt input parsing and validation
│   └── SampleData.kt        starting data + groupByMonth()
├── data/
│   ├── ExpenseRepository.kt          the interface everything depends on
│   └── InMemoryExpenseRepository.kt  current implementation
└── ui/expenses/
    ├── ExpenseListScreen.kt / ExpenseListViewModel.kt
    ├── ExpenseDetailScreen.kt / ExpenseDetailViewModel.kt
    ├── AddExpenseScreen.kt / AddExpenseViewModel.kt   (used for both add and edit)
    └── ExpenseRow.kt
```

## Getting started

1. Open the project in **Android Studio** (on Windows, keep it on the Windows filesystem, e.g. `C:\dev\pocketledger`).
2. Let Gradle sync finish.
3. Pick an emulator or device and press **Run**.

## Running tests

Unit tests live in `app/src/test/`. Run them from Android Studio (right-click the `test` folder → **Run Tests**) or:

```
./gradlew testDebugUnitTest
```

## Testing process death manually

1. Enable **Settings → Developer options → Don't keep activities**.
2. Open the add/edit form and type something.
3. Press Home, then return to the app. The form and your input should still be there.
4. Turn the setting off again afterwards.
