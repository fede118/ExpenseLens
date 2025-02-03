package com.section11.expenselens.ui.utils

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Dark Mode", showBackground = true, uiMode = UI_MODE_NIGHT_YES, showSystemUi = true)
@Preview(name = "Light Mode", showBackground = true, uiMode = UI_MODE_NIGHT_NO, showSystemUi = true)
annotation class DarkAndLightPreviews
