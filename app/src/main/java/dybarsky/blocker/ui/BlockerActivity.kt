package dybarsky.blocker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import dybarsky.blocker.ui.screens.main.MainScreen
import dybarsky.blocker.ui.theme.BlockerTheme

class BlockerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockerTheme {
                Surface {
                    MainScreen()
                }
            }
        }
    }
}
