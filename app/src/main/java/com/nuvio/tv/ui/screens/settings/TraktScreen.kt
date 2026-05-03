@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.screens.settings

import androidx.annotation.RawRes
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nuvio.tv.R
import com.nuvio.tv.core.qr.QrCodeGenerator
import com.nuvio.tv.data.local.TraktSettingsDataStore
import com.nuvio.tv.data.local.WatchProgressSource
import com.nuvio.tv.data.repository.TraktProgressService
import com.nuvio.tv.domain.model.LibrarySourceMode
import com.nuvio.tv.ui.components.NuvioDialog
import com.nuvio.tv.ui.theme.NuvioColors
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun TraktScreen(
    viewModel: TraktViewModel = hiltViewModel(),
    onBackPress: () -> Unit
) {
    BackHandler { onBackPress() }

    SettingsStandaloneScaffold(
        title = "Trakt",
        subtitle = stringResource(R.string.trakt_description)
    ) {
        TraktSettingsContent(viewModel = viewModel)
    }
}

@Composable
fun TraktSettingsContent(
    viewModel: TraktViewModel = hiltViewModel(),
    initialFocusRequester: FocusRequester? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val primaryFocusRequester = remember { FocusRequester() }

    var showDisconnectConfirm by remember { mutableStateOf(false) }
    var showDaysCapDialog by remember { mutableStateOf(false) }
    var showUnairedNextUpDialog by remember { mutableStateOf(false) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var showWatchProgressDialog by remember { mutableStateOf(false) }
    var showLibrarySourceDialog by remember { mutableStateOf(false) }

    val strAllHistory = stringResource(R.string.trakt_all_history)
    val strDaysFormat = stringResource(R.string.trakt_days_format)
    val strWatchProgressTrakt = stringResource(R.string.trakt_watch_progress_source_trakt)
    val strWatchProgressNuvio = stringResource(R.string.trakt_watch_progress_source_nuvio)
    val strSettingOn = stringResource(R.string.trakt_setting_on)
    val strSettingOff = stringResource(R.string.trakt_setting_off)

    val cwWindowFormatter: (Int) -> String = { days ->
        formatContinueWatchingWindow(days, strAllHistory) { strDaysFormat.format(it) }
    }
    val watchProgressFormatter: (WatchProgressSource) -> String = { source ->
        when (source) {
            WatchProgressSource.TRAKT -> strWatchProgressTrakt
            WatchProgressSource.NUVIO_SYNC -> strWatchProgressNuvio
        }
    }
    val strLibrarySourceTrakt = stringResource(R.string.trakt_library_source_trakt)
    val strLibrarySourceNuvio = stringResource(R.string.trakt_library_source_nuvio)
    val librarySourceFormatter: (LibrarySourceMode) -> String = { mode ->
        when (mode) {
            LibrarySourceMode.TRAKT -> strLibrarySourceTrakt
            LibrarySourceMode.LOCAL -> strLibrarySourceNuvio
        }
    }

    val continueWatchingDayOptions = remember {
        listOf(
            14,
            30,
            60,
            90,
            180,
            365,
            TraktSettingsDataStore.CONTINUE_WATCHING_DAYS_CAP_ALL
        )
    }

    val nowMillis by produceState(initialValue = System.currentTimeMillis(), key1 = uiState.mode) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000)
        }
    }

    LaunchedEffect(uiState.mode) {
        if (initialFocusRequester == null) {
            primaryFocusRequester.requestFocus()
        }
    }

    val userCode = uiState.deviceUserCode
    val qrBitmap = remember(userCode) {
        userCode?.let {
            runCatching { QrCodeGenerator.generate("https://trakt.tv/activate/$it", 420) }.getOrNull()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsGroupCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.mode == TraktConnectionMode.AWAITING_APPROVAL) {
                        item(key = "awaiting_approval") {
                            val expiresAt = uiState.deviceCodeExpiresAtMillis
                            val remaining = expiresAt?.let { (it - nowMillis).coerceAtLeast(0L) } ?: 0L

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.trakt_awaiting_instruction),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = NuvioColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = userCode ?: "-",
                                    color = NuvioColors.Primary,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 4.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = stringResource(R.string.cd_trakt_qr),
                                        modifier = Modifier.size(180.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.trakt_code_expires, formatDuration(remaining)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NuvioColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        item(key = "action_retry") {
                            SettingsActionRow(
                                title = stringResource(R.string.trakt_retry),
                                subtitle = null,
                                onClick = { viewModel.onRetryPolling() },
                                enabled = !uiState.isLoading,
                                trailingIcon = Icons.Default.Refresh,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .then(if (initialFocusRequester != null) Modifier.focusRequester(initialFocusRequester) else Modifier.focusRequester(primaryFocusRequester))
                            )
                        }

                        item(key = "action_cancel") {
                            SettingsActionRow(
                                title = stringResource(R.string.action_cancel),
                                subtitle = null,
                                onClick = { viewModel.onCancelDeviceFlow() },
                                trailingIcon = Icons.Default.Close
                            )
                        }
                    } else if (uiState.mode == TraktConnectionMode.CONNECTED) {
                        item(key = "connected_status") {
                            val expiresAtMillis = uiState.tokenExpiresAtMillis
                            val subtitleStr = if (expiresAtMillis != null) {
                                stringResource(R.string.trakt_token_refreshes, formatDuration((expiresAtMillis - nowMillis).coerceAtLeast(0L)))
                            } else null

                            SettingsActionRow(
                                title = stringResource(R.string.trakt_disconnect),
                                subtitle = subtitleStr,
                                value = stringResource(R.string.trakt_connected_as, uiState.username ?: "Trakt user"),
                                onClick = { showDisconnectConfirm = true },
                                trailingIcon = Icons.Default.Logout,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .then(if (initialFocusRequester != null) Modifier.focusRequester(initialFocusRequester) else Modifier.focusRequester(primaryFocusRequester))
                            )
                        }

                        item(key = "connected_stats") {
                            TraktConnectedStatsStrip(
                                stats = uiState.connectedStats,
                                isLoading = uiState.isStatsLoading
                            )
                        }

                        item(key = "library_source") {
                            SettingsActionRow(
                                title = stringResource(R.string.trakt_library_source_title),
                                subtitle = stringResource(R.string.trakt_library_source_subtitle),
                                value = librarySourceFormatter(uiState.librarySourceMode),
                                onClick = { showLibrarySourceDialog = true }
                            )
                        }

                        item(key = "watch_progress") {
                            SettingsActionRow(
                                title = stringResource(R.string.trakt_watch_progress_title),
                                subtitle = stringResource(R.string.trakt_watch_progress_subtitle),
                                value = watchProgressFormatter(uiState.watchProgressSource),
                                onClick = { showWatchProgressDialog = true }
                            )
                        }

                        item(key = "continue_watching") {
                            SettingsActionRow(
                                title = stringResource(R.string.trakt_continue_watching_window),
                                subtitle = stringResource(R.string.trakt_continue_watching_subtitle),
                                value = cwWindowFormatter(uiState.continueWatchingDaysCap),
                                onClick = { showDaysCapDialog = true }
                            )
                        }

                        item(key = "unaired_next_up") {
                            SettingsActionRow(
                                title = stringResource(R.string.trakt_unaired_next_up),
                                subtitle = stringResource(R.string.trakt_unaired_next_up_subtitle),
                                value = if (uiState.showUnairedNextUp) strSettingOn else strSettingOff,
                                onClick = { showUnairedNextUpDialog = true }
                            )
                        }

                        item(key = "comments") {
                            SettingsActionRow(
                                title = stringResource(R.string.trakt_comments_title),
                                subtitle = stringResource(R.string.trakt_comments_subtitle),
                                value = if (uiState.showMetaComments) strSettingOn else strSettingOff,
                                onClick = { showCommentsDialog = true }
                            )
                        }
                    } else {
                        item(key = "login_instruction") {
                            SettingsActionRow(
                                title = stringResource(R.string.trakt_login),
                                subtitle = stringResource(R.string.trakt_login_instruction),
                                onClick = { viewModel.onConnectClick() },
                                enabled = uiState.credentialsConfigured && !uiState.isLoading,
                                trailingIcon = Icons.Default.Login,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .then(if (initialFocusRequester != null) Modifier.focusRequester(initialFocusRequester) else Modifier.focusRequester(primaryFocusRequester))
                            )
                        }

                        if (!uiState.credentialsConfigured) {
                            item(key = "missing_credentials") {
                                Text(
                                    text = stringResource(R.string.trakt_missing_credentials),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFFFB74D),
                                    modifier = Modifier.padding(horizontal = 18.dp)
                                )
                            }
                        }
                    }

                    if (uiState.mode != TraktConnectionMode.CONNECTED) {
                        uiState.statusMessage?.let { status ->
                            item(key = "status_message") {
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NuvioColors.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 18.dp)
                                )
                            }
                        }
                    }

                    uiState.errorMessage?.let { error ->
                        item(key = "error_message") {
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFF6E6E),
                                modifier = Modifier.padding(horizontal = 18.dp)
                            )
                        }
                    }
                }
                SettingsVerticalScrollIndicators(state = listState)
            }
        }
    }

    if (showWatchProgressDialog) {
        NuvioDialog(
            onDismiss = { showWatchProgressDialog = false },
            title = stringResource(R.string.trakt_watch_progress_dialog_title),
            subtitle = stringResource(R.string.trakt_watch_progress_dialog_subtitle),
            width = 620.dp,
            suppressFirstKeyUp = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.onWatchProgressSourceSelected(WatchProgressSource.TRAKT)
                        showWatchProgressDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (uiState.watchProgressSource == WatchProgressSource.TRAKT) {
                            NuvioColors.Primary
                        } else {
                            NuvioColors.BackgroundCard
                        },
                        contentColor = if (uiState.watchProgressSource == WatchProgressSource.TRAKT) {
                            Color.Black
                        } else {
                            NuvioColors.TextPrimary
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_watch_progress_source_trakt))
                }
                Button(
                    onClick = {
                        viewModel.onWatchProgressSourceSelected(WatchProgressSource.NUVIO_SYNC)
                        showWatchProgressDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (uiState.watchProgressSource == WatchProgressSource.NUVIO_SYNC) {
                            NuvioColors.Primary
                        } else {
                            NuvioColors.BackgroundCard
                        },
                        contentColor = if (uiState.watchProgressSource == WatchProgressSource.NUVIO_SYNC) {
                            Color.Black
                        } else {
                            NuvioColors.TextPrimary
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_watch_progress_source_nuvio))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showWatchProgressDialog = false },
                        colors = ButtonDefaults.colors(
                            containerColor = NuvioColors.BackgroundCard,
                            contentColor = NuvioColors.TextPrimary
                        )
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }

    if (showLibrarySourceDialog) {
        NuvioDialog(
            onDismiss = { showLibrarySourceDialog = false },
            title = stringResource(R.string.trakt_library_source_dialog_title),
            subtitle = stringResource(R.string.trakt_library_source_dialog_subtitle),
            width = 620.dp,
            suppressFirstKeyUp = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.onLibrarySourceModeSelected(LibrarySourceMode.TRAKT)
                        showLibrarySourceDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (uiState.librarySourceMode == LibrarySourceMode.TRAKT) {
                            NuvioColors.Primary
                        } else {
                            NuvioColors.BackgroundCard
                        },
                        contentColor = if (uiState.librarySourceMode == LibrarySourceMode.TRAKT) {
                            Color.Black
                        } else {
                            NuvioColors.TextPrimary
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_library_source_trakt))
                }
                Button(
                    onClick = {
                        viewModel.onLibrarySourceModeSelected(LibrarySourceMode.LOCAL)
                        showLibrarySourceDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (uiState.librarySourceMode == LibrarySourceMode.LOCAL) {
                            NuvioColors.Primary
                        } else {
                            NuvioColors.BackgroundCard
                        },
                        contentColor = if (uiState.librarySourceMode == LibrarySourceMode.LOCAL) {
                            Color.Black
                        } else {
                            NuvioColors.TextPrimary
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_library_source_nuvio))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showLibrarySourceDialog = false },
                        colors = ButtonDefaults.colors(
                            containerColor = NuvioColors.BackgroundCard,
                            contentColor = NuvioColors.TextPrimary
                        )
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }

    if (showDaysCapDialog) {
        NuvioDialog(
            onDismiss = { showDaysCapDialog = false },
            title = stringResource(R.string.trakt_cw_window_title),
            subtitle = stringResource(R.string.trakt_cw_window_subtitle),
            width = 620.dp,
            suppressFirstKeyUp = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                continueWatchingDayOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowOptions.forEach { days ->
                            val selected = uiState.continueWatchingDaysCap == days
                            Button(
                                onClick = {
                                    viewModel.onContinueWatchingDaysCapSelected(days)
                                    showDaysCapDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.colors(
                                    containerColor = if (selected) NuvioColors.Primary else NuvioColors.BackgroundCard,
                                    contentColor = if (selected) Color.Black else NuvioColors.TextPrimary
                                )
                            ) {
                                Text(cwWindowFormatter(days))
                            }
                        }
                        if (rowOptions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showDaysCapDialog = false },
                        colors = ButtonDefaults.colors(
                            containerColor = NuvioColors.BackgroundCard,
                            contentColor = NuvioColors.TextPrimary
                        )
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }

    if (showUnairedNextUpDialog) {
        NuvioDialog(
            onDismiss = { showUnairedNextUpDialog = false },
            title = stringResource(R.string.trakt_unaired_dialog_title),
            subtitle = stringResource(R.string.trakt_unaired_dialog_subtitle),
            width = 620.dp,
            suppressFirstKeyUp = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.onShowUnairedNextUpChanged(true)
                        showUnairedNextUpDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (uiState.showUnairedNextUp) NuvioColors.Primary else NuvioColors.BackgroundCard,
                        contentColor = if (uiState.showUnairedNextUp) Color.Black else NuvioColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_show_unaired))
                }
                Button(
                    onClick = {
                        viewModel.onShowUnairedNextUpChanged(false)
                        showUnairedNextUpDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (!uiState.showUnairedNextUp) NuvioColors.Primary else NuvioColors.BackgroundCard,
                        contentColor = if (!uiState.showUnairedNextUp) Color.Black else NuvioColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_hide_unaired))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showUnairedNextUpDialog = false },
                        colors = ButtonDefaults.colors(
                            containerColor = NuvioColors.BackgroundCard,
                            contentColor = NuvioColors.TextPrimary
                        )
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }

    if (showCommentsDialog) {
        NuvioDialog(
            onDismiss = { showCommentsDialog = false },
            title = stringResource(R.string.trakt_comments_dialog_title),
            subtitle = stringResource(R.string.trakt_comments_dialog_subtitle),
            width = 620.dp,
            suppressFirstKeyUp = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        viewModel.onShowMetaCommentsChanged(true)
                        showCommentsDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (uiState.showMetaComments) NuvioColors.Primary else NuvioColors.BackgroundCard,
                        contentColor = if (uiState.showMetaComments) Color.Black else NuvioColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_setting_on))
                }
                Button(
                    onClick = {
                        viewModel.onShowMetaCommentsChanged(false)
                        showCommentsDialog = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = if (!uiState.showMetaComments) NuvioColors.Primary else NuvioColors.BackgroundCard,
                        contentColor = if (!uiState.showMetaComments) Color.Black else NuvioColors.TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.trakt_setting_off))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showCommentsDialog = false },
                        colors = ButtonDefaults.colors(
                            containerColor = NuvioColors.BackgroundCard,
                            contentColor = NuvioColors.TextPrimary
                        )
                    ) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }

    if (showDisconnectConfirm) {
        NuvioDialog(
            onDismiss = { showDisconnectConfirm = false },
            title = stringResource(R.string.trakt_disconnect_title),
            subtitle = stringResource(R.string.trakt_disconnect_subtitle),
            width = 520.dp,
            suppressFirstKeyUp = false
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        showDisconnectConfirm = false
                        viewModel.onDisconnectClick()
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = NuvioColors.BackgroundCard,
                        contentColor = NuvioColors.TextPrimary
                    )
                ) {
                    Text(stringResource(R.string.trakt_disconnect))
                }
                Button(
                    onClick = { showDisconnectConfirm = false },
                    colors = ButtonDefaults.colors(
                        containerColor = NuvioColors.BackgroundCard,
                        contentColor = NuvioColors.TextPrimary
                    )
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    }
}

@Composable
private fun TraktConnectedStatsStrip(
    stats: TraktProgressService.TraktCachedStats?,
    isLoading: Boolean
) {
    val values = if (isLoading) {
        listOf("...", "...", "...", "...")
    } else {
        listOf(
            stats?.moviesWatched?.toString() ?: "-",
            stats?.showsWatched?.toString() ?: "-",
            stats?.episodesWatched?.toString() ?: "-",
            stats?.totalWatchedHours?.let { "${it}h" } ?: "-"
        )
    }
    val labels = listOf(
        stringResource(R.string.trakt_stat_movies),
        stringResource(R.string.trakt_stat_shows),
        stringResource(R.string.trakt_stat_episodes),
        stringResource(R.string.trakt_stat_watched_hours)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.trakt_cached_label),
            style = MaterialTheme.typography.labelMedium,
            color = NuvioColors.TextTertiary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NuvioColors.Border.copy(alpha = 0.8f))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(values.size) { index ->
                TraktStatItem(
                    value = values[index],
                    label = labels[index],
                    modifier = Modifier.weight(1f)
                )
                if (index != values.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(44.dp)
                            .background(NuvioColors.Border.copy(alpha = 0.75f))
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(NuvioColors.Border.copy(alpha = 0.8f))
        )
    }
}

@Composable
private fun TraktStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = NuvioColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = NuvioColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun rememberRawSvgPainter(@RawRes iconRes: Int): Painter {
    val context = LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val sizePx = with(density) { 24.dp.roundToPx() }
    val request = remember(iconRes, context, sizePx) {
        ImageRequest.Builder(context)
            .data(iconRes)
            .size(sizePx)
            .crossfade(false)
            .build()
    }
    return rememberAsyncImagePainter(model = request)
}

private fun formatDuration(valueMs: Long): String {
    val totalSeconds = (valueMs / 1000L).coerceAtLeast(0L)
    val days = TimeUnit.SECONDS.toDays(totalSeconds)
    val hours = TimeUnit.SECONDS.toHours(totalSeconds) % 24
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
    val seconds = totalSeconds % 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

private fun formatContinueWatchingWindow(days: Int, allHistoryLabel: String, daysFormat: (Int) -> String): String {
    return if (days == TraktSettingsDataStore.CONTINUE_WATCHING_DAYS_CAP_ALL) {
        allHistoryLabel
    } else {
        daysFormat(days)
    }
}
