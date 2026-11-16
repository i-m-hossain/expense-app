package com.imran.pocketledger

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * App-wide UI settings in one place. Change a value here and it applies everywhere.
 * In M8 these can move into user Settings (DataStore) so they're changeable at runtime.
 */
object UiConfig {
    /** How long the "Deleted … / Undo" snackbar stays on screen. */
    val undoSnackbarTimeout: Duration = 8.seconds
}
