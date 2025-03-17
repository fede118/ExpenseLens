package com.section11.expenselens.ui.household.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.section11.expenselens.R
import com.section11.expenselens.ui.common.previewrepository.FakeRepositoryForPreviews
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUiState.ShowHouseholdDetails
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUpstreamEvent
import com.section11.expenselens.ui.household.HouseholdDetailsViewModel.HouseholdDetailsUpstreamEvent.OnCtaClicked
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel
import com.section11.expenselens.ui.household.model.HouseholdDetailsUiModel.HouseholdDetailsCta.Delete
import com.section11.expenselens.ui.theme.LocalDimens
import com.section11.expenselens.ui.utils.DarkAndLightPreviews
import com.section11.expenselens.ui.utils.Preview
import com.section11.expenselens.ui.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HouseholdDetailsScreen(
    uiStateFlow: StateFlow<UiState>,
    modifier: Modifier = Modifier,
    onEvent: (HouseholdDetailsUpstreamEvent) -> Unit
) {
    val uiState by uiStateFlow.collectAsState()

    when(val valState = uiState) {
       is ShowHouseholdDetails -> HouseholdDetails(valState.householdDetails, modifier, onEvent)
    }
}

@Composable
fun HouseholdDetails(
    householdDetailsModel: HouseholdDetailsUiModel,
    modifier: Modifier = Modifier,
    onEvent: (HouseholdDetailsUpstreamEvent) -> Unit
) {
    val dimens = LocalDimens.current

    Column(
        modifier = modifier.fillMaxWidth()
            .padding(dimens.m2)
            .statusBarsPadding()
    ) {
        Text(
            text = stringResource(
                R.string.household_details_title,
                householdDetailsModel.householdName
            ),
            style = MaterialTheme.typography.titleLarge
        )
        HorizontalDivider(Modifier.height(dimens.mOctave))
        Spacer(Modifier.height(dimens.m1))
        Text(
            text = stringResource(R.string.household_details_members_title),
            style = MaterialTheme.typography.titleMedium
        )
        HorizontalDivider(Modifier.height(dimens.mOctave))
        Spacer(Modifier.height(dimens.m1))

        householdDetailsModel.users.forEach {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(dimens.m1))
        }

        Spacer(Modifier.height(dimens.m1))
        with(householdDetailsModel.cta) {
            Button(
                onClick = { onEvent(OnCtaClicked(this)) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (this is Delete) {
                    ButtonDefaults.buttonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(text = label)
            }
        }
    }
}

@DarkAndLightPreviews
@Composable
fun HouseholdDetailsScreenPreview() {
    val fakeRepo = FakeRepositoryForPreviews(LocalContext.current)

    val uiState = MutableStateFlow(ShowHouseholdDetails(fakeRepo.getHouseholdDetails()))
    Preview {
        HouseholdDetailsScreen(uiState) {}
    }
}
