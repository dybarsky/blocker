package dybarsky.blocker.ui.screens.home

import androidx.compose.runtime.Composable
import dybarsky.blocker.common.models.Number
import dybarsky.blocker.ui.common.Callback
import dybarsky.blocker.ui.common.PreviewFrame
import dybarsky.blocker.ui.common.ScreenPreview

private val noop = Callback<HomeIntention> { }

private val sampleNumbers = listOf(
    Number(Number.Id(1), "+43*"),
    Number(Number.Id(2), "+44165*"),
    Number(Number.Id(3), "+49 145 *"),
    Number(Number.Id(4), "+38050*4567"),
    Number(Number.Id(5), "+38 (067) 123-45-67 *89*0000000000000000"),
    Number(Number.Id(6), "*"),
)

@Composable
private fun Preview(state: HomeState) = PreviewFrame { HomeScreen(state, noop) }

@ScreenPreview
@Composable
private fun HomeContentPreview() = Preview(HomeState.Content(sampleNumbers))

@ScreenPreview
@Composable
private fun HomeEmptyPreview() = Preview(HomeState.Content(emptyList()))

@ScreenPreview
@Composable
private fun HomeLoadingPreview() = Preview(HomeState.Loading)

@ScreenPreview
@Composable
private fun HomeErrorPreview() = Preview(HomeState.Error)

@ScreenPreview
@Composable
private fun HomeDeleteFailedPreview() =
    Preview(HomeState.Content(sampleNumbers, deleteFailed = true))
