package com.section11.expenselens.ui.review.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.section11.expenselens.R
import com.section11.expenselens.framework.utils.toDateString
import com.section11.expenselens.ui.common.CardDialog
import com.section11.expenselens.ui.common.ExpenseLensDatePicker
import com.section11.expenselens.ui.common.HandleDownstreamEvents
import com.section11.expenselens.ui.common.MaxCharsOutlinedTextField
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUiState.ShowExpenseReview
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.ExpenseSubmitted
import com.section11.expenselens.ui.review.ExpenseReviewViewModel.ExpenseReviewUpstreamEvent.UserInputEvent
import com.section11.expenselens.ui.review.mapper.ExpenseReviewScreenUiMapper.ExpenseReviewSections
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel
import com.section11.expenselens.ui.review.model.ExpenseReviewUiModel.ReviewRow
import com.section11.expenselens.ui.review.model.ReviewRowType.DatePickerType
import com.section11.expenselens.ui.review.model.ReviewRowType.DropdownMenuType
import com.section11.expenselens.ui.review.model.ReviewRowType.MoneyInputType
import com.section11.expenselens.ui.review.model.ReviewRowType.TextInputType
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.DownstreamUiEvent
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ExpenseReviewScreen(
    modifier: Modifier = Modifier,
    expenseReviewUiStateFlow: StateFlow<UiState>,
    downstreamUiEvent: SharedFlow<DownstreamUiEvent>,
    onUpstreamUiEvent: (ExpenseReviewUpstreamEvent) -> Unit = {}
) {
    val dimens = LocalDimens.current
    val expenseReviewUiState by expenseReviewUiStateFlow.collectAsState()

    HandleDownstreamEvents(downstreamUiEvent)

    when (expenseReviewUiState) {
        is ShowExpenseReview -> ExpenseReviewSection(
            modifier = modifier.padding(top = dimens.m4),
            expenseReviewUiModel = (expenseReviewUiState as ShowExpenseReview).expenseReviewUiModel
        ) { event -> onUpstreamUiEvent(event) }
        else -> { /* no op for now */ }
    }
}

@Composable
fun ExpenseReviewSection(
    modifier: Modifier = Modifier,
    expenseReviewUiModel: ExpenseReviewUiModel,
    onUpstreamUiEvent: (ExpenseReviewUpstreamEvent) -> Unit
) {
    val dimens = LocalDimens.current

    LazyColumn(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = dimens.m2)
    ) {
        items(expenseReviewUiModel.reviewRows) { item ->
            Spacer(modifier.height(dimens.m1))
            ReviewSectionRow(reviewRow = item) { section, newValue ->
                onUpstreamUiEvent(UserInputEvent(section, newValue))
            }
        }
        item { Spacer(modifier = Modifier.padding(dimens.m2)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier,
                    onClick = {
                        onUpstreamUiEvent(ExpenseSubmitted(expenseReviewUiModel))
                    }
                ) {
                    Text(stringResource(R.string.expense_review_screen_submit_expense))
                }
            }

        }
        item { Spacer(modifier = Modifier.padding(dimens.m4)) }
        item {
            expenseReviewUiModel.extractedText?.let {
                ExtractedTextSection(extractedText = it)
            } ?: ExtractedTextSection(extractedText = "No Extracted Text")
        }


    }
}

@Composable
fun ReviewSectionRow(
    modifier: Modifier = Modifier,
    reviewRow: ReviewRow,
    onRowValueChanged: (ExpenseReviewSections, String) -> Unit
) {
    val dimens = LocalDimens.current

    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.m2)
    ) {
        when(reviewRow.section.viewType) {
            is DatePickerType -> ExpenseLensDatePicker(selectedDate = reviewRow.value) { selectedDate ->
                onRowValueChanged(reviewRow.section, selectedDate.toDateString())
            }
            is DropdownMenuType -> {
                RowDropDownMenu(
                    title = reviewRow.title,
                    currentValue = reviewRow.value,
                    options = reviewRow.section.viewType.options
                ) { newValue ->
                    onRowValueChanged(reviewRow.section, newValue)
                }
            }
            MoneyInputType -> {
                OutlinedTextField(
                    value = reviewRow.value,
                    onValueChange = { newValue ->
                        onRowValueChanged(reviewRow.section, newValue)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(1f),
                    label = { Text(text = reviewRow.title) },
                    singleLine = true,
                    leadingIcon = { Text(stringResource((R.string.dollar_sign))) }
                )
            }
            TextInputType -> {
                MaxCharsOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(1f),
                    value = reviewRow.value,
                    title = reviewRow.title,
                    onValueChange = { newValue ->
                        onRowValueChanged(reviewRow.section, newValue)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowDropDownMenu(
    modifier: Modifier = Modifier,
    title: String,
    currentValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = currentValue,
            onValueChange = {},
            label = { Text(title) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Icon"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        expanded = false
                        onValueChange(it)
                    }
                )
            }
        }
    }
}

@Composable
fun ExtractedTextSection(modifier: Modifier = Modifier, extractedText: String) {
    val dimens = LocalDimens.current
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = !showDialog },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimens.m4)
    ) {
        Text(
            text = "See extracted text from image",
            style = MaterialTheme.typography.titleMedium
        )
    }

    if (showDialog) {
        CardDialog(
            onDismiss = { showDialog = false },
        ) {
            Text(
                text = "Extracted Text from Image",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.padding(LocalDimens.current.m2))
            Text(
                text = extractedText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@DarkAndLightPreviews
@Composable
fun ExpenseReviewPreview(modifier: Modifier = Modifier) {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)
    Preview {
        ExpenseReviewScreen(
            modifier = modifier,
            expenseReviewUiStateFlow = MutableStateFlow(
                ShowExpenseReview(fakeRepo.getExpenseReviewUiModel())
            ),
            downstreamUiEvent = MutableSharedFlow()
        )
    }
}
