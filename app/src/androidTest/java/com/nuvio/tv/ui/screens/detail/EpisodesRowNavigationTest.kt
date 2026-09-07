package com.nuvio.tv.ui.screens.detail

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nuvio.tv.domain.model.Video
import com.nuvio.tv.ui.theme.NuvioTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class EpisodesRowNavigationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun canNavigateFromEpisodeSixBackToOneAndAcrossOffscreenCards() {
        val episodes = (1..20).map {
            Video(id = "28:$it", title = "Episode $it", released = null,
                thumbnail = null, season = 28, episode = it, overview = null)
        }
        val requesters = episodes.associate { it.id to FocusRequester() }.toMutableMap()
        var focusedId: String? = null
        composeRule.setContent {
            NuvioTheme {
                EpisodesRow(
                    episodes = episodes,
                    onEpisodeClick = {}, onToggleEpisodeWatched = {},
                    upFocusRequester = FocusRequester(),
                    episodeFocusRequesters = requesters,
                    scrollToEpisodeId = "28:6",
                    onEpisodeFocused = { focusedId = it }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { requesters.getValue("28:6").requestFocus() }
        composeRule.waitUntil { focusedId == "28:6" }
        for (episode in 5 downTo 1) {
            composeRule.onRoot().performKeyInput { pressKey(Key.DirectionLeft) }
            composeRule.waitUntil { focusedId == "28:$episode" }
        }
        composeRule.onRoot().performKeyInput { pressKey(Key.DirectionLeft) }
        composeRule.runOnIdle { assertEquals("28:1", focusedId) }
        for (episode in 2..20) {
            composeRule.onRoot().performKeyInput { pressKey(Key.DirectionRight) }
            composeRule.waitUntil { focusedId == "28:$episode" }
        }
        composeRule.onRoot().performKeyInput { pressKey(Key.DirectionRight) }
        composeRule.runOnIdle { assertEquals("28:20", focusedId) }
    }
}
