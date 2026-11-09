package dybarsky.blocker.ui.screens.role

import android.app.role.RoleManager
import android.app.role.RoleManager.ROLE_CALL_SCREENING
import android.content.Context.ROLE_SERVICE
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RoleScreen(
    goToHome: () -> Unit,
) {
    val context = LocalContext.current
    val manager = context.getSystemService(ROLE_SERVICE) as RoleManager
    val intent = manager.createRequestRoleIntent(ROLE_CALL_SCREENING)
    val handler = {
        val roleHold = manager.isRoleHeld(ROLE_CALL_SCREENING)
        if (roleHold) goToHome()
    }
    val launcher = rememberLauncherForActivityResult(
        contract = StartActivityForResult(),
        onResult = { handler() }
    )

    LaunchedEffect(context) { handler() }

    RoleScreenContent {
        launcher.launch(intent)
    }
}

@Composable
fun RoleScreenContent(callback: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(42.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Для коректної роботи додатку необхідно активувати його як блокувальник викликів.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Button(onClick = callback) {
                Text("Активувати")
            }
        }
    }
}
