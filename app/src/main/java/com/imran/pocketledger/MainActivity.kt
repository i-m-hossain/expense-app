package com.imran.pocketledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.imran.pocketledger.ui.theme.PocketLedgerTheme
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketLedgerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "World",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String?,
    modifier: Modifier = Modifier,
    hour: Int = LocalTime.now().hour,
) {
    Text(
        text = "${greetingFor(hour)}, ${name ?: "there"}!",
        modifier = modifier
    )
}

fun greetingFor(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PocketLedgerTheme {
        Greeting(name = "Imran")
    }
}

@Preview(showBackground = true, name = "Null name, morning")
@Composable
fun GreetingNullNamePreview() {
    PocketLedgerTheme {
        Greeting(name = null, hour = 9)
    }
}