package com.section11.expenselens.ui.utils

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.section11.expenselens.ui.theme.ExpenseLensTheme

@Preview(name = "Dark Mode", showBackground = true, uiMode = UI_MODE_NIGHT_YES, showSystemUi = true)
@Preview(name = "Light Mode", showBackground = true, uiMode = UI_MODE_NIGHT_NO, showSystemUi = true)
annotation class DarkAndLightPreviews

@Composable
fun Preview(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ExpenseLensTheme {
        Surface(modifier) {
            content()
        }
    }
}
