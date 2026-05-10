package com.nuvio.tv.ui.screens.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState

private const val LIST_TOP_ANIMATION_WINDOW = 3
private const val GRID_TOP_ANIMATION_WINDOW = 18

internal suspend fun LazyListState.quickAnimateScrollToTop() {
    if (firstVisibleItemIndex > LIST_TOP_ANIMATION_WINDOW) {
        scrollToItem(LIST_TOP_ANIMATION_WINDOW, 0)
    }
    animateScrollToItem(0, 0)
}

internal suspend fun LazyGridState.quickAnimateScrollToTop() {
    if (firstVisibleItemIndex > GRID_TOP_ANIMATION_WINDOW) {
        scrollToItem(GRID_TOP_ANIMATION_WINDOW, 0)
    }
    animateScrollToItem(0, 0)
}
