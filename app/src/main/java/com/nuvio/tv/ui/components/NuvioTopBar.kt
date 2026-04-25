package com.nuvio.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.nuvio.tv.ui.theme.NuvioColors
import com.nuvio.tv.DrawerItem
import androidx.compose.ui.res.painterResource
import com.nuvio.tv.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun NuvioTopBar(
    drawerItems: List<com.nuvio.tv.DrawerItem>,
    selectedDrawerRoute: String?,
    drawerItemFocusRequesters: Map<String, FocusRequester>,
    onDrawerItemFocused: (Int) -> Unit,
    onDrawerItemClick: (String) -> Unit,
    activeProfileName: String,
    activeProfileColorHex: String,
    activeProfileAvatarImageUrl: String?,
    showProfileSelector: Boolean,
    onSwitchProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(999.dp)
    val bgElevated = NuvioColors.BackgroundElevated
    val bgCard = NuvioColors.BackgroundCard
    val borderBase = NuvioColors.Border
    val pillBackgroundBrush = remember(bgElevated, bgCard) {
        Brush.verticalGradient(listOf(bgElevated, bgCard))
    }
    val pillBorderColor = remember(borderBase) {
        borderBase.copy(alpha = 0.9f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(44.dp)
                .clip(pillShape)
                .background(brush = pillBackgroundBrush, shape = pillShape)
                .border(width = 1.dp, color = pillBorderColor, shape = pillShape)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                drawerItems.forEachIndexed { index, item ->
                    val isSelected = item.route == selectedDrawerRoute
                    TopBarNavItem(
                        item = item,
                        isSelected = isSelected,
                        focusRequester = drawerItemFocusRequesters[item.route],
                        onFocused = { onDrawerItemFocused(index) },
                        onClick = { onDrawerItemClick(item.route) }
                    )
                }

                if (showProfileSelector && activeProfileName.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TopBarProfileItem(
                        activeProfileName = activeProfileName,
                        activeProfileColorHex = activeProfileColorHex,
                        activeProfileAvatarImageUrl = activeProfileAvatarImageUrl,
                        onClick = onSwitchProfile
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TopBarNavItem(
    item: com.nuvio.tv.DrawerItem,
    isSelected: Boolean,
    focusRequester: FocusRequester?,
    onFocused: () -> Unit,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused || isSelected) NuvioColors.FocusBackground else Color.Transparent,
        label = "navItemBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) NuvioColors.FocusRing else Color.Transparent,
        label = "navItemBorder"
    )

    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { state ->
                isFocused = state.hasFocus
                if (state.hasFocus) {
                    onFocused()
                }
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    tint = if (isFocused || isSelected) NuvioColors.TextPrimary else NuvioColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            } ?: item.iconRes?.let { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = item.label,
                    tint = if (isFocused || isSelected) NuvioColors.TextPrimary else NuvioColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleSmall,
                color = if (isFocused || isSelected) NuvioColors.TextPrimary else NuvioColors.TextSecondary
            )
        }
    }
}

@Composable
private fun TopBarProfileItem(
    activeProfileName: String,
    activeProfileColorHex: String,
    activeProfileAvatarImageUrl: String?,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) NuvioColors.FocusBackground else Color.Transparent,
        label = "profileItemBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) NuvioColors.FocusRing else Color.Transparent,
        label = "profileItemBorder"
    )

    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .onFocusChanged { state ->
                isFocused = state.hasFocus
            }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (activeProfileAvatarImageUrl != null) {
                AsyncImage(
                    model = activeProfileAvatarImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(activeProfileColorHex))
                            } catch (e: Exception) {
                                NuvioColors.Primary
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activeProfileName.take(1).uppercase(),
                        color = Color.White,
                        style = androidx.tv.material3.MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = activeProfileName,
                style = androidx.tv.material3.MaterialTheme.typography.titleSmall,
                color = if (isFocused) NuvioColors.TextPrimary else NuvioColors.TextSecondary
            )
        }
    }
}
