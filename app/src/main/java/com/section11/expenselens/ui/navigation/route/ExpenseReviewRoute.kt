package com.section11.expenselens.ui.navigation.route

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.section11.expenselens.domain.models.ExpenseInformation
import com.section11.expenselens.ui.theme.LocalDimens

@Composable
fun ExpenseReviewRoute(expenseInfo: ExpenseInformation?, extractedTextFromImage: String) {
    val dimens = LocalDimens.current
    val textToShow = if (expenseInfo == null) {
        "Error retrieving expense information"
    } else {
        "Processed Text from Gemini: \n" +
                "total = ${expenseInfo.total} | " +
                "category = ${expenseInfo.estimatedCategory}"
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = dimens.m2)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Extracted Text from Image:\n $extractedTextFromImage",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.padding(dimens.m2))

        Text(
            text = textToShow,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
