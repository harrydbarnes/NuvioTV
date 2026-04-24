package com.nuvio.tv.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.DrawerItem
import com.nuvio.tv.ui.theme.NuvioColors

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TopNavigationScaffold(
    currentRoute: String?,
    drawerItems: List<DrawerItem>,
    selectedDrawerRoute: String?,
    onNavigate: (String) -> Unit,
    contentFocusRequester: FocusRequester,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(NuvioColors.Background)
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NUVIO",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = NuvioColors.Primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                drawerItems.forEach { item ->
                    TopNavItem(
                        item = item,
                        isSelected = item.route == selectedDrawerRoute,
                        onNavigate = { onNavigate(item.route) }
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f).focusRequester(contentFocusRequester)) {
            content()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TopNavItem(
    item: DrawerItem,
    isSelected: Boolean,
    onNavigate: () -> Unit
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
    val NavItemShape = RoundedCornerShape(14.dp)

    Card(
        onClick = onNavigate,
        modifier = Modifier
            .height(48.dp)
            .onFocusChanged { state ->
                isFocused = state.hasFocus
            },
        colors = CardDefaults.colors(
            containerColor = backgroundColor,
            focusedContainerColor = backgroundColor,
        ),
        border = CardDefaults.border(
            border = androidx.tv.material3.Border.None,
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
                shape = NavItemShape
            )
        ),
        shape = CardDefaults.shape(shape = NavItemShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isFocused || isSelected) NuvioColors.TextPrimary else NuvioColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            } else if (item.iconRes != null) {
                androidx.compose.ui.res.painterResource(id = item.iconRes)?.let { painter ->
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = item.label,
                        modifier = Modifier.size(18.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                            if (isFocused || isSelected) NuvioColors.TextPrimary else NuvioColors.TextSecondary
                        )
                    )
                }
            }

            Text(
                text = item.label,
                style = MaterialTheme.typography.titleMedium,
                color = if (isFocused || isSelected) NuvioColors.TextPrimary else NuvioColors.TextSecondary
            )
        }
    }
}
