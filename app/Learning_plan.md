# PocketLedger — Learn Kotlin & Android by Building

**Learner:** Java + Spring Boot + React experience, new to Kotlin and Android.
**Approach:** Project-first. No separate "theory" phase. Each milestone ships a working
feature, and every Kotlin/Android concept is introduced *at the moment the project needs it*.
**Pace:** ~1 milestone per week (8–10 hrs) → ~11 weeks.

---

## 1. The App

**PocketLedger**: an offline-first expense tracker that syncs to a REST backend.

Final feature set:
- Expense list grouped by month, add / edit / delete, categories
- Monthly summary (totals per category)
- Works fully offline; syncs when the network is back (background sync)
- Login with token-based auth
- Settings: currency, dark/light theme
- Signed, minified release build produced by CI, with crash reporting

Backend: optional **Spring Boot + Kotlin** service you write in Milestone 7 (recommended,
since you already know Spring), or a mock server.

---

## 2. Your Bridges: React → Compose, Spring → Android

| React | Compose / Android |
|---|---|
| Function component | `@Composable` function |
| Props | Function parameters |
| `useState` | `remember { mutableStateOf(...) }` |
| Lifting state up | State hoisting |
| `useEffect` | `LaunchedEffect`, `DisposableEffect` |
| `useMemo` | `remember(key) { ... }`, `derivedStateOf` |
| `array.map(x => <Row/>)` | `LazyColumn { items(list) { Row(it) } }` |
| `useReducer` / Redux store | `ViewModel` + `StateFlow<UiState>` + events |
| Context API | `CompositionLocal` (UI), Hilt (dependencies) |
| React Router | Navigation Compose (type-safe routes) |
| React Query cache | Repository + Room as the single source of truth |
| axios interceptors | OkHttp interceptors |
| localStorage | DataStore |
| CSS / styled-components | `Modifier` chains + Material 3 theme |
| Re-render | Recomposition |
| Jest + RTL | JUnit + MockK + Compose UI tests |

| Spring | Android |
|---|---|
| `@Autowired` / IoC container | Hilt `@Inject` |
| JPA Repository / Entity | Room DAO / `@Entity` |
| Flyway migrations | Room `Migration` |
| `RestTemplate` / `WebClient` | Retrofit / Ktor Client |
| `@Async`, `CompletableFuture`, Reactor | Coroutines, `Flow` |
| `@Scheduled` | WorkManager |
| `application-{profile}.yml` | Build types / product flavors |
| Lombok `@Data` | `data class` |

---

## 3. Target Architecture (reached gradually, not on day 1)

```
app/                      ← Application, MainActivity, NavHost, Hilt entry point
core/
  model/                  ← pure Kotlin domain models
  data/                   ← repositories (offline-first)
  database/               ← Room
  network/                ← Retrofit + DTOs
  datastore/              ← user preferences, auth token
  designsystem/           ← theme + shared composables
  testing/                ← fakes, test dispatchers
feature/
  expenses/  summary/  settings/  auth/
```

You start with one `app` module and **split into modules in M5**, once you feel why it's needed.

---

## 4. Milestones

Each milestone uses the same format: **Build**, **Kotlin you'll learn**, **Android you'll learn**,
**Bridge**, **Done when**.

### M0 — Setup & Hello Kotlin (2–3 days)
- **Build:** new project from the "Empty Activity (Compose)" template, running on emulator/device; git init
- **Kotlin:** `val`/`var`, type inference, functions, string templates, null safety (`?`, `?.`, `?:`), `if`/`when` as expressions
- **Android:** project anatomy, `AndroidManifest.xml`, `MainActivity`, Gradle Kotlin DSL, `libs.versions.toml`
- **Bridge:** `libs.versions.toml` ≈ `package.json` dependencies; `MainActivity.setContent {}` ≈ `ReactDOM.createRoot().render()`
- **Done when:** app runs and shows a greeting built from a Kotlin function

### M1 — Expense List UI, static data (Top 10: #1, #3)
- **Build:** `Expense` model, hard-coded list, `ExpenseListScreen`, `ExpenseRow`, Material 3 theme
- **Kotlin:** `data class`, `enum class`, `List`/`listOf`, lambdas, trailing-lambda syntax, collection ops (`map`, `filter`, `groupBy`, `sumOf`), `BigDecimal` for money, `java.time` usage from Kotlin
- **Android:** composables, `Modifier`, `Column`/`Row`/`Box`, `LazyColumn`, `Scaffold`, `@Preview`
- **Bridge:** components = composables, props = params, `.map()` rendering = `items()`
- **Done when:** a scrollable list grouped by month with a total header

### M2 — Add Expense Form & Local State (#1, #3)
- **Build:** add-expense screen: amount, title, category dropdown, date picker, validation
- **Kotlin:** `sealed interface` for validation results, exhaustive `when`, extension functions (e.g. `String.toAmountOrNull()`), scope functions (`let`, `apply`, `also`), default & named arguments
- **Android:** `remember`, `mutableStateOf`, `rememberSaveable`, `TextField`, state hoisting, recomposition basics
- **Bridge:** controlled inputs = `TextField(value, onValueChange)`; lifting state up = hoisting
- **Done when:** a valid expense appears in the list; an invalid one shows field errors; rotating the device keeps form input

### M3 — ViewModel, Coroutines & Unidirectional Data Flow (#2, #4)
- **Build:** `ExpenseListViewModel` + `AddExpenseViewModel`, in-memory repository behind an interface
- **Kotlin:** coroutines (`suspend`, `launch`, `viewModelScope`), `StateFlow`/`MutableStateFlow`, `update {}`, sealed `UiState` (Loading / Success / Empty / Error), one-off events, `object`, interfaces
- **Android:** `ViewModel`, `collectAsStateWithLifecycle`, lifecycle awareness
- **Bridge:** `useReducer` → ViewModel; dispatch(action) → `viewModel.onEvent(event)`
- **Done when:** the UI is a pure function of `UiState`; no business logic in composables

### M4 — Navigation & Process Death (#8)
- **Build:** list → add/edit → detail navigation, edit an existing expense, delete with an undo snackbar
- **Kotlin:** `@Serializable` route classes, `value class` for `ExpenseId`, nullability around arguments
- **Android:** Navigation Compose (type-safe routes), back stack, `SavedStateHandle`, config change vs process death (test with "Don't keep activities")
- **Bridge:** React Router routes/params → `NavHost` + `@Serializable` route objects
- **Done when:** edit flow works and survives process death

### M5 — Dependency Injection & Modularization (#4, #5)
- **Build:** add Hilt; split into `core/*` and `feature/expenses` modules
- **Kotlin:** `internal` visibility, interface + impl patterns, generics basics, `lateinit` vs `by lazy`
- **Android:** `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Module`/`@Binds`/`@Provides`, scopes, Gradle convention plugins (light version)
- **Bridge:** this is Spring DI with annotation processing at compile time (KSP) instead of reflection at runtime
- **Done when:** no `ViewModel` constructs its own dependencies; feature modules depend only on `core` interfaces

### M6 — Room: Offline Persistence (#7, #2)
- **Build:** persist expenses + categories; summary screen with totals per category per month
- **Kotlin:** `Flow` (cold streams), operators `map`/`combine`/`flatMapLatest`/`stateIn`, `withContext(Dispatchers.IO)`, annotations & KSP
- **Android:** `@Entity`, `@Dao`, `@Database`, relations, DAO returning `Flow`, schema export + migrations
- **Bridge:** JPA entities/repositories → Room; Flyway → Room `Migration`
- **Done when:** data survives app restarts; the summary updates live when an expense changes

### M7 — Networking & Sync (#6, #7)
- **Build:** (optional) a Spring Boot **Kotlin** backend for `/expenses`; Retrofit client; offline-first repository with a `syncStatus` column
- **Kotlin:** `kotlinx.serialization`, DTO ↔ domain mapping with extension functions, `Result`/sealed `AppError`, `runCatching` + the `CancellationException` trap, `async`/`awaitAll` for parallel calls
- **Android:** Retrofit + OkHttp, timeouts, logging interceptor (debug only), `INTERNET` permission, connectivity
- **Bridge:** React Query stale-while-revalidate ≈ "read from Room, refresh from network, write to Room"
- **Done when:** create offline → syncs later; network errors appear as user-friendly UI state, never crashes

### M8 — Auth & Settings (#6, #7, #10-security)
- **Build:** login screen, token storage, auth interceptor, 401 → logout; settings for currency + theme
- **Kotlin:** `Flow` from DataStore, `first()`, delegation (`by`), `companion object` constants
- **Android:** Preferences DataStore, secure token handling (Keystore-backed encryption), dynamic theming, conditional navigation graph (auth vs main)
- **Bridge:** axios interceptors → OkHttp `Interceptor`/`Authenticator`; localStorage → DataStore
- **Done when:** app opens on login if no token, otherwise main; theme switch applies instantly

### M9 — Background Sync & Notifications (#8)
- **Build:** periodic + on-demand sync worker; notification on sync failure
- **Kotlin:** `CoroutineWorker`, structured concurrency & cancellation in long-running work
- **Android:** WorkManager (constraints, backoff, unique work), Hilt + WorkManager, notification channels, POST_NOTIFICATIONS runtime permission
- **Bridge:** `@Scheduled` → `PeriodicWorkRequest`
- **Done when:** sync runs with the app closed and only on network

### M10 — Testing & Code Quality (#9)
- **Build:** a test suite across layers + static analysis in Gradle
- **Kotlin:** `runTest`, `StandardTestDispatcher`, `MainDispatcherRule`, Turbine for Flow, MockK, fakes vs mocks, backtick test names
- **Android:** Room in-memory DAO tests, MockWebServer, Compose UI tests (`createComposeRule`, semantics), screenshot tests (Roborazzi), Hilt testing, detekt + ktlint + Android Lint
- **Bridge:** Jest + React Testing Library ≈ JUnit + Compose UI testing (find by text/semantics, perform click, assert)
- **Done when:** ViewModels & repositories ≥ 70% covered; the add/edit/sync critical path has UI tests; lint is clean

### M11 — Release & Observability (#10)
- **Build:** a production pipeline
- **Android/Gradle:** build types + flavors (`dev`/`prod`) with different API URLs, `BuildConfig`, secrets out of git, R8 minify/shrink + keep rules, signing config, AAB, GitHub Actions (lint → test → bundleRelease), Crashlytics or Sentry, Timber (no logs in release), Baseline Profiles, LeakCanary (debug), StrictMode, network security config
- **Bridge:** Spring profiles → flavors; webpack prod build/minify → R8
- **Done when:** CI produces a signed, minified release AAB; a test crash shows up in the dashboard

---

## 5. Top 10 Topics → Where You Learn Them

| # | Topic | Milestones |
|---|---|---|
| 1 | Kotlin idioms (null safety, data/sealed classes, extensions, collections) | M0–M2, and every milestone after |
| 2 | Coroutines & Flow | M3, M6, M7, M9 |
| 3 | Jetpack Compose | M1, M2, M4 |
| 4 | Architecture (MVVM, UDF, layers, modules) | M3, M5 |
| 5 | Dependency Injection (Hilt) | M5 |
| 6 | Networking & serialization | M7, M8 |
| 7 | Persistence & offline-first | M6, M7, M8 |
| 8 | Navigation, lifecycle, background work | M4, M9 |
| 9 | Testing & code quality | M10 (+ small tests from M3 onward) |
| 10 | Build, release, security, observability | M0, M8, M11 |

---

## 6. How Each Session Works
1. I explain the milestone's concepts briefly, with Java/React comparisons
2. You write the code (I give scaffolding, hints, and review); I avoid dumping full solutions unless you ask
3. We run it on the emulator and check the "Done when" criteria
4. Commit, tick the tracker, move on

---

## 7. Environment Note (WSL2)
Android Studio + emulator work best **natively on Windows**. Recommended:
- Install Android Studio on Windows, and keep the project on the Windows filesystem (e.g. `C:\dev\pocketledger`)
- Use WSL only for the optional Spring Boot backend (reach it from the emulator at `http://10.0.2.2:8080`)

---

## 8. Progress Tracker
- [ ] M0 — Setup & Hello Kotlin
- [ ] M1 — Expense list UI
- [ ] M2 — Add expense form & local state
- [ ] M3 — ViewModel, coroutines, UDF
- [ ] M4 — Navigation & process death
- [ ] M5 — Hilt & modularization
- [ ] M6 — Room persistence
- [ ] M7 — Networking & sync
- [ ] M8 — Auth & settings
- [ ] M9 — Background sync & notifications
- [ ] M10 — Testing & code quality
- [ ] M11 — Release & observability

## Resources
- Now in Android (reference production architecture): github.com/android/nowinandroid
- developer.android.com: Compose, Architecture, Testing guides
- kotlinlang.org/docs: "Kotlin for Java developers"
